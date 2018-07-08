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
// * Time: 15:30
// * TODO: 版本问题
// */
//public class NettyResponse implements BinaryWritable {
//	private byte 			payloadId;
//	private long 			rid;				//请求ID，批量请求时此ID相同，
//	private long 			srid;
//	private String 			responseIp;
//	private boolean 		success;
//	private Payload			payload;
//
//	public NettyResponse(Payload payload){
//		this.payloadId = payload.getPayloadId();
//		this.payload = payload;
//		this.responseIp = NetworkUtils.localIp();
//	}
//
//	public void reset(NettyRequest request){
//		this.rid = request.getRid();
//		this.srid = request.getSrid();
//		this.payload.reset();
//	}
//
//	public boolean accept(byte payloadId){
//		return payload.getPayloadId() == payloadId;
//	}
//
//	public boolean isSuccess() {
//		return success;
//	}
//
//	public void setSuccess(boolean success) {
//		this.success = success;
//	}
//
//	@Override
//	public void readFields(NeuronReader reader) throws IOException {
//		this.payloadId = reader.readByte();
//		this.rid = reader.readSVLong();
//		this.srid = reader.readSVLong();
//		this.responseIp = reader.readString();
//		this.success = reader.readBoolean();
//		this.payload.readFields(reader);
//	}
//
//	@Override
//	public void write(NeuronWriter writer) throws IOException {
//		writer.writeByte(payloadId);
//		writer.writeSVLong(rid);
//		writer.writeSVLong(srid);
//		writer.writeString(responseIp);
//		writer.writeBoolean(success);
//		this.payload.write(writer);
//	}
//
//
//	public long getSrid() {
//		return srid;
//	}
//
//	public byte getPayloadId() {
//		return payloadId;
//	}
//
//	public long getRid() {
//		return rid;
//	}
//
//	public String getResponseIp() {
//		return responseIp;
//	}
//
//	public Payload getPayload() {
//		return payload;
//	}
//
//	@Override
//	public String toString() {
//		return "NettyResponse{" +
//				"payloadId=" + payloadId +
//				", rid=" + rid +
//				", srid=" + srid +
//				", responseIp='" + responseIp + '\'' +
//				", success=" + success +
//				", payload=" + payload +
//				'}';
//	}
//}
