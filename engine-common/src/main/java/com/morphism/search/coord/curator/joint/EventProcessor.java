package com.morphism.search.coord.curator.joint;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

/**
 * User: xukun.fyp
 * Date: 17/4/18
 * Time: 15:47
 */
public class EventProcessor {
	private static final Logger				LOGGER		= LoggerFactory.getLogger(EventProcessor.class);
	private final Function<PathData>		function;
	private final Joint						joint;
	private final ExecutorService			service;

	public EventProcessor(Function<PathData> function,Joint joint){
		this.function = function;
		this.joint = joint;

		ThreadFactory tf = new ThreadFactoryBuilder().setNameFormat("joint-event").build();
		this.service = Executors.newFixedThreadPool(2,tf);
	}

	public void onEvent(final PathData data){
		this.service.execute(new Runnable() {
			@Override
			public void run() {
				String sequenceId = data.getSequenceId();
				try {

					switch (data.getStatus()) {
						case SUCCESS:
							if(!function.isRunning()){
								joint.reportLocalStatus(sequenceId, PathStatus.RUNNING, null);
								try {
									String payload = function.call(data);
									joint.reportLocalStatus(sequenceId, PathStatus.SUCCESS, payload);
								} catch (Exception e) {
									joint.reportLocalStatus(sequenceId, PathStatus.FAILED, e.getMessage());
								}
							}else{
								LOGGER.warn("node is running! ignore this event! ipPath:" + joint.localIpPath());
							}
							break;
						case FAILED:
							joint.reportLocalStatus(sequenceId, PathStatus.FAILED, data.getPayload());
							break;
						case STOP:
							if(function.isRunning()){
								function.stop();
								joint.reportLocalStatus(sequenceId, PathStatus.STOP, null);
								joint.reportStepStatus(sequenceId,PathStatus.STOP,null);
								break;
							}
					}
				} catch (Exception e) {
					LOGGER.error(String.format("work failed,path:%,data=%s", joint.path, data), e);
				} finally {
					rewriteStepResult(sequenceId,data.getNextStepBatchCount());
				}
			}
		});
	}

	private void rewriteStepResult(String sequenceId,int batchCount){
		String currentStepPath = joint.stepPath();
		try{
			byte[] value = joint.getData(currentStepPath);

			if(value != null && value.length > 0){
				PathData pathData = PathData.fromBytes(value);
				if(pathData.isStop() && pathData.getSequenceId().equals(sequenceId)){
					return;
				}
			}

			List<PathData> stepChildren = joint.listStepChildren();
			int successCount = 0,failedCount = 0;
			String errorInfo = null;

			for(PathData data : stepChildren){
				if(data.isSuccess()){
					successCount++;
				}else if(data.isFailed()){
					failedCount++;
					errorInfo = String.format("host:%s failed,info:%s", data.getIp(), data.getPayload());
				}
			}

			//0.任何一个节点失败则标示为失败
			if(failedCount > 0){
				joint.reportStepStatus(sequenceId, PathStatus.FAILED, errorInfo + " failedCount:" + failedCount);
			}else{
				//1.成功数量达到指定数量则标示成功
				if(successCount >= batchCount){
					joint.reportStepStatus(sequenceId, PathStatus.SUCCESS, null);
				}

				//2.如果没有设置batchCount,则所有子节点都成功就标示成功
				if(successCount >= stepChildren.size()){
					joint.reportStepStatus(sequenceId, PathStatus.SUCCESS, null);
				}
			}
		}catch (Exception e){
			throw new RuntimeException(String.format("[RewriteStepResult] batchCount:%s,path:%s",batchCount,currentStepPath),e);
		}
	}


}
