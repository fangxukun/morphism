package com.vdian.search.coord.curator;

/**
 * User: xukun.fyp
 * Date: 17/4/10
 * Time: 11:29
 */
public class Listeners {

	public static interface NodeChangeListener{
		public void nodeChange(byte[] newData);
	}

	public static interface NodeAddListener{
		public void nodeAdd(String nodePath, byte[] data);
	}

	public static interface NodeRemoveListener{
		public void nodeRemove(String nodePath);
	}

}
