package com.vdian.search.ann.algorithm;

import com.vdian.search.ann.AnnLayout;
import com.vdian.search.ann.Item;
import com.vdian.search.ann.Node;
import org.apache.commons.lang.math.RandomUtils;
import org.apache.commons.lang3.tuple.Pair;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * User: xukun.fyp
 * Date: 17/3/12
 * Time: 18:18
 */
public class FloatCalculator {


	/**
	 * 通过迭代计算两个均值点(可以认为是通过距离聚类),并得到两个向量之差 以及planeOffset, 通过二维来进行理解推导,可扩展到多维
	 *      ^
	 *      |
	 *      |  * A
	 *      |   .   * X:输入变量
	 *      |    .
	 *      |     * C:(A+B)/2
	 *      |      .
	 *      |       .
	 *      |        * B
	 * --------------------------------------->
	 *      |O
	 *      |
	 *      |
	 *      |
	 *      |
	 *      |
	 * CX * CA > 0 == >在切面上方
	 * CX = X - C
	 * CA = A - C
	 * X = (x1,x2,...xn)
	 * A = (a1,a1,...an)
	 * B = (b1,b2,...bn)
	 * C = (A+B)/2 = ((a1+b1)/2,(a2+b2)/2,...(an+bn)/2)
	 *
	 * margin = CX * CA
	 * = (X-C)(A-C)
	 * = (X-(A+B)/2)(A-(A+B)/2)
	 * = (X-(A+B)/2)(A-B)/2
	 * = 1/2[X * (A-B)     -    (A-B)(A+B)/2]
	 * .=    X * (A-B)     -    (A-B)(A+B)/2
	 *   -------------          -------------
	 *   后续通过输入X计算          node.planeOffset 此方法中计算得到的
	 *
	 *
	 * 对一批数据构建Ann Tree
	 * @param items
	 * @param layout
	 * @return
	 */
	public static Node makeTree(List<Item> items, AnnLayout layout) {

		//1.数量小于叶节点最大数量时，直接存为叶节点，不在下分。
		if (items.size() < layout.leafNodeMaxItem) {
			return Node.leafNode(items);
		}

		//2.计算切分的超平面Node

		//2.1 计算vector / planeOffset
		Pair<float[], float[]> twoMeans = twoMeans(items, layout.meanIterationStep);
		float[] lv = twoMeans.getLeft();
		float[] rv = twoMeans.getRight();
		int d = lv.length;

		float[] vector = new float[d];
		for(int k=0;k<d;k++){
			vector[k] = lv[k] - rv[k];
		}

		float planeOffset = 0f;
		for(int k=0;k<d;k++){
			planeOffset += -vector[k] * ( lv[k] + rv[k])/2;	// -(A-B)(A+B)/2
		}

		//2.2 按照通过超平面将items的所有数据切分成两类
		List<Item> leftItems = new ArrayList<>(items.size() / 2);
		List<Item> rightItems = new ArrayList<>(items.size() / 2);
		for(int i=0;i<items.size();i++){
			Item item = items.get(i);
			float margin = margin(vector,planeOffset,item.getVector());
			if(margin < 0){
				leftItems.add(item);
			}else{
				rightItems.add(item);
			}
		}

		ensureSplit(leftItems,rightItems,items,vector);

		Node left = makeTree(leftItems, layout);
		Node right = makeTree(rightItems,layout);
		return Node.midNode(left,right,vector,planeOffset);
	}


	private static void ensureSplit(List<Item> leftItems,List<Item> rightItems,List<Item> allItems,float[] vector){
		while((Math.abs(leftItems.size() - rightItems.size())) > allItems.size() *98/100){
			System.err.println(String.format("split bias,random split!left:%s,right:%s",leftItems.size(),rightItems.size()));

			Arrays.fill(vector, 0f);
			leftItems.clear();
			rightItems.clear();

			for(Item item : allItems){
				if(RandomUtils.nextBoolean()){
					leftItems.add(item);
				}else{
					rightItems.add(item);
				}
			}
		}
	}


	/**
	 * 计算 X*(A-B) + planeOffset((A-B)(A+B)/2)
	 * @param vector
	 * @param planeOffset
	 * @param itemVector
	 * @return
	 */
	public static float margin(float[] vector,float planeOffset, float[] itemVector){
		float margin = planeOffset;
		for(int k=0;k<vector.length;k++){
			margin += vector[k] * itemVector[k];
		}
		return margin;
	}


	/**
	 * 对给定的一组向量，随机选取两个向量并不断迭代计算均值点。
	 *
	 * @param items
	 * @param iterationStep
	 * @return
	 */
	public static Pair<float[], float[]> twoMeans(List<Item> items, int iterationStep) {
		int count = items.size();
		int lIdx = RandomUtils.nextInt(count);                //随机选择两个点
		int rIdx = RandomUtils.nextInt(count - 1);

		while (lIdx == rIdx) {
			rIdx = RandomUtils.nextInt(count - 1);            //确保左右两个不相同
		}

		float[] lv = new float[items.get(lIdx).getVector().length];
		float[] rv = new float[items.get(rIdx).getVector().length];
		System.arraycopy(items.get(lIdx).getVector(), 0, lv, 0, items.get(lIdx).getVector().length);
		System.arraycopy(items.get(rIdx).getVector(), 0, rv, 0, items.get(rIdx).getVector().length);

		int lc = 1, rc = 1;
		int step = Math.min(iterationStep, items.size());

		for (int i = 0; i < step; i++) {
			int idx = RandomUtils.nextInt(count);
			float[] randomV = items.get(idx).getVector();        		//随机抽取的Item

			float dLeft = lc * distance(lv, randomV);                //计算与两个中心点的距离，之所以乘以lc/rc是为了让两边数量尽可能均等，是否会照成准确度降低? TODO:
			float dRight = rc * distance(rv, randomV);

			if (dLeft < dRight) {
				for (int k = 0; k < lv.length; k++) {
					lv[k] = (lv[k] * lc + randomV[k]) / (lc + 1);    //更新Left中心点向量
				}
				lc++;
			} else if (dLeft > dRight) {
				for (int k = 0; k < lv.length; k++) {
					rv[k] = (rv[k] * rc + randomV[k]) / (rc + 1);    //更新Right中心点向量
				}
				rc++;
			}
		}
		return Pair.of(lv, rv);
	}


	/**
	 * 直接计算距离
	 *
	 * @param v1
	 * @param v2
	 * @return
	 */
	public static float distance(float[] v1, float[] v2) {
		return (float) Math.sqrt(distanceSquare(v1, v2));
	}


	/**
	 * 计算两个向量之间的距离，返回欧几里得距离
	 * d(x,y) = ||x-y||2 = sqrt( (x1-y1)^2 + (x2-y2)^2 + ... + (xn-yn)^2)
	 * 实际比较大小时，可以不计算sqrt，不影响大小比较。
	 * <p/>
	 * v1,v2长度外部保证长度一致
	 *
	 * @param v1
	 * @param v2
	 * @return
	 */
	public static float distanceSquare(float[] v1, float v2[]) {
		float d = 0f;
		for (int i = 0; i < v1.length; i++) {
			float diff = v1[i] - v2[i];
			d += diff * diff;
		}
		return d;
	}

	public static float distanceSquare(byte[] v,float[] a,float[] b){
		float d = 0f;
		for(int i=0;i<v.length;i++){
			float diff = a[i] - (v[i] + 128)* b[i];
			d += diff * diff;
		}
		return d;
	}


	public static int distanceSquare(byte[] v1,byte[] v2){
		int d = 0;
		for(int i=0;i<v1.length;i++){
			int diff = v1[i] - v2[i];
			d += diff * diff;
		}
		return d;
	}


	public static float[] strToFloatVector(String v){
		String[] sv = v.split(",");
		float[] fv = new float[sv.length];
		for(int i=0;i<sv.length;i++){
			fv[i] = Float.parseFloat(sv[i]);
		}
		return fv;
	}

	public static String strFromFloatVector(float[] vs){
		StringBuilder builder = new StringBuilder(vs.length * 10);
		for(int i=0;i<vs.length;i++){
			if(builder.length() > 0){
				builder.append(",");
			}
			builder.append(vs[i]);
		}
		return builder.toString();
	}
}
