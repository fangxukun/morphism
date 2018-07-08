package com.vdian.search.mocker;

import com.vdian.vmodel.core.ZookeeperClientFactory;
import com.vdian.vmodel.core.ZookeeperShardingModel;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.junit.Before;
import org.junit.Test;

/**
 * User: xukun.fyp
 * Date: 17/6/5
 * Time: 17:59
 */
public class ZKOperator {
	private ZookeeperShardingModel 	model;
	private String 					serviceName	=	"vsearch-vitem";
	private String 					hostKey		=	"";

	@Before
	public void init(){
		model = new ZookeeperShardingModel(serviceName,new ZookeeperClientFactory().setConnectString("zk1.idcvdian.com,zk2.idcvdian.com,zk3.idcvdian.com").setRetryPolicy(new ExponentialBackoffRetry(1000,3)));
		model.start();
	}


}
