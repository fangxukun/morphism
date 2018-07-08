package com.morphism.search.sync.session;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

/**
 * User: xukun.fyp
 * Date: 17/5/11
 * Time: 10:10
 */
public class SessionContext {
	private static final Logger					LOGGER		= LoggerFactory.getLogger(SessionContext.class);
	private final AtomicLong					ridSeed;
	private final Map<Long,CountDownLatch>		sessions;
	private final Map<Long,Tracker>				results;

	public SessionContext(){
		ridSeed = new AtomicLong(0);
		sessions = new ConcurrentHashMap<>();
		results = new ConcurrentHashMap<>();
	}

	public long allocateRid(Tracker tracker){
		long requestId = ridSeed.incrementAndGet();
		tracker.setRid(requestId);
		sessions.put(requestId,new CountDownLatch(1));
		return requestId;
	}

	public void complete(Tracker tracker){
		long responseId = tracker.getRid();
		if(sessions.containsKey(responseId)){
			results.put(responseId,tracker);
			CountDownLatch latch = sessions.get(responseId);
			latch.countDown();
		}else{
			LOGGER.error("request is timeout or canceled! id:" + responseId);
		}

	}

	public <T extends Tracker> T waitComplete(long rid) throws ExecutionException, InterruptedException, TimeoutException {
		Future<Tracker> future = future(rid);
		return (T)future.get();
	}

	public <T extends Tracker> T waitComplete(long rid,long timeout,TimeUnit unit) throws ExecutionException, InterruptedException, TimeoutException {
		Future<Tracker> future = future(rid);
		return (T)future.get(timeout,unit);
	}

	public Future<Tracker> future(long rid){
		return new Future<Tracker>() {
			AtomicBoolean cancel = new AtomicBoolean(false);
			@Override
			public boolean cancel(boolean mayInterruptIfRunning) {
				cancel.set(true);
				return true;
			}

			@Override
			public boolean isCancelled() {
				return cancel.get();
			}

			@Override
			public boolean isDone() {
				return sessions.get(rid).getCount() == 0;
			}

			@Override
			public Tracker get() throws InterruptedException, ExecutionException {
				try{
					CountDownLatch latch = sessions.get(rid);
					if(latch != null){
						latch.await();
					}
					return results.get(rid);
				}finally {
					sessions.remove(rid);
					results.remove(rid);
				}
			}

			@Override
			public Tracker get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
				try{
					CountDownLatch latch = sessions.get(rid);
					if(latch != null){
						latch.await(timeout,unit);
					}
					return results.get(rid);
				}finally {
					sessions.remove(rid);
					results.remove(rid);
				}
			}
		};
	}
}
