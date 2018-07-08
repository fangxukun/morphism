package com.vdian.search.dlog.test;

import com.vdian.search.dlog.store.LogRecord;
import com.vdian.search.dlog.store.LogStreamImpl;
import com.vdian.search.dlog.store.StoreDirectory;
import com.vdian.search.dlog.store.StoreLayout;
import org.apache.commons.lang.math.RandomUtils;
import org.junit.Test;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Random;
import java.util.concurrent.atomic.AtomicLong;

/**
 * User: xukun.fyp
 * Date: 17/6/1
 * Time: 16:53
 */
public class LogStoreTest {
	private AtomicLong		sequence		=	new AtomicLong(50000);

	@Test
	public void writeStore() throws IOException, InterruptedException {
		StoreLayout layout = new StoreLayout(10 * 1024 * 1024,1,5);

		Path storePath = Paths.get("/Users/fangxukun/search-engine/store");
		StoreDirectory directory = new StoreDirectory(storePath,layout);


		Thread wt1 = new Thread(new Runnable() {
			@Override
			public void run() {
				writeStore(directory,1);
			}
		});
		Thread wt2 = new Thread(new Runnable() {
			@Override
			public void run() {
				writeStore(directory,2);
			}
		});
		Thread rt = new Thread(new Runnable() {
			@Override
			public void run() {
				readStore(directory);
			}
		});

//		wt1.start();
//		wt2.start();
//		Thread.sleep(15 * 1000);
		rt.start();
//		wt1.join();
		rt.join();
	}

	private void writeStore(StoreDirectory directory,int threadNum) {
		while(true){
			try{
				LogRecord record = newRecord();
				directory.write(record);
				if(sequence.get() % 100 == 0){
					System.out.println(threadNum + ". write " + record.getTimestamp());
				}
				Thread.sleep(10);
			}catch (Exception e){
				e.printStackTrace();
			}
		}
	}
	private void readStore(StoreDirectory directory) {
		try{
			LogStreamImpl stream = new LogStreamImpl(directory,61000);
			while(true){
				LogRecord record = stream.take();
				if(record.getTimestamp() % 1000 == 0){
					System.out.println("1. read " + record.getTimestamp());
				}
			}
		}catch (Exception e){
			e.printStackTrace();
		}
	}


	private LogRecord newRecord(){
		ByteBuffer buffer = ByteBuffer.allocate(4096 + RandomUtils.nextInt(1000));
		new Random().nextBytes(buffer.array());
		buffer.position(buffer.capacity());
		return new LogRecord(sequence.incrementAndGet(),buffer);
	}


	@Test
	public void printTime() throws ParseException {
		SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd HH:mm:ss");
		System.out.println(format.parse("20170604 23:00:00").getTime());
	}

}
