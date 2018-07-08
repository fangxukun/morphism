package com.morphism.search.sync.command.path;

import com.morphism.search.sync.command.Command;
import com.morphism.search.sync.command.Commands;
import com.morphism.search.sync.command.common.EchoCommand;
import io.netty.buffer.ByteBufInputStream;
import io.netty.buffer.ByteBufOutputStream;
import io.netty.channel.ChannelHandlerContext;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * User: xukun.fyp
 * Date: 17/5/10
 * Time: 08:57
 * 路径同步命令，client-->Server.
 */
public class PathSyncCommand implements Command {
	public Path			fromPath;
	public Path			toPath;


	@Override
	public int getCommandId() {
		return Commands.COMMAND_PATH_SYNC;
	}

	@Override
	public Command newInstance() {
		return new PathSyncCommand();
	}

	@Override
	public void write(ByteBufOutputStream out) throws IOException {
		out.writeUTF(fromPath.toString());
		out.writeUTF(toPath.toString());
	}

	@Override
	public void readFields(ByteBufInputStream in) throws IOException {
		fromPath = Paths.get(in.readUTF());
		toPath = Paths.get(in.readUTF());
	}


	@Override
	public void handleCommand(ChannelHandlerContext ctx) throws Exception {
		if(!Files.exists(fromPath)){
			EchoCommand echo = new EchoCommand();
			echo.success = false;
			echo.message = String.format("[PathListAction] from path not exist,path:%s",fromPath);
			ctx.writeAndFlush(echo);
			return;
		}

		PathSyncMetaCommand pathCommand = new PathSyncMetaCommand();
		if(fromPath.toFile().isFile()){
			Path toSubPath = Paths.get(toPath.toString(), fromPath.getFileName().toString());
			long fileSize = Files.size(fromPath);
			pathCommand.addFileEntry(fromPath,toSubPath,fileSize);
		}else{
			walkPathForSync(pathCommand,fromPath,toPath);
		}

		ctx.writeAndFlush(pathCommand);
	}

	private void walkPathForSync(PathSyncMetaCommand pathCommand,Path fromPath,Path toPath) throws IOException {
		try(DirectoryStream<Path> ds = Files.newDirectoryStream(fromPath)){
			for(Path fromSubPath : ds){
				Path toSubPath = Paths.get(toPath.toString(), fromSubPath.getFileName().toString());
				if(fromSubPath.toFile().isFile()){
					long fileSize = Files.size(fromSubPath);
					pathCommand.addFileEntry(fromSubPath,toSubPath,fileSize);
				}else{
					walkPathForSync(pathCommand,fromSubPath,toSubPath);
				}
			}
		}
	}
}
