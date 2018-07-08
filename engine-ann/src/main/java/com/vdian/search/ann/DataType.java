package com.vdian.search.ann;

/**
 * User: xukun.fyp
 * Date: 17/3/12
 * Time: 16:44
 */
public enum  DataType {
	BYTE((byte)1),
	SHORT((byte)2),
	FLOAT((byte)3);


	public final byte 	code;

	DataType(byte code){
		this.code = code;
	}


	public static DataType fromCode(byte code){
		for(DataType dt : DataType.values()){
			if(dt.code == code){
				return dt;
			}
		}
		throw new IllegalArgumentException("input data type code illegal:" + code);
	}


}
