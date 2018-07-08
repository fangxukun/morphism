package com.morphism.search.sync.command.path;

import com.morphism.search.sync.command.BinaryWritable;
import com.morphism.search.sync.command.Command;
import com.morphism.search.sync.command.Commands;
import io.netty.buffer.ByteBufInputStream;
import io.netty.buffer.ByteBufOutputStream;
import io.netty.channel.ChannelHandlerContext;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 * User: xukun.fyp
 * Date: 17/5/8
 * Time: 14:02
 * 路径同步详细的Meta信息(每个文件From,To,fileSize)。Server->Client,
 * Client接受到此命令会控制整体同步的进度。
 */
public class PathSyncMetaCommand implements Command {
	public List<SyncFileEntry>			syncFiles		=	new ArrayList<>();


	@Override
	public int getCommandId() {
		return Commands.COMMAND_PATH_SYNC_META;
	}

	@Override
	public Command newInstance() {
		return new PathSyncMetaCommand();
	}


	@Override
	public void write(ByteBufOutputStream out) throws IOException {
		out.writeInt(syncFiles.size());
		for(SyncFileEntry entry : syncFiles){
			entry.write(out);
		}
	}

	@Override
	public void readFields(ByteBufInputStream in) throws IOException {
		int size = in.readInt();
		for(int i=0;i<size;i++){
			SyncFileEntry entry = new SyncFileEntry();
			entry.readFields(in);
			syncFiles.add(entry);
		}
	}

	public void addFileEntry(Path fromFile,Path toFile,long fileSize){
		SyncFileEntry entry = new SyncFileEntry();
		entry.fromFile = fromFile;
		entry.toFile = toFile;
		entry.fileSize = fileSize;

		syncFiles.add(entry);
	}

	@Override
	public void handleCommand(ChannelHandlerContext ctx) throws Exception {
		throw new UnsupportedOperationException("this command must intercept by ClientSyncHandler");
	}

	public class SyncFileEntry implements BinaryWritable{
		public Path 			fromFile;
		public Path 			toFile;
		public long				fileSize;

		@Override
		public void write(ByteBufOutputStream out) throws IOException {
			out.writeUTF(fromFile.toString());
			out.writeUTF(toFile.toString());
			out.writeLong(fileSize);
		}

		@Override
		public void readFields(ByteBufInputStream in) throws IOException {
			fromFile = Paths.get(in.readUTF());
			toFile = Paths.get(in.readUTF());
			fileSize = in.readLong();
		}

	}
}
