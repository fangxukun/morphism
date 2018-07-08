package com.vdian.search.field.dump;

import com.koudai.rio.commons.utils.GsonUtils;
import com.vdian.search.field.update.provider.ProviderType;

/**
 * User: xukun.fyp
 * Date: 17/4/11
 * Time: 16:47
 */
public class DumpLayout {
	public final String 			provider;
	public final boolean 			proxy;

	public final String 			remotePath;
	public final String 			delimiter;
	public final String 			localPath;

	public final String 			coreName;
	public final FieldEntry			keyField;
	public final FieldEntry[]		updateFields;

	public DumpLayout(FieldEntry[] updateFields, FieldEntry keyField, String coreName, String localPath, String delimiter, String remotePath, String provider,boolean proxy) {
		this.updateFields = updateFields;
		this.keyField = keyField;
		this.coreName = coreName;
		this.localPath = localPath;
		this.delimiter = delimiter;
		this.remotePath = remotePath;
		this.provider = provider;
		this.proxy = proxy;
	}

	public ProviderType getProviderType(){
		try{
			return ProviderType.valueOf(provider);
		}catch (Exception e){
			if(remotePath != null){
				return ProviderType.HDFS;
			}else{
				return ProviderType.LOCAL;
			}
		}
	}

	public static DumpLayout fromJsonString(String jsonString){
		return GsonUtils.fromString(jsonString,DumpLayout.class);
	}
	public String toJsonString(){
		return GsonUtils.toPrettyString(this);
	}

	public static class FieldEntry{
		public final int 			index;
		public final String 		fieldName;

		public FieldEntry(int index, String fieldName) {
			this.index = index;
			this.fieldName = fieldName;
		}
	}
}
