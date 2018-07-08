//package com.vdian.engine.client.netty.io;
//
//import com.koudai.rio.commons.io.BinaryWritable;
//import com.koudai.rio.commons.io.NeuronReader;
//import com.koudai.rio.commons.io.NeuronWriter;
//import com.vdian.engine.client.netty.common.NetworkUtils;
//import com.vdian.engine.client.netty.io.payload.Payload;
//
//import java.io.IOException;
//
///**
// * User: xukun.fyp
// * Date: 16/12/13
// * Time: 15:29
// */
//public class NettyRequest implements BinaryWritable{
//	private byte 			componentId;		//用于进行服务路由
//	private long 			rid;				//服务内进行Request路由
//	private long 			srid;
//	private String 			requestIp;			//请求服务器Ip
//	private Payload 		payload;			//具体请求的数据。
//
//	public NettyRequest(Payload payload){
//		this.componentId = payload.getPayloadId();
//		this.payload = payload;
//		this.requestIp = NetworkUtils.localIp();
//	}
//
//	public void setRid(long rid,long srid){
//		this.rid = rid;
//		this.srid = srid;
//	}
//
//
//	public boolean accept(byte payloadId){
//		return payload.getPayloadId() == payloadId;
//	}
//
//	@Override
//	public void readFields(NeuronReader reader) throws IOException {
//		this.componentId = reader.readByte();
//		this.rid = reader.readFLong();
//		this.requestIp = reader.readString();
//		this.payload.readFields(reader);
//	}
//
//
//	@Override
//	public void write(NeuronWriter writer) throws IOException {
//		writer.writeByte(componentId);
//		writer.writeFLong(rid);
//		writer.writeString(requestIp);
//		this.payload.write(writer);
//	}
//
//	public byte getComponentId() {
//		return componentId;
//	}
//
//	public long getRid() {
//		return rid;
//	}
//
//	public long getSrid() {
//		return srid;
//	}
//
//	public Payload getPayload() {
//		return payload;
//	}
//
//	public <T extends Payload> T getPayload(Class<T> clazz){
//		return clazz.cast(payload);
//	}
//	public String getRequestIp() {
//		return requestIp;
//	}
//
//	@Override
//	public String toString() {
//		return "NettyRequest{" +
//				"componentId=" + componentId +
//				", rid=" + rid +
//				", srid=" + srid +
//				", requestIp='" + requestIp + '\'' +
//				", payload=" + payload +
//				'}';
//	}
//}
