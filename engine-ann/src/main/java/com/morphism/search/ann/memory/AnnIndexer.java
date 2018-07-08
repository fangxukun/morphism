package com.morphism.search.ann.memory;

import com.morphism.search.ann.AnnLayout;
import com.morphism.search.ann.Item;
import com.morphism.search.ann.Node;
import com.morphism.search.ann.PriorityElement;
import com.morphism.search.ann.algorithm.FloatCalculator;
import gnu.trove.set.TLongSet;
import gnu.trove.set.hash.TLongHashSet;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.PriorityQueue;

/**
 * User: xukun.fyp
 * Date: 17/3/14
 * Time: 12:48
 */
public class AnnIndexer {
	private final AnnLayout layout;
	private final List<Item>	items;
	private final List<Node>	roots;
	private int 				count;

	public AnnIndexer(AnnLayout layout){
		this.layout = layout;
		this.items = new ArrayList<>();
		this.roots = new ArrayList<>();
	}


	public void addItem(float[] vector){
		items.add(new Item(count++,vector));
	}

	public void buildIndex(){
		for(int i=0;i<layout.numOfTree;i++){
			roots.add(FloatCalculator.makeTree(items,layout));
		}
	}

	public PriorityElement[] search(float[] queryVector, int returnNum, int searchNum){
		TLongSet searchIds = new TLongHashSet(searchNum);
		PriorityQueue<PriorityElement<Node>> pq = new PriorityQueue(searchNum, new Comparator<PriorityElement<Node>>() {
			@Override
			public int compare(PriorityElement<Node> o1, PriorityElement<Node> o2) {
				return o2.compareTo(o1);
			}
		});

		for(Node root: roots){
			pq.add(new PriorityElement(Integer.MAX_VALUE,root));
		}

		// 通过ANN的索引结构，初选出指定数量的结果
		while(searchIds.size() < searchNum && !pq.isEmpty()){
			PriorityElement<Node> pn = pq.poll();
			if(pn.node.docIds != null){
				searchIds.addAll(pn.node.docIds);
			}else{
				float margin = FloatCalculator.margin(pn.node.vector,pn.node.planeOffset, queryVector);
				pq.add(new PriorityElement(Math.min(pn.priority, +margin),pn.node.right));
				pq.add(new PriorityElement(Math.min(pn.priority, -margin),pn.node.left));
			}
		}

		// 对初选的结果计算距离并排序
		PriorityQueue<PriorityElement<Item>> itemQueue = new PriorityQueue();

		for(long id : searchIds.toArray()){
			float distance = FloatCalculator.distance(items.get((int)id).getVector(),queryVector);
			itemQueue.add(new PriorityElement(distance,items.get((int)id)));
		}
		PriorityElement[] result = new PriorityElement[Math.min(returnNum,itemQueue.size())];
		for(int i=0;i<result.length;i++){
			result[i] = itemQueue.poll();
		}
		return result;
	}


}
