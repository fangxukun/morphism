//package com.vdian.engine.client.netty.session;
//
//import com.google.common.base.Preconditions;
//import com.vdian.engine.client.netty.RequestContext;
//import com.vdian.engine.client.netty.common.Callback;
//import com.vdian.engine.client.netty.io.NettyResponse;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//
//import java.util.Map;
//import java.util.concurrent.*;
//import java.util.concurrent.atomic.AtomicBoolean;
//import java.util.concurrent.atomic.AtomicInteger;
//
///**
// * User: xukun.fyp
// * Date: 16/11/4
// * Time: 11:55
// */
//public class RemoteSession {
//	private final static Logger						LOGGER		= LoggerFactory.getLogger(RemoteSession.class);
//	private CountDownLatch							latch;
//	private int 									latchCount;
//	private AtomicInteger 							count;
//	private Map<Long,RequestContext>				requestMap;
//
//	public void init(int latch,RequestContext ... requestContexts){
//		this.latch = new CountDownLatch(latch);
//		this.latchCount = latch;
//		this.count = new AtomicInteger(0);
//
//		this.requestMap = new ConcurrentHashMap<>();
//		for(RequestContext rc : requestContexts){
//			requestMap.put(rc.getSrid(),rc);
//		}
//	}
//
//	public void completeRpc(NettyResponse response){
//		try{
//			Long srid = response.getSrid();
//			RequestContext context = requestMap.get(srid);
//			context.completeRpc(response);
//		}catch (Exception e){
//			LOGGER.error("completeRpc failed",e);
//		}finally {
//			this.latch.countDown();
//		}
//
//	}
//
//	public Future<Boolean> future(final Callback releaseCallback){
//		Preconditions.checkArgument(latchCount == 1);
//		return new Future<Boolean>() {
//			AtomicBoolean cancel = new AtomicBoolean(false);
//
//			@Override
//			public boolean cancel(boolean mayInterruptIfRunning) {
//				cancel.set(true);
//				return true;
//			}
//
//			@Override
//			public boolean isCancelled() {
//				return cancel.get();
//			}
//
//			@Override
//			public boolean isDone() {
//				return latch.getCount() == 0;
//			}
//
//			@Override
//			public Boolean get() throws InterruptedException, ExecutionException {
//				try{
//					latch.await();
//					return count.get() == 1;
//				}finally {
//					releaseCallback.call();
//				}
//			}
//
//			@Override
//			public Boolean get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
//				try{
//					latch.await(timeout, unit);
//					return count.get() == 1;
//				}finally {
//					releaseCallback.call();
//				}
//			}
//		};
//	}
//
//	public Future<Boolean> batchFuture(final Callback releaseCallback){
//		return new Future<Boolean>() {
//			AtomicBoolean cancel = new AtomicBoolean(false);
//
//			@Override
//			public boolean cancel(boolean mayInterruptIfRunning) {
//				cancel.set(true);
//				return true;
//			}
//
//			@Override
//			public boolean isCancelled() {
//				return cancel.get();
//			}
//
//			@Override
//			public boolean isDone() {
//				return latch.getCount() == 0;
//			}
//
//			@Override
//			public Boolean get() throws InterruptedException, ExecutionException {
//				try{
//					latch.await();
//					return latch.getCount() == 0;
//				}finally {
//					releaseCallback.call();
//				}
//			}
//
//			@Override
//			public Boolean get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException {
//				try{
//					latch.await(timeout, unit);
//					return latch.getCount() == 0;
//				}finally {
//					releaseCallback.call();
//				}
//			}
//		};
//	}
//
//}
