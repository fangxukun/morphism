package com.vdian.search.dlog.store;

import com.koudai.rio.commons.annotation.ThreadSafe;
import org.apache.lucene.util.RefCount;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

/**
 * User: xukun.fyp
 * Date: 17/5/27
 * Time: 14:26
 * 管理DLog目录
 */
@ThreadSafe
public class StoreDirectory {
	private static final Logger				LOGGER					= 	LoggerFactory.getLogger(StoreDirectory.class);

	private final Path						storeDirectory;
	private final List<Path> 				filePaths;
	private final StoreLayout				layout;

	private LogStore						current;
	private ReentrantLock					newStoreLock;
	private Map<Path,RefCount<LogStore>>	logStores				=	new ConcurrentHashMap<>();

	public StoreDirectory(Path storeDirectory,StoreLayout layout) throws IOException {
		ensureDirectory(storeDirectory);

		this.layout = layout;
		this.storeDirectory = storeDirectory;

		List<Path> filePaths = Files.list(storeDirectory)
							  .filter(path -> validLogFile(path))
							  .sorted(Comparator.comparingLong(path -> Long.parseLong(path.getFileName().toString())))
							  .collect(Collectors.toList());
		this.filePaths = new CopyOnWriteArrayList<>(filePaths);

		if(!filePaths.isEmpty()){
			Path path = this.filePaths.get(filePaths.size() - 1);
			current = obtainStore(path,false);
		}else{
			Path path = newStoreFile();
			current = obtainStore(path,false);
			this.filePaths.add(path);
		}
		this.newStoreLock = new ReentrantLock();
	}

	public void write(LogRecord record) throws IOException {
		newStoreLock.lock();
		try{
			ensureCapacity(record);
			current.write(record);
		}finally {
			newStoreLock.unlock();
		}
	}

	public LogStream logStream(long timestamp) throws IOException {
		return new LogStreamImpl(this,timestamp);
	}

	long oldestTimestamp() throws IOException {
		Path oldestPath = filePaths.get(0);
		LogStore store = obtainStore(oldestPath,true);
		return store.meta.startTimestamp.get();
	}

	long seek(long timestamp) throws IOException {
		for(Path path : filePaths){
			LogStore store = obtainStore(path,true);
			try{
				long position = store.seek(timestamp);
				if(position != -1){
					return position;
				}
			}finally {
				returnStore(store);
			}
		}
		return -1;
	}

	public LogStore locateStore(long position) throws IOException {
		Path findPath = null;
		for(Path path : filePaths){
			long startPosOfFile = Long.parseLong(path.getFileName().toString());
			if(startPosOfFile <= position){
				findPath = path;
			}
		}
		return obtainStore(findPath,true);
	}


	private void ensureCapacity(LogRecord record) throws IOException {
		if(current.ensureWriteCapacity(record)){
			return;
		}

		Path newStorePath = newStoreFile();
		LogStore newStore = obtainStore(newStorePath,false);

		current.markFull();
		current.close();
		filePaths.add(newStorePath);
		returnStore(current);

		current = newStore;
		deleteOldLogs();
	}

	private void deleteOldLogs() throws IOException {
		AtomicInteger fileNum = new AtomicInteger(0);
		Files.list(storeDirectory)
			 .sorted(Comparator.comparingLong(path -> - Long.parseLong(path.getFileName().toString())))
			 .filter(path -> isOldLogs(path,fileNum)).forEach(path -> removeStoreFile(path));
	}

	private void removeStoreFile(Path path){
		try{
			filePaths.remove(path);
			Files.delete(path);
		}catch (Exception e){
			LOGGER.error(String.format("remove store file %s failed",path),e);
		}
	}

	private boolean isOldLogs(Path path,AtomicInteger fileNum){
		if(fileNum.incrementAndGet() > layout.minPersistFileNum){
			try{
				long modifiedTime = Files.getLastModifiedTime(path).toMillis();
				if(System.currentTimeMillis() > layout.minPersistHour * 3600 * 1000 + modifiedTime){
					return true;
				}
			}catch (Exception e){
				return false;
			}
		}
		return false;
	}

	private Path newStoreFile() throws IOException {
		long position = 0;
		if(current != null){
			position = current.writePosition();
		}

		String fileName = String.valueOf(position);
		return storeDirectory.resolve(fileName);
	}

	private void ensureDirectory(Path storeDirectory) throws IOException {
		if(!Files.exists(storeDirectory)){
			Files.createDirectories(storeDirectory);
		}
	}
	private boolean validLogFile(Path path){
		try(RandomAccessFile file = new RandomAccessFile(path.toFile(),"r")) {
			file.seek(0);
			return LogMeta.MAGIC_NUM == file.readLong();
		}catch (IOException e){
			LOGGER.error(String.format("read file %s failed",path),e);
			return false;
		}
	}

	public List<Path> getFilePaths() {
		return filePaths;
	}



	LogStore obtainStore(Path path,boolean readOnly) throws IOException {
		if(logStores.containsKey(path)){
			RefCount<LogStore> store = logStores.get(path);
			store.incRef();
			return store.get();
		}else{
			LogStore store = new LogStore(path,layout.mappedFileSize,readOnly);
			logStores.put(path, new RefCount(store));
			return store;
		}
	}
	void returnStore(LogStore store) throws IOException {
		RefCount<LogStore> storeRef = logStores.get(store.getFilePath());
		int refCount = storeRef.getRefCount();
		if(refCount <= 1){
			storeRef.get().close();
			logStores.remove(store.getFilePath());
		}else{
			storeRef.decRef();
		}
	}

}
