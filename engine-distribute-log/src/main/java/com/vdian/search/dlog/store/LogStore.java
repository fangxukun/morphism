package com.vdian.search.dlog.store;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

/**
 * User: xukun.fyp
 * Date: 17/5/27
 * Time: 16:23
 */
public class LogStore{
	public static final int 		END_OF_FILE			=	-1;
	public static final int 		CLOSED_FILE			=	-2;
	public static final int 		CATCH_UP			=	-3;

	private final MMapFile			mapFile;
	private final ByteBuffer		dataBuffer;
	private final ReentrantLock		writeLock;

	private final Path				filePath;
	private final long 				basePosition;
	public final LogMeta 			meta;

	private volatile boolean 		close;
	private volatile int 			readablePosition;
	private final Condition			inCondition;
	private final LogRecord			record;


	public LogStore(Path filePath,int fileSize,boolean readOnly) throws IOException{
		this.filePath = filePath;
		if(Files.exists(filePath)){
			this.mapFile = new MMapFile(filePath,(int)Files.size(filePath),readOnly);
			this.meta = new LogMeta(metaBuffer(),(int)Files.size(filePath));
		}else{
			this.mapFile = new MMapFile(filePath,fileSize,readOnly);
			this.meta = new LogMeta(metaBuffer(),fileSize);
		}

		this.basePosition = Long.parseLong(filePath.getFileName().toString());
		this.readablePosition = this.meta.dataEndPos.get();
		this.dataBuffer = dataBuffer();
		this.writeLock = new ReentrantLock();
		this.close = false;

		this.inCondition = writeLock.newCondition();

		this.record = new LogRecord();
	}

	//写入时需要进行加锁处理
	public void write(LogRecord record){
		writeLock.lock();
		try{
			int writeLength = record.bytesSize();
			int writePosition = dataBuffer.position();
			record.writeTo(dataBuffer);
			this.readablePosition = dataBuffer.position();
			meta.write(record,writePosition);
			mapFile.flushIfNeeded(writeLength);
			inCondition.signalAll();
		}finally {
			writeLock.unlock();
		}
	}

	public boolean ensureWriteCapacity(LogRecord record){
		int writeLength = record.bytesSize();
		if(dataBuffer.position() + writeLength > dataBuffer.limit()){
			return false;
		}
		return true;
	}

	public long writePosition(){
		return this.meta.dataEndPos.get() + basePosition;
	}


	public long seek(long timestamp){
		int dataSlotPosition = meta.seek(timestamp);
		if(dataSlotPosition == -1){
			return -1;
		}
		long dataPosition = dataSlotPosition + basePosition;

		LogRecord record = new LogRecord();
		while(true){
			long result = poll(dataPosition,record);
			if(result < 0){
				return -1;
			}

			if(record.getTimestamp() >= timestamp){
				break;
			}
			dataPosition = result;
		}
		return dataPosition;
	}


	public long poll(long globalPosition,LogRecord record) {
		int position = (int)(globalPosition - basePosition);

		if(close){
			return CLOSED_FILE;
		}
		while(position >= readablePosition){
			if(isFull()){
				return END_OF_FILE;
			}else {
				return CATCH_UP;
			}
		}

		ByteBuffer readBuffer = dataBuffer.duplicate();
		readBuffer.position(position);
		return record.readFrom(readBuffer) + basePosition;
	}
	public long take(long globalPosition,LogRecord record) throws InterruptedException {
		int position = (int)(globalPosition - basePosition);

		if(close){
			return CLOSED_FILE;
		}
		writeLock.lock();
		try{
			while(position >= readablePosition){
				if(isFull()){
					return END_OF_FILE;
				}else {
					inCondition.await();
				}
			}

			ByteBuffer readBuffer = dataBuffer.duplicate();
			readBuffer.position(position);
			return record.readFrom(readBuffer) + basePosition;
		}finally {
			writeLock.unlock();
		}
	}

	public void flush(){
		this.mapFile.flush();
	}

	public void close() throws IOException {
		if(close == false){
			this.close = true;
			this.flush();
			this.mapFile.close();
		}
	}

	private ByteBuffer metaBuffer(){
		ByteBuffer buffer = this.mapFile.duplicateBuffer();
		buffer.position(0);
		buffer.limit(LogMeta.byteUsed());
		return buffer.slice();
	}

	private ByteBuffer dataBuffer(){
		ByteBuffer buffer = this.mapFile.duplicateBuffer();
		buffer.position(LogMeta.byteUsed());
		buffer.limit(buffer.capacity());

		ByteBuffer dataBuffer = buffer.slice();
		dataBuffer.position(meta.dataEndPos.get());

		return dataBuffer;
	}

	public void markFull(){
		writeLock.lock();
		try{
			this.meta.markFull();
			inCondition.signalAll();
		}finally {
			writeLock.unlock();
		}
	}
	public boolean isFull(){
		return this.meta.isFull();
	}

	public Path getFilePath() {
		return filePath;
	}

}
