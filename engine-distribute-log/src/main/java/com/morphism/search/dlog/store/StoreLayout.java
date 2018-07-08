package com.morphism.search.dlog.store;

import com.koudai.rio.commons.properties.JsonProperties;

import java.util.Calendar;
import java.util.Date;

/**
 * User: xukun.fyp
 * Date: 17/5/27
 * Time: 14:30
 */
public class StoreLayout {
	public final int 			mappedFileSize;
	public final int 			minPersistHour;
	public final int 			minPersistFileNum;

	public StoreLayout(int mappedFileSize, int minPersistMin, int minPersistFileNum) {
		this.mappedFileSize = mappedFileSize;
		this.minPersistHour = minPersistMin;
		this.minPersistFileNum = minPersistFileNum;
	}

	public StoreLayout(JsonProperties root) {
		this.mappedFileSize = root.getInteger("mapped.file.size",1024 * 1024 * 50);		//默认50M大小
		this.minPersistHour = root.getInteger("min.persist.hour",48);					    //最少保留最近48小时的日志
		this.minPersistFileNum = root.getInteger("min.persist.file.num",3);				//最少保留最近3个文件
	}
}
