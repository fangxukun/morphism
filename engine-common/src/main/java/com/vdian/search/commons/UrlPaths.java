package com.vdian.search.commons;

import java.net.MalformedURLException;
import java.net.URL;

/**
 * User: xukun.fyp
 * Date: 17/4/28
 * Time: 14:13
 */
public class UrlPaths {
	public static final String 			DELIMITER		=	"/";

	public static String getUrl(String baseUrl,String path){
		if(baseUrl.endsWith(DELIMITER) && path.startsWith(DELIMITER)){
			return baseUrl + path.substring(1);
		}
		if(!baseUrl.endsWith(DELIMITER) && !path.startsWith(DELIMITER)){
			return baseUrl + DELIMITER + path;
		}
		return baseUrl + path;
	}

	public static boolean checkBaseUrl(String baseUrl){
		try{
			new URL(baseUrl);
			return true;
		}catch (MalformedURLException e){
			return false;
		}
	}
}
