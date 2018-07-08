package com.vdian.search.mocker;

import com.vdian.engine.server.cloud.VModelCloud;
import com.vdian.search.netty.client.NettyClient;
import com.vdian.search.netty.common.ClientLayout;
import com.vdian.search.netty.common.RequestContext;
import com.vdian.vmodel.core.ShardEntry;
import com.vdian.vmodel.core.ZookeeperClientFactory;
import com.vdian.vmodel.core.ZookeeperShardingModel;
import com.vdian.vmodel.node.ServerNode;
import org.apache.commons.lang.math.RandomUtils;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.solr.client.solrj.request.UpdateRequest;
import org.apache.solr.common.SolrInputDocument;
import org.junit.Test;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.concurrent.atomic.AtomicLong;

/**
 * User: xukun.fyp
 * Date: 17/5/22
 * Time: 17:19
 */
public class RecoveryChecker {
	public final static AtomicLong 	sequence 		= 	new AtomicLong(40000);
	public ZookeeperShardingModel	model;
	private boolean 				printOnce		=	true;

	@Test
	public void insertData() throws IOException, InterruptedException {
		String serviceName = "morphism";
		NettyClient client = new NettyClient(ClientLayout.defaultNettyLayout("nc"));
		model = new ZookeeperShardingModel(serviceName,new ZookeeperClientFactory().setConnectString("zk1.daily.idcvdian.com,zk2.daily.idcvdian.com,zk3.daily.idcvdian.com").setRetryPolicy(new ExponentialBackoffRetry(1000,3)));
		model.start();

		final RequestContext context = new RequestContext();
		ShardEntry entry = model.getShardEntry(0);
		for (int i = 1; i <= 1000000; i++) {
			if(entry.getAllWriters().size() == 2 && printOnce){
				System.out.println("two writers");
				printOnce = false;
			}

			SolrInputDocument document = newDocument();
			for(ServerNode node :entry.getAllWriters()){


				UpdateRequest request = new UpdateRequest();
				request.add(document);
				InetSocketAddress server = new InetSocketAddress(node.getHost(), node.getNettyPort());

				context.reset(request, server, "morphism");
				client.invokeOnce(context);

				if(!context.isSuccess()){
					System.out.println("insert failed! " + node.getHost());
				}
				if(i % 10 == 0){
					System.out.println("insert " + i);
				}
			}

			Thread.sleep(500);
		}

	}


	private static SolrInputDocument newDocument() {
		SolrInputDocument document = new SolrInputDocument();
		document.setField("item_id", sequence.incrementAndGet());
		document.setField("add_time",System.currentTimeMillis());
		document.setField("audition_score", RandomUtils.nextDouble());
		document.setField("seller_id",RandomUtils.nextInt(10000));
		document.setField("item_title", newItemTitle());
		return document;
	}


	private static String[] keywordSeeds = new String[]{
			"箱包皮具","热销女包","男包","牛津","纺涤","纶软","深灰色","军绿色","手机袋","男士包袋",
			"2016","平底","小白鞋","一脚蹬","懒人鞋","厚底","休闲","单鞋","乐福鞋","女鞋红",
			"奔驰","专车","专用","氙气灯","高端大气","上档次"
	};

	private static String newItemTitle(){
		int wordCount = RandomUtils.nextInt(5) + 5;
		StringBuilder builder = new StringBuilder();
		for(int i=0;i<wordCount;i++){
			builder.append(keywordSeeds[RandomUtils.nextInt(keywordSeeds.length)]);
		}
		return builder.toString();
	}
}
