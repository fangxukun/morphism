//package com.vdian.engine.client.netty.common;
//
//import com.koudai.rio.commons.io.impl.NeuronBaseReader;
//import io.netty.buffer.ByteBuf;
//
//import java.io.IOException;
//import java.nio.ByteBuffer;
//
//public class ByteBufReader extends NeuronBaseReader {
//
//	private ByteBuf byteBuf;
//
//	private int		offset	= 0;
//
//	public ByteBufReader() {
//
//	}
//
//	@Override
//	public void close() throws IOException {
//		this.byteBuf = null;
//	}
//
//	@Override
//	public long getRelativeOffset() {
//		// NO FLIP/CLEAR/RESET ops BEFORE THIS CALL!!
//		return this.byteBuf.readerIndex() - this.offset;
//	}
//
//	@Override
//	public byte readRawByte() throws IOException {
//		return this.byteBuf.readByte();
//	}
//
//	@Override
//	public void readRawBytes(byte[] dst, int offset, int length) throws IOException {
//		this.byteBuf.readBytes(dst, offset, length);
//	}
//
//	@Override
//	public void readRawBytes(ByteBuffer byteBuffer, int length) throws IOException {
//		for (int i = 0; i < length; ++i) {
//			byteBuffer.put(this.byteBuf.readByte());
//		}
//
//	}
//
//	public void reset(ByteBuf byteBuf) {
//		this.byteBuf = byteBuf;
//		this.offset = byteBuf.readerIndex();
//	}
//
//	@Override
//	public void skip(int bytes) throws IOException {
//		this.byteBuf.skipBytes(bytes);
//
//	}
//
//}
