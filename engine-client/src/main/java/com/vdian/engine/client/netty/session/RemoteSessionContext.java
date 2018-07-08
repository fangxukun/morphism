//package com.vdian.engine.client.netty.session;
//
//import com.vdian.engine.client.netty.RequestContext;
//import com.vdian.engine.client.netty.common.Callback;
//import com.vdian.engine.client.netty.io.NettyResponse;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//
//import java.util.Map;
//import java.util.concurrent.ConcurrentHashMap;
//import java.util.concurrent.Future;
//import java.util.concurrent.atomic.AtomicLong;
//
///**
// * User: xukun.fyp
// * Date: 16/11/3
// * Time: 18:17
// */
//public class RemoteSessionContext {
//	private static final Logger 				LOGGER = LoggerFactory.getLogger(RemoteSessionContext.class);
//	private final AtomicLong 					ridSeed;
//	private final Map<Long, RemoteSession> 		sessions;
//
//	public RemoteSessionContext() {
//		ridSeed = new AtomicLong(0);
//		sessions = new ConcurrentHashMap<>();
//	}
//
//
//	public long allocateRid(RequestContext... requestContexts) {
//		long rid = ridSeed.incrementAndGet();
//		RemoteSession session = new RemoteSession();
//		for (int i = 0; i < requestContexts.length; i++) {
//			requestContexts[i].setRid(rid, i);
//		}
//
//		session.init(requestContexts.length, requestContexts);
//		sessions.put(rid, session);
//		return rid;
//	}
//
//	public void release(long rid) {
//		sessions.remove(rid);
//	}
//
//	public void complete(NettyResponse nettyResponse) {
//		long rid = nettyResponse.getRid();
//		RemoteSession session = sessions.get(rid);
//
//		if (session != null) {
//			session.completeRpc(nettyResponse);
//		} else {
//			LOGGER.warn("session rid:" + rid + "is removed ;time:" + System.currentTimeMillis());
//		}
//	}
//
//	public Future<Boolean> batchFuture(final long rid) {
//		return sessions.get(rid).batchFuture(new Callback() {
//			@Override
//			public void call() {
//				release(rid);
//			}
//		});
//	}
//
//	public Future<Boolean> future(final long rid) {
//		return sessions.get(rid).future(new Callback() {
//			@Override
//			public void call() {
//				release(rid);
//			}
//		});
//	}
//
//}
