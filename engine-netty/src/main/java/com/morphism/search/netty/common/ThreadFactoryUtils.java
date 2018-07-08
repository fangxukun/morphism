package com.morphism.search.netty.common;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * User: xukun.fyp
 * Date: 15/12/17
 * Time: 20:06
 */
public class ThreadFactoryUtils {

	private static Map<String,AtomicInteger>  sequenceMap = new ConcurrentHashMap<String, AtomicInteger>();

	public static synchronized ThreadFactory newThreadFactory(final String namespace){
		final AtomicInteger sequence;
		if(sequenceMap.containsKey(namespace)){
			sequence = sequenceMap.get(namespace);
		}else{
			sequence = new AtomicInteger(0);
			sequenceMap.put(namespace,sequence);
		}

		return new ThreadFactory() {
			@Override
			public Thread newThread(Runnable r) {
				return new Thread(r,"ThreadPool-" + namespace + "-" + sequence.getAndIncrement());
			}
		};
	}
}
