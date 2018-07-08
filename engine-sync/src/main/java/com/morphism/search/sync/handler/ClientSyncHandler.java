package com.morphism.search.sync.handler;

import com.morphism.search.sync.command.path.FileSyncCommand;
import com.morphism.search.sync.command.path.PathSyncMetaCommand;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * User: xukun.fyp
 * Date: 17/5/10
 * Time: 11:32
 * Client端，控制整体文件写入的Handler
 * 		Client								Command										Server
 * 	1.PathSyncClient.syncPath	->		PathSyncCommand             ->
 *																				2. 解析目录并罗列所有文件以及文件大小
 * 								<-		PathSyncMetaCommand	    		<-
 *  3.ClientSyncHandler
 *  		|3.x				->		FileSyncCommand				->
 *  																			4. 按照命令发送文件
 *  								   DefaultFileRegine(ByteBuf)	<-
 *			|接受文件、完成，循环到3.x.
 *	  所有文件列表同步介绍，关闭Channel
 */
public class ClientSyncHandler extends ChannelInboundHandlerAdapter {
	private static final Logger 					LOGGER			= LoggerFactory.getLogger(ClientSyncHandler.class);
	private PathSyncMetaCommand pathCommand;
	private FileOutputStream 							fos;
	private volatile int 								transferBytes;
	private volatile boolean							done			=	true;
	private volatile int 								currentIdx		=	0;
	private volatile PathSyncMetaCommand.SyncFileEntry	currentFile		=	null;

	private final boolean 								closeOnComplete;
	private volatile CountDownLatch						latch;

	public ClientSyncHandler(boolean closeOnSyncComplete){
		this.closeOnComplete = closeOnSyncComplete;
		this.latch = new CountDownLatch(1);
	}

	public void reset(){
		this.latch = new CountDownLatch(1);
		this.currentIdx = 0;
	}

	@Override
	public void channelRead(ChannelHandlerContext ctx, Object message) throws Exception {
		if(done == false && message instanceof ByteBuf){
			ByteBuf in = (ByteBuf)message;

			if(transferBytes < currentFile.fileSize){
				transferBytes += in.readableBytes();
				in.readBytes(fos,in.readableBytes());
			}else{
				throw new IllegalStateException("unexpected byteBuf received!");
			}

			if(transferBytes == currentFile.fileSize){
				completeCurrent();
				syncNextFile(ctx);
			}
		}else if(message instanceof PathSyncMetaCommand){
			if(done == false){
				throw new RuntimeException(String.format("currentFile:%s is not complete(received %s)! but new command:%s coming!",currentFile,transferBytes,message));
			}

			pathCommand = (PathSyncMetaCommand)message;

			LOGGER.warn("------------------------------------------------------");
			LOGGER.warn("remote:{},remotePath0:{}",ctx.channel().remoteAddress(),pathCommand.syncFiles.get(0).fromFile);
			LOGGER.warn("remotePath size:{}",pathCommand.syncFiles.size());
			syncNextFile(ctx);
		}else{
			ctx.fireChannelRead(message);
		}

	}

	private void completeCurrent() throws IOException{
		done = true;
		transferBytes = 0;
		fos.flush();
		fos.close();

		LOGGER.warn("sync file {} complete! size:{}", currentFile.toFile,currentFile.fileSize < 1024 ? currentFile.fileSize + "B" : currentFile.fileSize/1024 + "KB");
	}


	private void syncNextFile(ChannelHandlerContext ctx) throws IOException{
		if(currentIdx < pathCommand.syncFiles.size()){
			currentFile = pathCommand.syncFiles.get(currentIdx++);

			if(currentFile.fileSize == 0){
				FileUtils.touch(currentFile.toFile.toFile());
				syncNextFile(ctx);
				return;
			}

			if(Files.notExists(currentFile.toFile.getParent())){
				Files.createDirectories(currentFile.toFile.getParent());
			}
			fos = new FileOutputStream(currentFile.toFile.toFile());
			done = false;
			writeSyncCommand(ctx);
		}else{
			LOGGER.warn("path sync complete,file count:{}", pathCommand.syncFiles.size());
			LOGGER.warn("------------------------------------------------------");

			this.latch.countDown();
			if(closeOnComplete){
				ctx.close();
			}
		}
	}

	private void writeSyncCommand(ChannelHandlerContext ctx){
		FileSyncCommand fileCommand = new FileSyncCommand();
		fileCommand.filePath = currentFile.fromFile;
		fileCommand.fileSize = currentFile.fileSize;
		ctx.writeAndFlush(fileCommand);
	}


	public void waitComplete() throws InterruptedException {
		this.latch.await();
	}

	public void waitComplete(long timeout,TimeUnit unit) throws InterruptedException {
		this.latch.await(timeout,unit);
	}

}
