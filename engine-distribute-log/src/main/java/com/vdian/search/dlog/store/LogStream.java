package com.vdian.search.dlog.store;

import java.io.Closeable;
import java.io.IOException;

/**
 * User: xukun.fyp
 * Date: 17/5/31
 * Time: 15:37
 */
public interface LogStream extends Closeable{

	/**
	 * 获取新的Record,如果没有数据则阻塞。
	 * @return
	 */
	LogRecord take() throws IOException, InterruptedException;


}
