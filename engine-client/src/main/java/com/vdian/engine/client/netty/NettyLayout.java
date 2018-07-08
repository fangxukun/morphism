//package com.vdian.engine.client.netty;
//
//import com.koudai.rio.commons.properties.JsonProperties;
//
///**
// * User: xukun.fyp
// * Date: 16/12/13
// * Time: 18:13
// */
//public class NettyLayout {
//
//	public final String 		name;
//	public final int 			workThreadNum;
//	public final int 			connectTimeout;
//	public final int 			recallTimeout;
//	public final int 			toleranceFailedConnect;
//	public final int 			toleranceFailedRequest;
//	public final int			maxFrameLength;
//
//	public NettyLayout(JsonProperties clientLayout){
//		this.name = clientLayout.getString("name","default");
//		this.workThreadNum = clientLayout.getInteger("work-thread-num", 20);
//		this.connectTimeout = clientLayout.getInteger("connect-timeout", 3000);
//		this.recallTimeout = clientLayout.getInteger("recall-timeout", 3000);
//		this.toleranceFailedConnect = clientLayout.getInteger("tolerance-failed-connect", 0);
//		this.toleranceFailedRequest = clientLayout.getInteger("tolerance-failed-request",0);
//		this.maxFrameLength = clientLayout.getInteger("max-frame-length",10 * 1024 * 1024);
//	}
//
//	public static NettyLayout defaultNettyLayout(String name){
//		JsonProperties root = new JsonProperties();
//		root.setString("name",name);
//		return new NettyLayout(root);
//	}
//}
