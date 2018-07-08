package com.vdian.search.field.update.provider;

import com.vdian.search.field.update.UpdateContext;
import com.vdian.search.field.update.UpdateException;
import com.vdian.search.field.update.provider.hdfs.HDFSTextDataProvider;
import com.vdian.search.field.update.provider.text.TextDataProvider;

/**
 * User: xukun.fyp
 * Date: 17/4/14
 * Time: 14:14
 */
public class DataProviders {
	public static DataProvider newProvider(UpdateContext context) throws UpdateException {
		ProviderType type = context.layout.getProviderType();

		switch (type){
			case HDFS:
				return new HDFSTextDataProvider(context);
			case LOCAL:
				return new TextDataProvider(context);
			default:
				throw new UpdateException("unrecognized data provider " + type);
		}
	}
}
