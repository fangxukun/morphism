package com.morphism.search.ann;

import java.util.List;

/**
 * User: xukun.fyp
 * Date: 17/3/12
 * Time: 17:52
 */
public class Node {
	public final Node				left;
	public final Node				right;
	public final float[]			vector;
	public final float				planeOffset;
	public final long[]				docIds;


	public Node(Node left, Node right, float[] vector, float planeOffset, long[] docIds) {
		this.left = left;
		this.right = right;
		this.vector = vector;
		this.planeOffset = planeOffset;
		this.docIds = docIds;
	}

	public static Node leafNode(List<Item> leafItems){
		long[] docIds = new long[leafItems.size()];
		for(int i=0;i<leafItems.size();i++){
			docIds[i] = leafItems.get(i).getId();
		}

		return new Node(null,null,null,0f,docIds);
	}


	public static Node midNode(Node left,Node right,float[] vector,float planeOffset){
		return new Node(left,right,vector,planeOffset,null);
	}
}
