package com.vdian.search.ann.lucene;

import com.vdian.search.ann.AnnLayout;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * User: xukun.fyp
 * Date: 17/3/18
 * Time: 11:49
 */
public class AnnLayouts {
	public static Map<String,AnnLayout>		caches 	=	new ConcurrentHashMap<>();

	public static AnnLayout getLayout(String fieldName){
		if(caches.containsKey(fieldName)){
			return caches.get(fieldName);
		}else{
			System.err.println("can not find layout for fieldName:" + fieldName);
			throw new RuntimeException(String.format("fieldName:%s AnnLayout not specify!",fieldName));
		}
	}

	public static void register(String fieldName,AnnLayout layout){
		caches.put(fieldName,layout);
	}

	public static boolean notExist(String fieldName){
		if(caches.containsKey(fieldName)){
			return false;
		}
		return true;
	}

	public static void clear(){
		caches.clear();
	}
}
