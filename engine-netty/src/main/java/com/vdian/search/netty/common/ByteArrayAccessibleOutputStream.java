package com.vdian.search.netty.common;

import com.google.protobuf.BoundedByteAccessibleString;
import com.google.protobuf.ByteString;

import java.io.ByteArrayOutputStream;

/**
 * User: xukun.fyp
 * Date: 17/3/27
 * Time: 16:01
 */
public class ByteArrayAccessibleOutputStream extends ByteArrayOutputStream {
	public ByteArrayAccessibleOutputStream() {
	}

	public ByteArrayAccessibleOutputStream(int size) {
		super(size);
	}

	public byte[] getBuffer(){
		return buf;
	}


	public ByteString toByteString(){
		return new BoundedByteAccessibleString(buf,0,count);
	}
}
