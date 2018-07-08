package com.morphism.search.coord.curator.joint;

import com.google.common.base.Charsets;
import com.koudai.rio.commons.utils.GsonUtils;
import com.morphism.search.commons.NetworkUtils;
import org.apache.commons.lang.time.DateFormatUtils;

import java.util.Date;

/**
 * User: xukun.fyp
 * Date: 17/4/10
 * Time: 17:14
 */
public class PathData {
	private String 			sequenceId;
	private String 			ip;
	private PathStatus 		status;
	private String			payload;
	private int 			nextStepBatchCount	=	Integer.MAX_VALUE;		//下一步需要监听并报告成功的数量，达到此数量则将STEP节点标示为成功。


	public static PathData newStartData(){
		PathData data = new PathData();
		data.sequenceId = DateFormatUtils.format(new Date(), "yyyyMMdd-HH:mm:ss.SSS");
		data.ip = NetworkUtils.localIp();
		data.status = PathStatus.SUCCESS;
		return data;
	}

	public static PathData newInstance(PathStatus status,String sequenceId){
		PathData data = new PathData();
		data.sequenceId = sequenceId;
		data.ip = NetworkUtils.localIp();
		data.status = status;
		return data;
	}

	public static PathData newInstance(PathStatus status,String sequenceId,String info){
		PathData data = new PathData();
		data.sequenceId = sequenceId;
		data.ip = NetworkUtils.localIp();
		data.payload = info;
		data.status = status;
		return data;
	}

	public String getSequenceId() {
		return sequenceId;
	}

	public void setSequenceId(String sequenceId) {
		this.sequenceId = sequenceId;
	}

	public boolean isSuccess(){
		return status == PathStatus.SUCCESS;
	}

	public boolean isFailed(){
		return status == PathStatus.FAILED;
	}

	public boolean isStop(){
		return status == PathStatus.STOP;
	}

	public String getIp() {
		return ip;
	}

	public void setIp(String ip) {
		this.ip = ip;
	}

	public PathStatus getStatus() {
		return status;
	}

	public void setStatus(PathStatus status) {
		this.status = status;
	}

	public int getNextStepBatchCount() {
		return nextStepBatchCount;
	}

	public PathData setNextStepBatchCount(int nextStepBatchCount) {
		this.nextStepBatchCount = nextStepBatchCount;
		return this;
	}

	public String getPayload() {
		return payload;
	}

	public PathData setPayload(String payload) {
		this.payload = payload;
		return this;
	}

	public byte[] toBytes(){
		return GsonUtils.toString(this).getBytes(Charsets.UTF_8);
	}

	public static PathData fromBytes(byte[] bytes){
		return GsonUtils.fromString(new String(bytes,Charsets.UTF_8),PathData.class);
	}
}
