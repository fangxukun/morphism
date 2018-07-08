package com.morphism.search.sync.demo;

import com.morphism.search.sync.command.list.PathListResponse;
import com.morphism.search.sync.PathSyncClient;
import com.morphism.search.sync.PathSyncServer;
import org.junit.Test;

import java.io.IOException;

/**
 * User: xukun.fyp
 * Date: 17/5/10
 * Time: 16:36
 */
public class PathSyncDemo {
	@Test
	public void startServer() throws InterruptedException {
		PathSyncServer server = new PathSyncServer();
		server.start();

		server.sync();			//此处只是为了这个Server持续Running,实际使用时，不需要调用sync() ,just for test!
	}

	@Test
	public void testSync() throws IOException, InterruptedException {
		PathSyncClient client = new PathSyncClient("localhost",10,true);
		client.syncPath("/Users/fangxukun/.m2/repository/com/taobao", "/Users/fangxukun/data");
	}

	@Test
	public void testListPath() throws Exception{
		PathSyncClient client = new PathSyncClient("localhost",false);
		PathListResponse response = client.listPath("/Users/fangxukun/.m2/repository/com/taobao");
		System.out.println(response);

		client.shutdown();
	}
}
