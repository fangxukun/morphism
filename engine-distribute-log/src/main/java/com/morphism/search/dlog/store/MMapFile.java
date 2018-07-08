package com.morphism.search.dlog.store;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Closeable;
import java.io.IOException;
import java.lang.reflect.Method;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * User: xukun.fyp
 * Date: 17/5/27
 * Time: 11:00
 * 通过MappedFile 的方式管理 写入与读取数据的逻辑。
 */
public class MMapFile implements Closeable{
	private static final Logger			LOGGER			= LoggerFactory.getLogger(MMapFile.class);
	private final FileChannel			channel;
	private final MappedByteBuffer		mappedByteBuffer;

	private final Path					filePath;
	private final AtomicInteger			memoryBytes;

	private final AtomicBoolean			flushFlag;



	public MMapFile(Path filePath, int fileSize,boolean readOnly) throws IOException {
		ensurePath(filePath);
		this.filePath = filePath;
		if(!Files.exists(filePath)){
			Files.createFile(filePath);
		}

		if(readOnly){
			this.channel = FileChannel.open(filePath, StandardOpenOption.READ);
			this.mappedByteBuffer = this.channel.map(FileChannel.MapMode.READ_ONLY, 0, fileSize);
		}else{
			this.channel = FileChannel.open(filePath, StandardOpenOption.READ, StandardOpenOption.WRITE);
			this.mappedByteBuffer = this.channel.map(FileChannel.MapMode.READ_WRITE, 0, fileSize);
		}

		this.memoryBytes = new AtomicInteger(0);
		this.flushFlag = new AtomicBoolean(false);
		LOGGER.warn("[MMapFile]init map file:" + filePath);
	}


	/**
	 * flush数据到磁盘
	 */
	public void flush(){
		if(flushFlag.compareAndSet(false,true)){
			return;
		}

		try{
			this.mappedByteBuffer.force();
		}finally {
			flushFlag.set(false);
			memoryBytes.set(0);
		}
	}

	public void flushIfNeeded(int writeLength){
		memoryBytes.addAndGet(writeLength);
		if(memoryBytes.get() > 4096 * 10){
			flush();
		}
	}

	private void ensurePath(Path filePath) throws IOException {
		Path parent = filePath.getParent();

		if(!Files.exists(parent)){
			Files.createDirectories(parent);
		}

		if(!Files.isDirectory(parent)){
			throw new RuntimeException(String.format("path %s's parent path %s is not directory",filePath,parent));
		}
	}

	public ByteBuffer duplicateBuffer(){
		return mappedByteBuffer.duplicate();
	}

	@Override
	public void close() throws IOException {
		flush();
		channel.close();
		invoke(invoke(viewed(mappedByteBuffer), "cleaner"), "clean");
		LOGGER.warn("MMapFile {} closed!",filePath);
	}

	private static ByteBuffer viewed(ByteBuffer buffer) {
		String methodName = "viewedBuffer";

		Method[] methods = buffer.getClass().getMethods();
		for (int i = 0; i < methods.length; i++) {
			if (methods[i].getName().equals("attachment")) {
				methodName = "attachment";
				break;
			}
		}

		ByteBuffer viewedBuffer = (ByteBuffer) invoke(buffer, methodName);
		if (viewedBuffer == null)
			return buffer;
		else
			return viewed(viewedBuffer);
	}

	private static Object invoke(final Object target, final String methodName, final Class<?>... args) {
		return AccessController.doPrivileged(new PrivilegedAction<Object>() {
			public Object run() {
				try {
					Method method = method(target, methodName, args);
					method.setAccessible(true);
					return method.invoke(target);
				} catch (Exception e) {
					throw new IllegalStateException(e);
				}
			}
		});
	}

	private static Method method(Object target, String methodName, Class<?>[] args)
			throws NoSuchMethodException {
		try {
			return target.getClass().getMethod(methodName, args);
		} catch (NoSuchMethodException e) {
			return target.getClass().getDeclaredMethod(methodName, args);
		}
	}
}
