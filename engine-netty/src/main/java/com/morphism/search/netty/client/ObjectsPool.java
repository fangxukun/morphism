package com.morphism.search.netty.client;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;

/**
 * User: xukun.fyp
 * Date: 17/4/5
 * Time: 19:40
 */
public class ObjectsPool<T> {
	public static final int 					INITIAL_SIZE		=	128;
	private ArrayBlockingQueue<T>				frees;

	private final int 							maxSize;
	private final Factory<T>					factory;
	private final AtomicInteger					count;

	private final ReentrantLock					lock;

	public ObjectsPool(int initialSize,int maxSize,Factory<T> factory){
		this.factory = factory;
		this.count = new AtomicInteger(0);
		this.maxSize = maxSize;
		this.lock = new ReentrantLock(true);

		this.frees = new ArrayBlockingQueue<T>(initialSize);

		count.set(initialSize);
		for(int i=0;i<initialSize;i++){
			this.frees.add(factory.newInstance());
		}
	}

	/**
	 * 从Pool中提取可用实例
	 * @param timeout
	 * @param unit
	 * @return
	 * @throws InterruptedException
	 */
	public T acquire(long timeout,TimeUnit unit) throws InterruptedException {
		T result = this.frees.poll();
		if(result == null){
			if(count.get() >= maxSize){
				return frees.poll(timeout,unit);
			}else{
				grow();
				return frees.poll(timeout,unit);
			}
		}
		return result;
	}

	/**
	 * 释放instance
	 * @param instance
	 */
	public void release(T instance){
		this.frees.offer(instance);
	}



	private void grow(){
		lock.lock();
		try{
			if(count.get() <= maxSize && frees.size() <= 1){
				int newSize = nextCapacity(count.get() + 1);

				ArrayBlockingQueue<T> newFrees = new ArrayBlockingQueue<T>(newSize);
				for(int i=0;i<newSize;i++){
					newFrees.add(factory.newInstance());
				}

				count.set(newSize);
				this.frees = newFrees;
			}
		}finally {
			lock.unlock();
		}
	}

	private int nextCapacity(int minCapacity){
		int newSize = minCapacity << 2;
		return Math.min(newSize,maxSize);
	}

	public static interface Factory<T>{
		T newInstance();
	}
}
