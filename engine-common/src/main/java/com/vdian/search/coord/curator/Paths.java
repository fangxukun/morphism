package com.vdian.search.coord.curator;

import com.google.common.base.Preconditions;
import org.apache.commons.lang.StringUtils;

/**
 * User: xukun.fyp
 * Date: 17/4/10
 * Time: 11:29
 */
public class Paths {
	public static final String 		DELIMITER			=	"/";

	public static final String 		JOINT_PATH			=	"/joint";

	public static final String 		UPDATE_JOINT_PATH	=	"/joint/field/update";


	public static final String 		ENGINE_STATUS_PATH	=	"/data/engine/status";


	public static String get(String parent,String ... more){
		Preconditions.checkNotNull(parent);
		Preconditions.checkNotNull(more);

		if(parent.endsWith(DELIMITER)){
			return parent + StringUtils.join(more,DELIMITER);
		}else{
			return parent + DELIMITER + StringUtils.join(more,DELIMITER);
		}
	}


	public static String getUpdateJointPath(String serviceName){
		return get(UPDATE_JOINT_PATH,serviceName);
	}
}
