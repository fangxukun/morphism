//package com.vdian.engine.client.netty.io.payload;
//
//import com.koudai.rio.commons.io.NeuronReader;
//import com.koudai.rio.commons.io.NeuronWriter;
//import org.apache.solr.client.solrj.impl.BinaryResponseParser;
//import org.apache.solr.common.util.NamedList;
//
//import java.io.IOException;
//import java.io.InputStream;
//import java.io.OutputStream;
//import java.util.Arrays;
//
///**
// * User: xukun.fyp
// * Date: 16/12/14
// * Time: 11:14
// */
//public class SolrResponsePayload implements Payload {
//	private String 					contentType;
//	private boolean 				hasException;
//	private ExceptionPayload		exception;
//
//	private byte[]					buffer;			//此buffer生命周期对于客户端只存在于netty的workerThread内。
//	private int 					offset;
//	private OutputStream			outputStream;
//
//	private InputStream				inputStream;
//	private BinaryResponseParser	responseParser;
//	private NamedList				extractedResponse;
//
//	public SolrResponsePayload(byte[] outBuffer){
//		this.buffer = outBuffer;
//		this.exception = new ExceptionPayload();
//		this.reset();
//
//		this.outputStream = new OutputStream() {
//			@Override
//			public void write(int b) throws IOException {
//				ensureCapacity(offset + 1);
//				buffer[offset] = (byte) b;
//				offset += 1;
//			}
//		};
//
//		this.inputStream = new InputStream() {
//			int readPos = 0;
//			@Override
//			public int read() throws IOException {
//				return readPos < offset ? buffer[readPos++] : -1;
//
//			}
//		};
//		this.responseParser = new BinaryResponseParser();
//	}
//
//	@Override
//	public byte getPayloadId() {
//		return PAYLOAD_ID_SOLR_RESPONSE;
//	}
//
//	public void setContentType(String contentType) {
//		this.contentType = contentType;
//	}
//
//	@Override
//	public void readFields(NeuronReader reader) throws IOException {
//		this.contentType = reader.readString();
//
//		int length = reader.readSVInt();
//		this.ensureCapacity(length);
//		reader.readBytes(buffer, 0, length);
//		this.offset = length;
//
//		this.hasException = reader.readBoolean();
//		if(hasException){
//			this.exception.readFields(reader);
//		}
//
//		if(length != 0){
//			this.extractedResponse = this.responseParser.processResponse(inputStream,null);
//		}
//
//	}
//
//	@Override
//	public void write(NeuronWriter writer) throws IOException {
//		writer.writeString(contentType);
//
//		writer.writeSVInt(offset);
//		writer.writeBytes(buffer,0,offset);
//
//		writer.writeBoolean(hasException);
//		if(hasException){
//			this.exception.write(writer);
//		}
//	}
//
//	private void ensureCapacity(int minCapacity){
//		try{
//			if(this.buffer.length < minCapacity){
//				int capacity = Integer.highestOneBit(minCapacity) << 1;
//				this.buffer = Arrays.copyOf(buffer,capacity);
//			}
//		}catch (Throwable e){
//			System.err.println("size:" + minCapacity);
//			e.printStackTrace();
//		}
//	}
//
//	public void reset(){
//		this.offset = 0;
//		this.hasException = false;
//	}
//
//	public OutputStream wrapOutputStream(){
//		return outputStream;
//	}
//
//	public void setException(int code,String message){
//		this.hasException = true;
//		this.exception.reset(code,message);
//	}
//
//	public boolean isHasException() {
//		return hasException;
//	}
//
//	public ExceptionPayload getException() {
//		return exception;
//	}
//
//	public NamedList getNamedList(){
//		return extractedResponse;
//	}
//
//	@Override
//	public String toString() {
//		return "SolrResponsePayload{" +
//				"contentType='" + contentType + '\'' +
//				", hasException=" + hasException +
//				", exception=" + exception +
//				", buffer=" + Arrays.toString(buffer) +
//				", offset=" + offset +
//				", outputStream=" + outputStream +
//				'}';
//	}
//}
