package com.morphism.search.sync.command.list;

import com.morphism.search.sync.command.Command;
import com.morphism.search.sync.command.Commands;
import com.morphism.search.sync.command.common.RequestCommand;
import io.netty.buffer.ByteBufInputStream;
import io.netty.buffer.ByteBufOutputStream;
import io.netty.channel.ChannelHandlerContext;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * User: xukun.fyp
 * Date: 17/5/11
 * Time: 10:02
 */
public class PathListRequest extends RequestCommand{
	public Path				path;

	public PathListRequest() {
	}

	public PathListRequest(Path path) {
		this.path = path;
	}

	@Override
	public int getCommandId() {
		return Commands.REQUEST_PATH_LIST;
	}

	@Override
	public Command newInstance() {
		return new PathListRequest();		//避免通过反射调用
	}

	@Override
	public void handleCommand(ChannelHandlerContext ctx) throws Exception {
		PathListResponse response = new PathListResponse();
		response.setRid(getRid());

		if(!Files.exists(path)){
			ctx.writeAndFlush(response);
			return;
		}

		Files.list(path).forEach((subPath) -> {
			boolean isDir = Files.isDirectory(subPath);
			response.addTPath(new PathListResponse.TPath(subPath.toString(), isDir));
		});
		ctx.writeAndFlush(response);
	}

	@Override
	public void write0(ByteBufOutputStream out) throws IOException {
		out.writeUTF(path.toString());
	}

	@Override
	public void readFields0(ByteBufInputStream in) throws IOException {
		this.path = Paths.get(in.readUTF());
	}
}
