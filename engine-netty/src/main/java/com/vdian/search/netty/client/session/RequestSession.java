package com.vdian.search.netty.client.session;

import com.google.common.base.Preconditions;
import com.vdian.search.netty.common.Callback;
import com.vdian.search.netty.common.RequestContext;
import com.vdian.search.netty.protocol.SolrProtocol;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * User: xukun.fyp
 * Date: 16/11/4
 * Time: 11:55
 *
 */
public class RequestSession {
	private final static Logger							LOGGER		= LoggerFactory.getLogger(RequestSession.class);
	private CountDownLatch								latch;
	private int 										latchCount;
	private Map<Long,RequestContext> 					requests;

	public void init(int latch,RequestContext... contexts){
		this.latch = new CountDownLatch(latch);
		this.latchCount = latch;

		this.requests = new ConcurrentHashMap(contexts.length);
		for(RequestContext context : contexts){
			requests.put(context.getCid(),context);
		}
	}

	public void completeRpc(SolrProtocol.NettyResponse response){
		try{
			long connectId = response.getCid();
			RequestContext context = requests.get(connectId);
			context.completeRpc(response);
		}catch (Exception e){
			LOGGER.error("completeRpc failed",e);
		}finally {
			this.latch.countDown();
		}

	}

	public Future<Boolean> future(final Callback releaseCallback){
		Preconditions.checkArgument(latchCount == 1);
		return new Future<Boolean>() {
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
				return latch.getCount() == 0;
			}

			@Override
			public Boolean get() throws InterruptedException, ExecutionException {
				try{
					latch.await();
					return latch.getCount() == 0;
				}finally {
					releaseCallback.call();
				}
			}

			@Override
			public Boolean get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
				try{
					latch.await(timeout, unit);
					return latch.getCount() == 0;
				}finally {
					releaseCallback.call();
				}
			}
		};
	}

	public Future<Boolean> batchFuture(final Callback releaseCallback){
		return new Future<Boolean>() {
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
				return latch.getCount() == 0;
			}

			@Override
			public Boolean get() throws InterruptedException, ExecutionException {
				try{
					latch.await();
					return latch.getCount() == 0;
				}finally {
					releaseCallback.call();
				}
			}

			@Override
			public Boolean get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException {
				try{
					latch.await(timeout, unit);
					return latch.getCount() == 0;
				}finally {
					releaseCallback.call();
				}
			}
		};
	}

}
