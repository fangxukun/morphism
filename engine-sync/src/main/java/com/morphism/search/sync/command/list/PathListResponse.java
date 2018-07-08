package com.morphism.search.sync.command.list;

import com.morphism.search.sync.command.BinaryWritable;
import com.morphism.search.sync.command.Command;
import com.morphism.search.sync.command.Commands;
import com.morphism.search.sync.command.common.ResponseCommand;
import io.netty.buffer.ByteBufInputStream;
import io.netty.buffer.ByteBufOutputStream;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 * User: xukun.fyp
 * Date: 17/5/11
 * Time: 13:58
 */
public class PathListResponse extends ResponseCommand {
	private List<TPath> 		tPaths = new ArrayList<>();

	@Override
	public int getCommandId() {
		return Commands.RESPONSE_PATH_LIST;
	}

	@Override
	public Command newInstance() {
		return new PathListResponse();
	}

	public List<TPath> getTPaths() {
		return tPaths;
	}

	@Override
	public void write0(ByteBufOutputStream out) throws IOException {
		out.writeInt(tPaths.size());
		for (TPath tPath : tPaths) {
			tPath.write(out);
		}
	}

	@Override
	public void readFields0(ByteBufInputStream in) throws IOException {
		int size = in.readInt();
		for (int i = 0; i < size; i++) {
			TPath tPath = new TPath();
			tPath.readFields(in);
			tPaths.add(tPath);
		}
	}

	public void addTPath(TPath path) {
		tPaths.add(path);
	}


	public String toString(){
		StringBuilder builder = new StringBuilder("\n");
		tPaths.stream().forEach((tPath)->{
			builder.append(tPath).append("\n");
		});
		return builder.toString();
	}

	public static class TPath implements BinaryWritable,Comparable<TPath> {
		public String pathName;
		public boolean isDirectory;

		public TPath() {
		}

		public TPath(String pathName, boolean isDirectory) {
			this.pathName = pathName;
			this.isDirectory = isDirectory;
		}

		@Override
		public void write(ByteBufOutputStream out) throws IOException {
			out.writeUTF(pathName);
			out.writeBoolean(isDirectory);
		}

		@Override
		public void readFields(ByteBufInputStream in) throws IOException {
			this.pathName = in.readUTF();
			this.isDirectory = in.readBoolean();
		}

		public String getFileName(){
			return Paths.get(pathName).getFileName().toString();
		}

		@Override
		public int compareTo(TPath o) {
			return pathName.compareTo(o.pathName);
		}

		public String toString() {
			return "dir:" + isDirectory + "\t path:" + pathName;
		}
	}
}
