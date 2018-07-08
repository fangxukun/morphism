package com.vdian.search.coord.curator;

import com.google.common.base.Charsets;
import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.cache.*;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.curator.utils.ZKPaths;
import org.apache.zookeeper.data.Stat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * User: xukun.fyp
 * Date: 17/4/10
 * Time: 11:28
 */
public class CuratorAccessApi {
	private static final Logger 						LOGGER			= 	LoggerFactory.getLogger(CuratorAccessApi.class);

	private static Map<String,CuratorFramework>			clients			=	new ConcurrentHashMap<>();
	private CuratorFramework 							client;
	private ConcurrentMap<String, NodeCache>	 		nodeCaches;
	private ConcurrentMap<String, PathChildrenCache>	pathCaches;

	public CuratorAccessApi(String connectUrl, String namespace) {
		RetryPolicy retryPolicy = new ExponentialBackoffRetry(1000, 5);
		this.client = CuratorFrameworkFactory.builder().retryPolicy(retryPolicy).connectString(connectUrl).namespace(namespace).sessionTimeoutMs(60 * 1000).build();
		this.client.start();
		this.nodeCaches = new ConcurrentHashMap<>();
		this.pathCaches = new ConcurrentHashMap<>();

		LOGGER.warn("[CuratorAccessApi] init success! connectUrl:{},namespace:{}", connectUrl, namespace);
	}

	public CuratorAccessApi(CuratorFramework client) {
		this.client = client;
		this.nodeCaches = new ConcurrentHashMap<>();
		this.pathCaches = new ConcurrentHashMap<>();

		LOGGER.warn("[CuratorAccessApi] init by client success! connectUrl:{},namespace:{}", client.getZookeeperClient().getCurrentConnectionString(), client.getNamespace());
	}

	/**
	 * 对指定路径设置data
	 *
	 * @param path
	 * @param bytes
	 * @throws Exception
	 */
	public void setData(String path, byte[] bytes) throws Exception {
		client.setData().forPath(path, bytes);
		LOGGER.info("[CuratorAccessApi] set data,path:{},bytes:{}", path, new String(bytes));
	}

	/**
	 * 对指定路径添加子节点
	 *
	 * @param parent
	 * @param child
	 * @throws Exception
	 */
	public void addChild(String parent, String child) throws Exception {
		client.create().creatingParentsIfNeeded().forPath(ZKPaths.makePath(parent, child));
		LOGGER.info("[CuratorAccessApi] add child,parent:{},child", parent, child);
	}


	/**
	 * 创建指定路径
	 *
	 * @param path
	 * @throws Exception
	 */
	public void addPath(String path) throws Exception {
		client.create().creatingParentsIfNeeded().forPath(path);
		LOGGER.info("[CuratorAccessApi] add path, path:{}", path);
	}

	public void addPath(String path,byte[] data) throws Exception {
		if(exist(path)){
			setData(path,data);
		}else{
			client.create().creatingParentsIfNeeded().forPath(path,data);
		}
		LOGGER.info("[CuratorAccessApi] add path, path:{}", path);
	}

	public boolean exist(String path) throws Exception{
		Stat stat = client.checkExists().forPath(path);
		return stat == null ? false : true;
	}

	public void fireChange(String path,String data) throws Exception {
		byte[] bytes = data.getBytes(Charsets.UTF_8);
		if(exist(path)){
			setData(path,bytes);
		}else{
			client.create().creatingParentsIfNeeded().forPath(path,bytes);
		}
	}

	/**
	 * 删除指定路径下的子节点
	 *
	 * @param parent
	 * @param child
	 * @throws Exception
	 */
	public void removeChild(String parent, String child) throws Exception {
		client.delete().deletingChildrenIfNeeded().forPath(ZKPaths.makePath(parent, child));
		LOGGER.info("[CuratorAccessApi] remove child,parent:{},child:{}", parent, child);
	}

	public void removePath(String path) throws Exception {
		client.delete().deletingChildrenIfNeeded().forPath(path);
		LOGGER.info("[CuratorAccessApi] remove path,path:{}", path);
	}


	public byte[] getData(String path) throws Exception {
		byte[] data = client.getData().forPath(path);
		LOGGER.info("[CuratorAccessApi] get data,path:{},data:{}", path, new String(data));
		return data;
	}

	public String getDataString(String path) throws Exception {
		if(exist(path)){
			byte[] data = client.getData().forPath(path);
			LOGGER.info("[CuratorAccessApi] get data,path:{},data:{}", path, new String(data));
			return new String(data, Charsets.UTF_8);
		}else{
			LOGGER.warn("[CuratorAccessApi] get data,path:{} is not exsit", path);
			return null;
		}
	}

	public List<String> listChildren(String path) throws Exception {
		List<String> children = client.getChildren().forPath(path);
		LOGGER.info("[CuratorAccessApi] list child,path:{},children:{}", path, children);
		return children;
	}


	public void onDataChange(String path, final Listeners.NodeChangeListener listener) throws Exception {
		NodeCache cache = nodeCaches.get(path);
		synchronized (path) {
			if (cache == null) {
				cache = new NodeCache(client, path, false);
				cache.start(true);
				nodeCaches.put(path, cache);
			}
		}

		final NodeCache cacheNode = cache;
		cacheNode.getListenable().addListener(new NodeCacheListener() {
			@Override
			public void nodeChanged() throws Exception {
				listener.nodeChange(cacheNode.getCurrentData().getData());
				LOGGER.info("[CuratorAccessApi] data changed, currentData:{}", cacheNode.getCurrentData());
			}
		});

		LOGGER.info("[CuratorAccessApi] add data change listener, path:{}", path);
	}

	public void onChildAdded(String parentPath, final Listeners.NodeAddListener listener) throws Exception {
		PathChildrenCache cache = pathCaches.get(parentPath);
		synchronized (parentPath) {
			if (cache == null) {
				cache = new PathChildrenCache(client, parentPath, true);
				cache.start(PathChildrenCache.StartMode.BUILD_INITIAL_CACHE);
				pathCaches.put(parentPath, cache);
			}
		}
		cache.getListenable().addListener(new PathChildrenCacheListener() {
			@Override
			public void childEvent(CuratorFramework client, PathChildrenCacheEvent event) throws Exception {
				if (event.getType() == PathChildrenCacheEvent.Type.CHILD_ADDED) {
					listener.nodeAdd(event.getData().getPath(), event.getData().getData());
					LOGGER.info("[CuratorAccessApi] child added, newNode:{}", event.getData());
				}
			}
		});

		LOGGER.info("[CuratorAccessApi] add child add listener, parentPath:{}", parentPath);
	}

	public void onChildRemove(String parentPath, final Listeners.NodeRemoveListener listener) throws Exception {
		PathChildrenCache cache = pathCaches.get(parentPath);
		synchronized (parentPath) {
			if (cache == null) {
				cache = new PathChildrenCache(client, parentPath, true);
				cache.start(PathChildrenCache.StartMode.BUILD_INITIAL_CACHE);
				pathCaches.put(parentPath, cache);
			}
		}
		cache.getListenable().addListener(new PathChildrenCacheListener() {
			@Override
			public void childEvent(CuratorFramework client, PathChildrenCacheEvent event) throws Exception {
				if (event.getType() == PathChildrenCacheEvent.Type.CHILD_ADDED) {
					listener.nodeRemove(event.getData().getPath());
					LOGGER.info("[CuratorAccessApi] child removed, removeNode:{}", event.getData());
				}
			}
		});

		LOGGER.info("[CuratorAccessApi] add child remove listener, parentPath:{}", parentPath);
	}


}
