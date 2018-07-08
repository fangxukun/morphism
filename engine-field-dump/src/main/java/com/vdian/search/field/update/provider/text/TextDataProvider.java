package com.vdian.search.field.update.provider.text;

import com.google.common.base.Charsets;
import com.google.common.collect.Sets;
import com.vdian.search.field.update.UpdateContext;
import com.vdian.search.field.update.UpdateException;
import com.vdian.search.field.update.provider.DataProvider;
import com.vdian.search.field.update.provider.Record;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

/**
 * User: xukun.fyp
 * Date: 17/4/12
 * Time: 10:23
 */
public class TextDataProvider implements DataProvider {
	private final Set<String> 		IGNORE_FILES	= Sets.newHashSet("_SUCCESS",".+\\.crc");
	private	final Path				localPath;
	private final List<Path>		subDataFiles;
	private final UpdateContext		context;
	private long 					fileLength;

	public TextDataProvider(UpdateContext context) {
		this.localPath = Paths.get(context.layout.localPath);
		this.subDataFiles = new ArrayList<>();
		this.context = context;
	}

	public void init() throws UpdateException, IOException {
		if(!Files.exists(localPath)){
			throw new UpdateException(String.format("localPath:%s not exist!",localPath));
		}

		if(Files.isDirectory(localPath)){
			try(DirectoryStream<Path> ds = Files.newDirectoryStream(localPath)){
				for(Path path : ds){
					boolean ignore = false;
					for(String ignorePath : IGNORE_FILES){
						if(path.getFileName().toString().matches(ignorePath)){
							ignore = true;
							break;
						}
					}
					if(!ignore){
						subDataFiles.add(path);
						fileLength += Files.size(path);
					}
				}
			}
		}else{
			subDataFiles.add(localPath);
		}

		if(subDataFiles.size() == 0){
			throw new RuntimeException(String.format("localPath:%s is empty!",localPath));
		}
	}

	public int fileCount(){
		return subDataFiles.size();
	}

	public long fileLength(){
		return fileLength;
	}


	@Override
	public Iterable<Record> readRecords() {
		return new Iterable<Record>() {
			@Override
			public Iterator<Record> iterator() {
				return new DirectoryIterator(subDataFiles,context);
			}
		};
	}

	public class DirectoryIterator implements Iterator<Record>{
		private List<Path> 			files;
		private int 				currentIdx;
		private Scanner				current;
		private TextRecord			record;

		public DirectoryIterator(List<Path> files,UpdateContext context){
			try{
				this.files = files;
				this.currentIdx = 0;
				this.record = new TextRecord(context);
			}catch (Exception e){
				throw new RuntimeException(e);
			}
		}

		@Override
		public boolean hasNext() {
			try{
				if(current == null){
					current = new Scanner(files.get(currentIdx),Charsets.UTF_8.name());
				}

				while(!current.hasNextLine()){
					if(currentIdx + 1 < files.size()){
						current.close();
						current = new Scanner(files.get(currentIdx),Charsets.UTF_8.name());
						currentIdx++;
					}else{
						return false;
					}
				}

				return true;
			}catch (IOException e){
				throw new RuntimeException(e);
			}
		}

		@Override
		public Record next() {
			String line = current.nextLine();
			this.record.reset(line);
			return this.record;
		}

		@Override
		public void remove() {
			throw new UnsupportedOperationException();
		}
	}


	public static void main(String[] args){
		Set<String> 		IGNORE_FILES	= Sets.newHashSet("_SUCCESS",".+\\.crc");
		for(String pattern : IGNORE_FILES){
			System.out.println("test.crc".matches(pattern));
		}
	}
}
