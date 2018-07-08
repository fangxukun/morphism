package com.google.protobuf;

/**
 * User: xukun.fyp
 * Date: 17/3/27
 * Time: 14:38
 */
public class BoundedByteAccessibleString extends BoundedByteString {
	public BoundedByteAccessibleString(byte[] bytes, int offset, int length) {
		super(bytes, offset, length);
	}
}
