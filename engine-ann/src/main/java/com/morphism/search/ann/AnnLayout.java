package com.morphism.search.ann;

/**
 * User: xukun.fyp
 * Date: 17/3/12
 * Time: 15:42
 * Ann Index以及检索相关的基本配置，有些配置不太好从外部注入，只有通过这个配置注入
 */
public class AnnLayout {
	public static AnnLayout		injectLayout;

	public final int 			numOfTree;				//构建索引时，创建多少克树。
	public final int 			meanIterationStep;		//计算两个均值时迭代的次数.
	public final int 			leafNodeMaxItem;		//叶子节点最大数据量，此值越大性能就会好一些，但是准确率会降低
	public final DataType		dataType;				//数据存储类型Byte,Float。用户输入的是Float，可以将Float有损映射到Byte范围，对空间有很大的提升(为原来的30%一下)，速度也有一定提升，准确率有一定下降(96%-->94%)
	public final boolean 		diskIndex;				//如果构建索引量太大，则使用diskIndex，可以避免outOfMem


	public AnnLayout(int numOfTree, int meanIterationStep, int leafNodeMaxItem,DataType dataType,boolean diskIndex) {
		this.numOfTree = numOfTree;
		this.meanIterationStep = meanIterationStep;
		this.leafNodeMaxItem = leafNodeMaxItem;
		this.dataType = dataType;
		this.diskIndex = diskIndex;
	}


	public static AnnLayout annLayout(){
		if(injectLayout != null){
			return injectLayout;
		}
		return new AnnLayout(80,200,64,DataType.BYTE,false);
	}


	public static void injectAnnLayout(AnnLayout layout){
		injectLayout = layout;
	}


	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (!(o instanceof AnnLayout))
			return false;

		AnnLayout layout = (AnnLayout) o;

		if (numOfTree != layout.numOfTree)
			return false;
		if (meanIterationStep != layout.meanIterationStep)
			return false;
		if (leafNodeMaxItem != layout.leafNodeMaxItem)
			return false;
		if (diskIndex != layout.diskIndex)
			return false;
		return dataType == layout.dataType;

	}

	@Override
	public int hashCode() {
		int result = numOfTree;
		result = 31 * result + meanIterationStep;
		result = 31 * result + leafNodeMaxItem;
		result = 31 * result + dataType.hashCode();
		result = 31 * result + (diskIndex ? 1 : 0);
		return result;
	}

	@Override
	public String toString() {
		return "AnnLayout{" +
				"numOfTree=" + numOfTree +
				", meanIterationStep=" + meanIterationStep +
				", leafNodeMaxItem=" + leafNodeMaxItem +
				", dataType=" + dataType +
				", diskIndex=" + diskIndex +
				'}';
	}

	public static void main(String[] args){
		System.out.println(annLayout());
	}
}
