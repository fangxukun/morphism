package com.morphism.search.ann.lucene;

import com.morphism.search.ann.BytesUtils;
import com.morphism.search.ann.Item;
import com.morphism.search.ann.ScaleUtils;
import org.apache.lucene.util.BytesRef;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * User: xukun.fyp
 * Date: 17/3/17
 * Time: 15:47
 */
public class ValuesAnalyzer {
	public final Iterable<BytesRef>			values;
	public final List<Item>					items;
	public float[]							minArray;
	public float[]							maxArray;
	public float[]							intervalArray;

	public float							min;
	public float							max;

	public int 								vectorLength;
	public int 								count;



	public ValuesAnalyzer(Iterable<BytesRef> values){
		this.values = values;
		this.items = new ArrayList<>();
	}


	public void analysis(){
		for(BytesRef ref : values){
			float[] floatsVal = BytesUtils.floatsFromBytes(ref.bytes);

			if(vectorLength == 0){
				vectorLength = floatsVal.length;
			}

			if(minArray == null){
				minArray = Arrays.copyOf(floatsVal,floatsVal.length);
			}
			if(maxArray == null){
				maxArray = Arrays.copyOf(floatsVal,floatsVal.length);
			}

			for(int i=0;i<floatsVal.length;i++){
				minArray[i] = Math.min(minArray[i],floatsVal[i]);
				maxArray[i] = Math.max(maxArray[i], floatsVal[i]);
			}
			items.add(new Item(count,floatsVal));
			count++;
		}


		min = ScaleUtils.min(minArray);
		max = ScaleUtils.max(maxArray);

		intervalArray = new float[minArray.length];
		for(int i=0;i<intervalArray.length;i++){
			intervalArray[i] = maxArray[i] - minArray[i];
		}
	}


	public Item getItem(int i){
		return items.get(i);
	}
}
