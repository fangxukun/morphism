package com.morphism.search.netty.client;

import com.google.common.base.Preconditions;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.pool.ChannelPoolHandler;
import io.netty.channel.pool.SimpleChannelPool;
import io.netty.util.concurrent.Future;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * User: xukun.fyp
 * Date: 16/11/3
 * Time: 16:34
 * 不同地址ChannelPool管理器
 */
public class ChannelPoolContext {
	private static final Logger												LOGGER	= LoggerFactory.getLogger(ChannelPoolContext.class);
	private final ConcurrentHashMap<InetSocketAddress,SimpleChannelPool> 	pools;
	private final Bootstrap													bootstrap;
	private final int 														connectTimeout;
	private final int 														toleranceFailCount;
	private final ChannelPoolHandler										poolHandler;

	public ChannelPoolContext(Bootstrap bootstrap,int connectTimeout,int toleranceFailCount,ChannelPoolHandler poolHandler){
		this.pools = new ConcurrentHashMap<>(64);
		this.bootstrap = bootstrap;
		this.connectTimeout = connectTimeout;
		this.toleranceFailCount = toleranceFailCount;
		this.poolHandler = poolHandler;
	}

	public void initPool(InetSocketAddress... addresses){
		int failedCount = 0;
		for(InetSocketAddress address : addresses){
			try{
				SimpleChannelPool pool = newChannelPool(address);
				pools.put(address,pool);
			}catch (Exception e){
				if(failedCount++ > toleranceFailCount){
					throw new RuntimeException("Channel connect failed,failedCount:" + failedCount + ";tolerance:" + toleranceFailCount);
				}
				LOGGER.error(String.format("Connect to engine:%s failed",address),e);
			}

		}
	}

	private synchronized SimpleChannelPool newChannelPool(InetSocketAddress address){
		if(pools.containsKey(address)){
			return pools.get(address);
		}

		bootstrap.remoteAddress(address);
		SimpleChannelPool pool = new SimpleChannelPool(bootstrap,this.poolHandler);

		Channel channel = null;
		try{
			channel = acquire(pool);
		}finally {
			if(channel != null){
				pool.release(channel);
			}
		}

		return pool;
	}


	private Channel acquire(SimpleChannelPool pool){
		Future<Channel> future = pool.acquire();
		boolean done = future.awaitUninterruptibly(connectTimeout, TimeUnit.MILLISECONDS);

		Preconditions.checkState(done && future.isSuccess(),"Server may shutdown!");
		return future.getNow();
	}

	public void shutdown(){
		for(SimpleChannelPool channelPool : pools.values()){
			channelPool.close();
		}
	}

	public Channel acquire(InetSocketAddress address) {
		SimpleChannelPool channelPool = pools.get(address);
		if(channelPool == null){
			channelPool = newChannelPool(address);
			pools.put(address,channelPool);
		}

		return acquire(channelPool);
	}

	public void release(InetSocketAddress address,Channel channel){
		SimpleChannelPool pool = pools.get(address);
		if(pool != null && channel != null){
			pool.release(channel);
		}
	}
}
