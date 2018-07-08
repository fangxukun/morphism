//package com.vdian.engine.client.netty;
//
//import com.vdian.engine.client.netty.common.ByteBufWriter;
//import com.vdian.engine.client.netty.io.NettyRequest;
//import com.vdian.engine.client.netty.io.NettyResponse;
//import com.vdian.engine.client.netty.io.payload.RequestPayload;
//import io.netty.buffer.ByteBuf;
//
//import java.io.IOException;
//import java.net.InetSocketAddress;
//
///**
// * User: xukun.fyp
// * Date: 16/12/14
// * Time: 11:47
// * RequestContext 是由客户端调用线层初始化并最终读取，由NettyClient的IO线程写入[写入的NettyResponse是IO线程专属对象]，故写入
// * 时需要做对象的copy
// */
//public class RequestContext {
//	private long 					srid;
//	private RequestPayload			payload;
//	private NettyRequest			nettyRequest;
//	private NettyResponse			nettyResponse;
//
//	private InetSocketAddress		remoteAddress;
//	private boolean 				complete;
//
//
//	private long 					start;
//	private long 					cost;
//
//	public RequestContext(RequestPayload payload,InetSocketAddress remoteAddress){
//		this.complete = false;
//		this.payload = payload;
//		this.nettyRequest = new NettyRequest(payload);
//		this.remoteAddress = remoteAddress;
//		this.start = System.currentTimeMillis();
//	}
//
//
//	public NettyRequest getNettyRequest() {
//		return nettyRequest;
//	}
//
//	public NettyResponse getNettyResponse() {
//		return nettyResponse;
//	}
//
//	public InetSocketAddress getRemoteAddress() {
//		return remoteAddress;
//	}
//
//	public void completeRpc(NettyResponse response){
//		this.nettyResponse = response;
//		this.complete = true;
//		this.cost = System.currentTimeMillis() - this.start;
//	}
//
//	public void setRid(long rid,long srid){
//		this.srid = srid;
//		this.nettyRequest.setRid(rid, srid);
//	}
//
//	public long getSrid() {
//		return srid;
//	}
//
//	public long getCost() {
//		return cost;
//	}
//
//	public boolean isComplete() {
//		return complete;
//	}
//
//	public boolean isSuccess(){
//		return complete && nettyResponse != null && nettyResponse.isSuccess();
//	}
//
//	public long getRequestTimeout(long defaultTimeout){
//		return this.payload.getRequestTimeout(defaultTimeout);
//	}
//	/**
//	 * @param buffer
//	 * @throws IOException
//	 */
//	public void writeRequestBuffer(ByteBuf buffer) throws IOException {
//		ByteBufWriter writer = new ByteBufWriter(buffer);
//		nettyRequest.write(writer);
//	}
//}
