package com.vdian.search.coord.curator.joint;

import com.vdian.search.commons.NetworkUtils;
import com.vdian.search.coord.curator.CuratorAccessApi;
import com.vdian.search.coord.curator.Listeners;
import com.vdian.search.coord.curator.Paths;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * User: xukun.fyp
 * Date: 17/4/10
 * Time: 14:53
 * 协同器,完成不同系统组成的一个树形协同体系。不同节点共享path，不同步骤通过step来分割。
 *
 * Path(/joint/field-update):
 * 	--Step1(/joint/field-update/step1)	: success
 * 		-- 10.1.2.3: 发起任务
 * 	--Step2(/joint/field-update/step2)	: success
 * 		-- 10.2.1.1:update success!
 *		-- 10.2.1.2:update success!
 *	--Step3(/joint/field-update/step3)	: success
 *
 *
 * Step1:可以自定义为TriggerFieldUpdate,Step2:clusterUpdate,Step3:UpdateComplete

 * 1.节点可以report每个节点状态Start/Running/Success/Failed四种状态
 * 2.节点可以监听其他节点，上一个节点状态为Success则开始本节点的任务，否则标示为失败。
 *
 *
 * 可能存在的一些业务问题包括：由调用方做此类业务封装。
 * 1. 节点触发时，有机器重启或者没有接受到zk的通知
 * 2. 如何判断某给节点成功，目前这一层判断逻辑是有任何一个节点失败即为失败，成功数量达到指定的数量
 * 3. 重复触发问题。
 */
public class Joint {
	private static final Logger			LOGGER				= LoggerFactory.getLogger(Joint.class);
	private static final String 		DEFAULT_SEQUENCE	=	"defaultSequence";
	private final CuratorAccessApi 		api;

	public final String 				path;
	public final Step 					current;



	public Joint(CuratorAccessApi api, String path, Step currentStep) {
		this.api = api;
		this.path = path;
		this.current = currentStep;
	}

	public void listen(Step step, final Function<PathData> function) throws Exception{
		reportLocalStatus(DEFAULT_SEQUENCE, PathStatus.START, null);
		final String stepPath = Paths.get(path, step.name());
		LOGGER.warn("listen path:{}" + stepPath);

		final EventProcessor processor = new EventProcessor(function,this);
		api.onDataChange(stepPath, new Listeners.NodeChangeListener() {
			@Override
			public void nodeChange(byte[] newData) {
				PathData data = PathData.fromBytes(newData);

				LOGGER.info("node {} event:{}",stepPath,data);
				processor.onEvent(data);
			}
		});
	}

	void reportLocalStatus(String sequenceId,PathStatus status,String info) throws Exception{
		String ipPath = Paths.get(path, current.name(), NetworkUtils.localIp());
		PathData data = null;

		if(info == null){
			data = PathData.newInstance(status,sequenceId);
		}else{
			data = PathData.newInstance(status,sequenceId,info);
		}

		LOGGER.warn("report local status path:{},data:{}",ipPath,data);
		api.addPath(ipPath, data.toBytes());
	}


	public void reportStepStatus(String sequenceId,PathStatus status,String info) throws Exception{
		String stepPath = Paths.get(path, current.name());
		PathData data = null;

		if(info == null){
			data = PathData.newInstance(status,sequenceId);
		}else{
			data = PathData.newInstance(status,sequenceId,info);
		}

		LOGGER.warn("report step status path:{},data:{}",stepPath,data);
		api.addPath(stepPath, data.toBytes());
	}

	public void reportStop() throws Exception{
		String stepPath = Paths.get(path, current.name());
		PathData data = PathData.fromBytes(api.getData(stepPath));

		data.setStatus(PathStatus.STOP);
		LOGGER.warn("report stop path:{},data:{}",stepPath,data);
		api.addPath(stepPath,data.toBytes());

	}
	public void reportStart(String payload) throws Exception {
		String stepPath = Paths.get(path, current.name());
		PathData data = PathData.newStartData();
		data.setPayload(payload);

		LOGGER.warn("report start path:{},data:{}",stepPath,data);
		api.addPath(stepPath, data.toBytes());
	}


	public PathData currentData() throws Exception {
		String stepPath = Paths.get(path, current.name());
		return PathData.fromBytes(api.getData(stepPath));
	}

	public List<PathData> childDataList() throws Exception{
		String stepPath = Paths.get(path, current.name());
		List<String> children = api.listChildren(stepPath);
		List<PathData> result = new ArrayList<>(children.size());

		for(String child : children){
			result.add(PathData.fromBytes(api.getData(child)));
		}
		return result;
	}


	public byte[] getData(String path) throws Exception{
		return api.getData(path);
	}

	public String stepPath(){
		return Paths.get(path, current.name());
	}

	public String localIpPath(){
		return Paths.get(path,current.name(),NetworkUtils.localIp());
	}

	public List<PathData> listStepChildren() throws Exception {
		String stepPath = stepPath();
		List<String> subPaths = api.listChildren(stepPath);
		List<PathData> result = new ArrayList<>();
		for(String path : subPaths) {
			PathData data = PathData.fromBytes(api.getData(Paths.get(stepPath, path)));
			result.add(data);
		}
		return result;
	}

	public boolean exist() throws Exception {
		String stepPath = Paths.get(path, current.name());
		return api.exist(stepPath);
	}
}

