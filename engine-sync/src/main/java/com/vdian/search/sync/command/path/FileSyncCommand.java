package com.vdian.search.sync.command.path;

import com.vdian.search.sync.command.Command;
import com.vdian.search.sync.command.Commands;
import io.netty.buffer.ByteBufInputStream;
import io.netty.buffer.ByteBufOutputStream;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.DefaultFileRegion;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * User: xukun.fyp
 * Date: 17/5/10
 * Time: 09:59
 * 请求文件内容。
 *  Client			 		        Server
 *  1.send FileSyncCommand
 *  								2.call handleCommand() and write file to client
 *  3.receive file content...
 */
public class FileSyncCommand  implements Command{
	private static final Logger		LOGGER		= LoggerFactory.getLogger(FileSyncCommand.class);
	public Path						filePath;
	public long 					fileSize;

	@Override
	public int getCommandId() {
		return Commands.COMMAND_FILE_SYNC;
	}

	@Override
	public Command newInstance() {
		return new FileSyncCommand();
	}

	@Override
	public void write(ByteBufOutputStream out) throws IOException {
		out.writeUTF(filePath.toString());
		out.writeLong(fileSize);
	}

	@Override
	public void readFields(ByteBufInputStream in) throws IOException {
		filePath = Paths.get(in.readUTF());
		fileSize = in.readLong();
	}


	@Override
	public void handleCommand(ChannelHandlerContext ctx) throws Exception{
		RandomAccessFile file = new RandomAccessFile(filePath.toFile(),"r");
		assert file.length() == fileSize;
		ctx.writeAndFlush(new DefaultFileRegion(file.getChannel(), 0, fileSize));
		LOGGER.warn("sending file:{}",filePath.toString());
	}

}
