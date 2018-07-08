package com.vdian.search.field.update.test;

import com.google.common.base.Charsets;
import com.google.common.base.Preconditions;
import com.google.common.io.Files;
import com.google.common.io.Resources;
import com.vdian.search.configuration.EngineConfiguration;
import com.vdian.search.coord.curator.CuratorAccessApi;
import com.vdian.search.coord.curator.Paths;
import com.vdian.search.coord.curator.joint.CommonSteps;
import com.vdian.search.coord.curator.joint.Joint;
import com.vdian.search.field.dump.DumpLayout;
import com.vdian.search.field.dump.FieldsDumper;
import com.vdian.search.netty.client.NettyClient;
import com.vdian.search.netty.common.ClientLayout;
import com.vdian.search.netty.common.RequestContext;
import com.vdian.search.netty.server.bootstrap.FileBootstrap;
import org.apache.commons.io.FileUtils;
import org.apache.solr.client.solrj.request.UpdateRequest;
import org.apache.solr.common.SolrInputDocument;
import org.junit.Test;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

/**
 * User: xukun.fyp
 * Date: 17/4/10
 * Time: 11:11
 */
public class UpdateDemo {

	private final String JOINT_ZK			=	"10.1.100.75:2181,10.1.100.76:2181,10.1.100.77:2181";
	private final String NAMESPACE			=	"search-joint";
	private final String SERVICE_NAME		=	"vitem";


	private FileBootstrap				server;

	@Test
	public void startServer() throws Exception {
		server = new FileBootstrap();
		server.initConfiguration();
		server.init();

		EngineConfiguration conf = server.getConfiguration();

		Preconditions.checkArgument(SERVICE_NAME.equals(conf.serviceName));
		FieldsDumper dumper = new FieldsDumper(JOINT_ZK,NAMESPACE,server.getCores(),conf.serviceName);
		dumper.init();

		server.sync();
	}


	@Test
	public void update() throws IOException {
		InetSocketAddress serverAddress = new InetSocketAddress("localhost", 8008);
		String coreName = "vitem";

		NettyClient client = new NettyClient(ClientLayout.defaultNettyLayout("nettyBm"));
		final RequestContext context = new RequestContext();

		for(int i=0;i<10;i++){
			SolrInputDocument document = new SolrInputDocument();
			document.setField("item_id",i);
			Map<String,Object> updateMap = new HashMap<>();
			updateMap.put("set",2);
			document.addField("status", updateMap);

			UpdateRequest request = new UpdateRequest();
			request.add(document);
			context.reset(request, serverAddress, coreName);
			client.invokeOnce(context);
			Preconditions.checkState(context.isSuccess());
		}
	}

	@Test
	public void startUpdate() throws Exception {
		CuratorAccessApi api = new CuratorAccessApi(JOINT_ZK,NAMESPACE);
		Joint joint = new Joint(api, Paths.getUpdateJointPath(SERVICE_NAME), CommonSteps.STEP_1);
		Path path = java.nio.file.Paths.get(Resources.getResource("dump-layout.json").getPath());
		String content = new String(java.nio.file.Files.readAllBytes(path),Charsets.UTF_8);
		DumpLayout layout = DumpLayout.fromJsonString(content);
		joint.reportStart(layout.toJsonString());
	}

	@Test
	public void stopUpdate() throws Exception{
		CuratorAccessApi api = new CuratorAccessApi(JOINT_ZK,NAMESPACE);
		Joint joint = new Joint(api,Paths.getUpdateJointPath(SERVICE_NAME),CommonSteps.STEP_1);
		joint.reportStop();
	}



	@Test
	public void touchStatus() throws Exception {
		CuratorAccessApi api = new CuratorAccessApi(JOINT_ZK,NAMESPACE);
		Joint joint = new Joint(api, Paths.getUpdateJointPath(SERVICE_NAME), CommonSteps.STEP_2);
		System.out.println("current:" + joint.currentData());
		System.out.println("children:" + joint.childDataList());
	}




	private static final int 				docSize			=	1000000;
	private static final char				DELIMITER		=	'\t';
	private static final char 				ROW_DELIMITER	=	'\n';

	@Test
	public void prepareDocValueData() throws IOException {
//		File file = new File("/Users/fangxukun/remote-data/vitem_doc_value.data");
		File file = new File("/data/local/vitem_doc_value.data");
		java.nio.file.Files.delete(java.nio.file.Paths.get(file.getPath()));
		BufferedWriter writer = Files.newWriter(file, Charsets.UTF_8);

		for(int i=1;i<=docSize;i++){
			writer.write(String.valueOf(i));
			writer.write(DELIMITER);
			writer.write(String.valueOf(1));
			writer.write(DELIMITER);
			writer.write(String.valueOf(2));
			writer.write(ROW_DELIMITER);
		}
		writer.flush();
		writer.close();
	}


	@Test
	public void prepareNettyServerData() throws Exception {

		FileBootstrap server =  new FileBootstrap();
		server.initConfiguration();


		InetSocketAddress serverAddress = new InetSocketAddress("localhost", server.getPort());
		String coreName = server.getConfiguration().coreConfigurations[0].coreName;
		if(java.nio.file.Files.exists(java.nio.file.Paths.get(server.getConfiguration().dataHome))){
			FileUtils.deleteDirectory(new File(server.getConfiguration().dataHome));
		}
		server.init();

		NettyClient client = new NettyClient(ClientLayout.defaultNettyLayout("nettyBm"));
		final RequestContext context = new RequestContext();

		for(int i=0;i<docSize;i++){
			SolrInputDocument document = DocumentGenerator.newDocument();
			UpdateRequest request = new UpdateRequest();
			request.add(document);
			context.reset(request, serverAddress, coreName);
			client.invokeOnce(context);
			Preconditions.checkState(context.isSuccess());
		}

	}
}
