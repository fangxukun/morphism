package com.vdian.search.dlog.store;

import java.nio.ByteBuffer;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * User: xukun.fyp
 * Date: 17/6/1
 * Time: 17:55
 */
public class LogMeta {
	public static final int 			FULL_FLAG_TRUE		=	1;
	public static final int 			FIX_SLOT_NUM		=	100;					//一个简单的timestamp+dataPosition的索引，固定大小写入一个索引
	public static final long			MAGIC_NUM			=	0x4543f839e0a215bL;		//用于校验文件是LogMeta.


	public final AtomicLong				startTimestamp;
	public final AtomicLong				endTimestamp;
	public final AtomicInteger			dataStartPos;
	public final AtomicInteger			dataEndPos;

	private final AtomicLong			numRecord;
	private final AtomicInteger			fullFlag;
	private final AtomicInteger 		numWriteSlots;
	private final Slot[]				slots;
	private final int 					slotSize;

	private final AtomicLong			bytesUsed;
	private final ByteBuffer			buffer;


	public LogMeta(ByteBuffer buffer, int fileSize){
		startTimestamp = new AtomicLong(0);
		endTimestamp = new AtomicLong(0);
		dataStartPos = new AtomicInteger(0);
		dataEndPos = new AtomicInteger(0);

		numRecord = new AtomicLong(0);
		fullFlag = new AtomicInteger(0);
		numWriteSlots = new AtomicInteger(0);
		slots = new Slot[FIX_SLOT_NUM];
		slotSize = fileSize / FIX_SLOT_NUM;

		this.bytesUsed = new AtomicLong(0);
		this.buffer = buffer;

		loadFromBuffer();
	}

	public int seek(long timestamp){
		if(timestamp < startTimestamp.get() || timestamp > endTimestamp.get()){
			return -1;
		}

		int seekPos = -1;
		for(int i=0;i<numWriteSlots.get();i++){
			long slotTimestamp = slots[i].timestamp;
			if(slotTimestamp <= timestamp){
				seekPos = slots[i].dataPosition;
			}else{
				break;
			}
		}
		return seekPos;
	}

	public void markFull(){
		fullFlag.set(FULL_FLAG_TRUE);
		syncToBuffer();
	}
	public boolean isFull(){
		return fullFlag.get() == FULL_FLAG_TRUE;
	}


	public void write(LogRecord record,int writePosition){
		startTimestamp.compareAndSet(0, record.getTimestamp());
		endTimestamp.set(record.getTimestamp());
		dataStartPos.compareAndSet(0, writePosition);
		dataEndPos.set(writePosition + record.bytesSize());

		numRecord.incrementAndGet();
		bytesUsed.addAndGet(record.bytesSize());

		if(bytesUsed.get() >= slotSize || numWriteSlots.get() == 0){
			slots[numWriteSlots.getAndIncrement()] = new Slot(record.getTimestamp(),writePosition);
			bytesUsed.set(0);
		}
		syncToBuffer();
	}

	private void loadFromBuffer(){
		if(buffer.getLong() == MAGIC_NUM){
			startTimestamp.set(buffer.getLong());
			endTimestamp.set(buffer.getLong());
			dataStartPos.set(buffer.getInt());
			dataEndPos.set(buffer.getInt());

			numRecord.set(buffer.getLong());
			fullFlag.set(buffer.getInt());
			numWriteSlots.set(buffer.getInt());
			for(int i=0;i<numWriteSlots.get();i++){
				long timestamp = buffer.getLong();
				int dataPos = buffer.getInt();
				slots[i] = new Slot(timestamp,dataPos);
			}
		}
	}
	private void syncToBuffer(){
		buffer.position(0);
		buffer.putLong(MAGIC_NUM);
		buffer.putLong(startTimestamp.get());
		buffer.putLong(endTimestamp.get());
		buffer.putInt(dataStartPos.get());
		buffer.putInt(dataEndPos.get());

		buffer.putLong(numRecord.get());
		buffer.putInt(fullFlag.get());
		buffer.putInt(numWriteSlots.get());
		for(int i=0;i<numWriteSlots.get();i++){
			buffer.putLong(slots[i].timestamp);
			buffer.putInt(slots[i].dataPosition);
		}
	}

	public class Slot{
		private final long 				timestamp;
		private final int 				dataPosition;

		public Slot(long timestamp, int dataPosition) {
			this.timestamp = timestamp;
			this.dataPosition = dataPosition;
		}
	}

	public static int byteUsed(){
		return 48 + FIX_SLOT_NUM * 12;
	}
}
