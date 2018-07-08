package com.vdian.engine.server;

import com.google.common.base.Charsets;
import com.vdian.ergate.meta.common.curator.CuratorAccessApi;
import com.vdian.search.commons.EngineStatus;
import com.vdian.search.commons.NetworkUtils;
import com.vdian.search.coord.curator.Paths;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * User: xukun.fyp
 * Date: 17/4/28
 * Time: 16:27
 */
public class EngineContext {
	private static final Logger			LOGGER		= LoggerFactory.getLogger(EngineContext.class);
	private final CuratorAccessApi 		api;

	public EngineContext(CuratorAccessApi api){
		this.api = api;
	}

	public void reportEngineStatus(EngineStatus engineStatus){
		try{
			if(api != null){
				String path = Paths.get(Paths.ENGINE_STATUS_PATH, NetworkUtils.localIp());
				this.api.addPath(path,bytes(engineStatus));
			}
		}catch (Exception e){
			LOGGER.error("report engine status failed,engineStatus:" + engineStatus,e);
		}
	}


	private byte[] bytes(EngineStatus status){
		return status.toString().getBytes(Charsets.UTF_8);
	}
}
