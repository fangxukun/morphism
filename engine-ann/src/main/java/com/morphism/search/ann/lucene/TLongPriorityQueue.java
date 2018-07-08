package com.morphism.search.ann.lucene;

import com.google.common.base.Stopwatch;
import com.google.common.math.IntMath;
import org.apache.commons.lang.math.RandomUtils;

import java.util.concurrent.TimeUnit;

/**
 * User: xukun.fyp
 * Date: 17/3/9
 * Time: 10:08
 */
public class TLongPriorityQueue {
	private int 				count;
	private int					modCount;

	private float[]				keyQueue;
	private long[]				valQueue;

	public TLongPriorityQueue(int initialSize){
		this.count = 0;
		this.modCount = 0;
		this.keyQueue = new float[initialSize];
		this.valQueue = new long[initialSize];

	}

	public boolean add(float priority,long val){
		modCount++;

		growIfNeeded();

		int i = count;
		if(i == 0){
			keyQueue[0] = priority;
			valQueue[0] = val;
			count++;
		}else{
			this.putTail(priority,val);
		}

		return true;
	}

	public long poll(){
		if(count == 0){
			return 0;
		}
		modCount++;
		long result = valQueue[0];

		keyQueue[0] = keyQueue[count-1];
		valQueue[0] = valQueue[count-1];
		this.count--;
		downHeap(0);
		return result;
	}

	public float peekPriority(){
		return keyQueue[0];
	}
	public long peekVal(){
		return valQueue[0];
	}

	public int size(){
		return count;
	}


	private void putTail(float priority,long val){
		keyQueue[count] = priority;
		valQueue[count] = val;
		upHeap(count);
		count++;
	}

	private void putTop(float priority,long val){
		keyQueue[0] = priority;
		valQueue[0] = val;
		downHeap(0);
	}

	private void downHeap(int i){
		while(i < count){
			int j = (i << 1) + 1;

			int max = i;

			if(j < count && keyQueue[j] > keyQueue[i]){
				max = j;
			}
			if(j+1 < count && keyQueue[j+1] > keyQueue[max]){
				max = j + 1;
			}

			if(i != max){
				swap(i,max);
				i = max;
			}else{
				break;
			}
		}
	}

	private void upHeap(int i){
		int j;
		while(i > 0){
			j = (i-1) >> 1;
			if(keyQueue[i] > keyQueue[j]){
				swap(i,j);
			}
			i = j;
		}
	}

	private void swap(int i,int j){
		float key = keyQueue[i];
		long val = valQueue[i];

		keyQueue[i] = keyQueue[j];
		valQueue[i] = valQueue[j];

		keyQueue[j] = key;
		valQueue[j] = val;
	}



	private void growIfNeeded(){
		if(count >= keyQueue.length){
			int newCapacity = calculateNewCapacity();

			float[] newKeyQueue = new float[newCapacity];
			System.arraycopy(keyQueue,0,newKeyQueue,0, keyQueue.length);

			long[] newValQueue = new long[newCapacity];
			System.arraycopy(valQueue,0,newValQueue,0,valQueue.length);

			keyQueue = newKeyQueue;
			valQueue = newValQueue;
		}
	}

	private int calculateNewCapacity() {
		int oldCapacity = keyQueue.length;
		int newCapacity =
				(oldCapacity < 64)
						? (oldCapacity + 1) * 2
						: IntMath.checkedMultiply(oldCapacity / 2, 3);
		return newCapacity;
	}


	public static void main(String[] args){


		Stopwatch sw1 = Stopwatch.createUnstarted();
		Stopwatch sw2 = Stopwatch.createUnstarted();

		for(int k=0;k<10000;k++){
			int size = 1000,topSize=100;
			float[] key = new float[size];
			long[] val = new long[size];
			for(int i=0;i<size;i++){
				key[i] = RandomUtils.nextFloat();
				val[i] = Math.round(key[i] * size * 10);
			}



			sw2.start();
			TLongPriorityQueue q = new TLongPriorityQueue(topSize);
			for(int i=0;i<size;i++){
				q.add(key[i], val[i]);
			}
			for(int i=0;i<topSize;i++){
				q.poll();
			}
			sw2.stop();


		}

		System.out.println("cost-pq:" + sw1.elapsed(TimeUnit.MILLISECONDS));
		System.out.println("cost-t:" + sw2.elapsed(TimeUnit.MILLISECONDS));


	}

}
