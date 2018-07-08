package com.vdian.search.ann;

/**
 * User: xukun.fyp
 * Date: 17/3/2
 * Time: 10:41
 */
public class PriorityElement<T> implements Comparable<PriorityElement>{
	public final float 		priority;
	public final T			node;

	public PriorityElement(float priority, T node) {
		this.priority = priority;
		this.node = node;
	}

	public static <T> PriorityElement<T> of(float priority,T node){
		return new PriorityElement<>(priority,node);
	}

	@Override
	public int compareTo(PriorityElement o) {
		if(priority == o.priority){
			return 0;
		}

		return priority - o.priority > 0 ? 1: -1;
	}
}
