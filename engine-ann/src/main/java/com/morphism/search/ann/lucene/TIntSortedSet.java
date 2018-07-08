package com.morphism.search.ann.lucene;

/**
 * User: xukun.fyp
 * Date: 17/3/14
 * Time: 12:08
 */
public class TIntSortedSet {
	private final int[]		array;
	private int 			count;
	private int 			size;

	public TIntSortedSet(int size){
		this.array = new int[size];
		this.size = size;
		this.count = 0;
	}

	public boolean add(int val){
		if(count > size){
			return false;
		}

		putTail(val);
		return true;
	}

	private void putTail(int val){
		array[count] = val;
		upHeap(count);
		count++;
	}


	private void upHeap(int i){
		int parent;
		while( i > 0 ){
			parent = (i-1) >> 1;
			if(array[i] < array[parent]){
				swap(i,parent);
				i = parent;
			}else{
				return;
			}
		}
	}

	private void swap(int i,int j){
		int val = array[i];
		array[i] = array[j];
		array[j] = val;
	}
}
