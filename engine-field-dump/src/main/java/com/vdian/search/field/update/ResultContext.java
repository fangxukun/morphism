package com.vdian.search.field.update;

import com.google.common.base.Stopwatch;
import com.koudai.rio.commons.utils.GsonUtils;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

/**
 * User: xukun.fyp
 * Date: 17/4/18
 * Time: 11:53
 */
public class ResultContext {
	public final AtomicLong 				success;
	public final AtomicLong					failed;
	public final int						tolerance;
	public String 							errorInfo;

	public long								pullCost;
	public long 							updateCost;
	public long 							updateQps;

	public transient final Stopwatch		stopwatch;

	public ResultContext(int tolerance){
		this.success = new AtomicLong(0);
		this.failed = new AtomicLong(0);
		this.tolerance = tolerance;
		this.stopwatch = Stopwatch.createStarted();
	}

	public void markPullComplete(){
		pullCost = stopwatch.elapsed(TimeUnit.SECONDS);
		stopwatch.reset().start();
	}

	public void markUpdateComplete(){
		updateCost = stopwatch.elapsed(TimeUnit.SECONDS);
		updateQps = success.get() / updateCost;

		stopwatch.reset();
	}

	public void reportSuccess(){
		success.incrementAndGet();
	}

	public void reportFailed(Throwable r) throws UpdateException {
		failed.incrementAndGet();

		if(failed.get() >= tolerance){
			this.errorInfo = r.getMessage();
			throw new UpdateException(String.format("tolerance failed,failed:%s,tolerance:%s",failed.get(),success.get()));
		}
	}

	public String toJson(){
		return GsonUtils.toPrettyString(this);
	}

	public ResultContext fromJson(String jsonVal){
		return GsonUtils.fromString(jsonVal,ResultContext.class);
	}

	public void interrupted(){
		errorInfo = "thread is interrupted";
	}
}
