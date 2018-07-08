package com.vdian.search.dlog.store;

import java.nio.ByteBuffer;

/**
 * User: xukun.fyp
 * Date: 17/5/29
 * Time: 06:40
 */
public class LogRecord{
	private long 				timestamp;
	private ByteBuffer			payload;

	public LogRecord(long timestamp, ByteBuffer payload) {
		this.timestamp = timestamp;
		this.payload = payload;
	}

	public LogRecord(int bufferSize){
		this.timestamp = -1;
		this.payload = ByteBuffer.allocate(bufferSize);
	}
	public LogRecord(){
		this(4096);
	}

	/**
	 * @param source
	 * @return  new position of source
	 */
	public int readFrom(ByteBuffer source){
		this.timestamp = source.getLong();
		int length = source.getInt();

		payload.clear();
		if(length > payload.remaining()){
			payload = ByteBuffer.allocate((int)Math.round(length * 1.1));
		}
		source.get(payload.array(),0,length);
		payload.position(length);
		return source.position();
	}

	public void writeTo(ByteBuffer dest){
		dest.putLong(timestamp);
		dest.putInt(payload.position());
		dest.put(payload.array(), 0, payload.position());
	}

	public int bytesSize(){
		return 8 + 4 + payload.position();
	}

	public long getTimestamp() {
		return timestamp;
	}
}

