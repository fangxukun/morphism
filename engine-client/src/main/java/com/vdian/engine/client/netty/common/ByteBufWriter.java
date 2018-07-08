//package com.vdian.engine.client.netty.common;
//
//import com.koudai.rio.commons.io.impl.NeuronBaseWriter;
//import io.netty.buffer.ByteBuf;
//import io.netty.util.ReferenceCountUtil;
//
//import java.io.IOException;
//import java.nio.ByteBuffer;
//
///**
// * User: xukun.fyp
// * Date: 16/12/15
// * Time: 14:48
// */
//public class ByteBufWriter extends NeuronBaseWriter {
//	private ByteBuf 		byteBuf;
//	private int 			relativeOffset;
//
//	public ByteBufWriter() {
//
//	}
//
//	public ByteBufWriter(final ByteBuf byteBuf) {
//		this.byteBuf = byteBuf;
//	}
//
//	public void reset(final ByteBuf byteBuf) {
//		this.byteBuf = byteBuf;
//		this.relativeOffset = 0;
//	}
//
//	@Override
//	public void flush() throws IOException {
//
//	}
//
//	@Override
//	public void writeRawByte(int b) throws IOException {
//		byteBuf.writeByte(b);
//		++relativeOffset;
//	}
//
//	@Override
//	public void writeRawBytes(byte[] src, int offset, int length) throws IOException {
//		byteBuf.writeBytes(src, offset, length);
//		relativeOffset += length;
//	}
//
//	@Override
//	public void writeRawBytes(ByteBuffer byteBuffer, int length) throws IOException {
//		for (int i = 0; i < length; i++) {
//			byteBuf.writeByte(byteBuffer.get());
//		}
//		relativeOffset += length;
//	}
//
//	@Override
//	public void close() throws IOException {
//		ReferenceCountUtil.release(byteBuf);
//	}
//
//	@Override
//	public int getRelativeOffset() {
//		return relativeOffset;
//	}
//}
