//package com.vdian.engine.client.netty.io.payload;
//
//import com.koudai.rio.commons.io.NeuronReader;
//import com.koudai.rio.commons.io.NeuronWriter;
//
//import java.io.IOException;
//
///**
// * User: xukun.fyp
// * Date: 16/12/16
// * Time: 18:45
// */
//public class ExceptionPayload implements Payload{
//	private int			code		=	0;
//	private String 		message		=	"";
//
//	@Override
//	public byte getPayloadId() {
//		return PAYLOAD_ID_DEFAULT;
//	}
//
//	@Override
//	public void readFields(NeuronReader reader) throws IOException {
//		this.code = reader.readSVInt();
//		this.message = reader.readString();
//	}
//
//	@Override
//	public void reset() {
//	}
//
//	@Override
//	public void write(NeuronWriter writer) throws IOException {
//		writer.writeSVInt(this.code);
//		writer.writeString(this.message);
//	}
//
//	public void reset(int code,String message){
//		this.code = code;
//		this.message = message;
//	}
//
//	public int getCode() {
//		return code;
//	}
//
//	public String getMessage() {
//		return message;
//	}
//}
