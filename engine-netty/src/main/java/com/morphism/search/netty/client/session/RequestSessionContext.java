package com.morphism.search.netty.client.session;

import com.morphism.search.netty.common.Callback;
import com.morphism.search.netty.common.RequestContext;
import com.morphism.search.netty.protocol.SolrProtocol;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicLong;

/**
 * User: xukun.fyp
 * Date: 16/11/3
 * Time: 18:17
 * 请求Session管理器，异步发出的请求返回后能够准确接受.
 */
public class RequestSessionContext {
	private static final Logger 					LOGGER = LoggerFactory.getLogger(RequestSessionContext.class);
	private final AtomicLong 						ridSeed;
	private final Map<Long, RequestSession> 		sessions;

	public RequestSessionContext() {
		ridSeed = new AtomicLong(0);
		sessions = new ConcurrentHashMap<>();
	}


	public long allocateRid(RequestContext... contexts) {
		long requestId = ridSeed.incrementAndGet();
		RequestSession session = new RequestSession();
		for (int i = 0; i < contexts.length; i++) {
			contexts[i].setSession(requestId, i);
		}

		session.init(contexts.length,contexts);
		sessions.put(requestId, session);
		return requestId;
	}

	public void release(long rid) {
		sessions.remove(rid);
	}

	public void complete(SolrProtocol.NettyResponse nettyResponse) {
		long rid = nettyResponse.getRid();
		RequestSession session = sessions.get(rid);

		if (session != null) {
			session.completeRpc(nettyResponse);
		} else {
			LOGGER.warn("session rid:" + rid + "is removed ;time:" + System.currentTimeMillis());
		}
	}

	public Future<Boolean> batchFuture(final long rid) {
		return sessions.get(rid).batchFuture(new Callback() {
			@Override
			public void call() {
				release(rid);
			}
		});
	}

	public Future<Boolean> future(final long rid) {
		return sessions.get(rid).future(new Callback() {
			@Override
			public void call() {
				release(rid);
			}
		});
	}

}
