package com.vdian.engine.server.engine.full;

/**
 * User: xukun.fyp
 * Date: 17/5/26
 * Time: 09:48
 * 远程索引地址
 */
public class RemoteLocations {
	public static final int 	HDFS_TYPE			=	1;
	public static final int		SERVER_TYPE			=	2;

	public static RemoteLocation newHDFSLocation(String remotePath){
		return new RemoteLocation(HDFS_TYPE,remotePath);
	}

	public static RemoteLocation newServerLocation(String serverIp,String remotePath){
		return new RemoteLocation(SERVER_TYPE,remotePath,serverIp);
	}

	public static class RemoteLocation{
		public final int			remoteType;
		public final String			remotePath;
		public final String 		serverIp;

		public RemoteLocation(int remoteType, String remotePath) {
			this.remoteType = remoteType;
			this.remotePath = remotePath;
			this.serverIp = null;
		}

		public RemoteLocation(int remoteType, String remotePath, String serverIp) {
			this.remoteType = remoteType;
			this.remotePath = remotePath;
			this.serverIp = serverIp;
		}
	}
}
