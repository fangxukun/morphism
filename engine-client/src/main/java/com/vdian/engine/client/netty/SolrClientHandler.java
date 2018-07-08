//package com.vdian.engine.client.netty;
//
//import com.koudai.rio.commons.io.impl.NeuronByteArrayReader;
//import com.vdian.engine.client.netty.common.ByteBufReader;
//import com.vdian.engine.client.netty.io.NettyResponse;
//import com.vdian.engine.client.netty.io.payload.SolrResponsePayload;
//import com.vdian.engine.client.netty.session.RemoteSessionContext;
//import io.netty.buffer.ByteBuf;
//import io.netty.buffer.ByteBufUtil;
//import io.netty.channel.ChannelHandlerContext;
//import io.netty.channel.ChannelInboundHandlerAdapter;
//import io.netty.util.ReferenceCountUtil;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//
///**
// * User: xukun.fyp
// * Date: 16/12/13
// * Time: 18:50
// * ThreadSafe guaranteed by netty
// */
//public class SolrClientHandler extends ChannelInboundHandlerAdapter {
//	private static final Logger				LOGGER		= LoggerFactory.getLogger(SolrClientHandler.class);
//	private final RemoteSessionContext 		sessionContext;
//	private final ByteBufReader 			reader;
//	private final byte[]					buffer;
//
//	public SolrClientHandler(RemoteSessionContext sessionContext){
//		this.sessionContext = sessionContext;
//		this.buffer = new byte[8192];
//		this.reader = new ByteBufReader();
//	}
//
//	@Override
//	public void channelRead(ChannelHandlerContext ctx, Object message) throws Exception {
//		if(message instanceof ByteBuf){
//			ByteBuf buffer = (ByteBuf)message;
//			this.reader.reset(buffer);
//			byte payloadId = this.reader.readByte();
//			buffer.resetReaderIndex();
//
//			//此处之所以会New，在于NETTY的workThread和发送请求的业务线程之间始终存在Response的拷贝，另外一种方式是请求业务线程重用NettyResponse,做buffer的copy。
//			NettyResponse response = new NettyResponse(new SolrResponsePayload(this.buffer));
//			if(response.accept(payloadId)){
//				try{
//					response.readFields(reader);
//					sessionContext.complete(response);
//				}finally {
//					ReferenceCountUtil.release(message);
//				}
//			}else{
//				super.channelRead(ctx,message);
//			}
//		}
//	}
//
//}
