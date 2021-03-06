## Ann检索:
```
    Ann(Approximate Nearest Neighbors)近似最邻近检索，用于在空间(d维)中搜索给定点queryPoint最邻近的点的集合(searchResult)。
```
### 代码:
```
http://gitlab.vdian.net/search_team/search-engine/tree/master/engine-ann
```
### 名称定义:
```
  向量/点:X,Y,...
  向量维度:d
  查询向量:qv
  向量数据集:Sn,数据集中向量数量:n
```
### 空间与点(向量):
  ```
    X = (x1,x2....xd) 表示d维空间中的一个点。
  ```

### 最邻近:
  ```
    欧几里得距离:D = sqrt(||X-Y||) = sqrt((x1-y1)^2 + (x2-y2)^2 + ... + (xd-yd)^2)来定义最邻近距离。

    两个向量的cos(X,Y) = X * Y /|X|*|Y|,如果是归一化的向量(|X|=1) 那么D = sqrt(2-2cos(X,Y)).
  ```

### 最邻近与近似最邻近：
  ```
    由于如果需要精确计算最近邻，则需要暴力的计算查询查询向量与数据集中所有向量的距离，然后排序取TopN.通常情况效率较低(d=40,n=100w)与最邻近搜索(75%准确率)差距25倍，一般的图片、语音、文本相识度等查找可以进行近似检索。
    本实现的近似搜索参考:https://github.com/spotify/annoy，是其Java版本的实现，并整合到solr/lucene 5.2.1版本中。可以将向量搜索和其他条件进行组合检索。
  ```
### Annoy检索:
  ```
    参考链接:https://github.com/spotify/annoy，
  ```

  1. 索引过程
    1. 对数据集Sn随机选取两个点(X1,X2)，S1:从Sn中随机选取点Y,计算Y与X1,X2的距离。d1 = d(X1,Y),d2=d(X2,Y).选取距离较小的那个点X1/X2，并使用平均值更新X1/X2。迭代S1 N次
    2. 通过X1,X2计算分割超平面HP。对数据集Sn按照超平面HP分成两类（左节点/右节点），并对两类数据集继续按照1进行迭代。
    3. 知道每个数据集数量小于maxLeafItem时，直接绑定到叶子节点。
    4. 迭代1~3 构建numOfTree棵此类树

  2. 检索过程:
    1. 对所有的树，计算queryVector与HP判断属于左节点还是右节点(计算margin)，不断迭代知道叶子节点.
    2. 选取margin TopN的叶子节点，将左右的数据加入到候选集合(<=searchNum).
    3. 在候选集合中，精确计算距离，进行排序，选取returnNum个数据返回。

  3. 超平面HP与margin计算：
  ```
  /**
	 * 通过迭代计算两个均值点(可以认为是通过距离聚类),并得到两个向量之差 以及planeOffset, 通过二维来进行理解推导,可扩展到多维
	 * 		^
	 * 		|
	 * 		|  * A
	 * 		|   .   * X:输入变量
	 * 		|    .
	 * 		|     * C:(A+B)/2
	 * 		|      .
	 * 		|    	.
	 * 		|		 * B
	 * --------------------------------------->
	 * 		|O
	 * 		|
	 * 		|
	 * 		|
	 * 		|
	 * 		|
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
	 * 	 -------------          -------------
	 * 	 后续通过输入X计算	        node.planeOffset 此方法中计算得到的
	 *
	 *
	 */
  ```

### 性能与准确率
1. 随机向量测试:

|参数项|参数值|
|------|------|
|向量数量(n)|10w|
|向量维数(d)|40|
|搜索返回数量(returnNum)|10|
|搜索数量(searchNum)|1w|
|AnnTree数量|80|
|Ann叶子节点数据量|64|

|测试类型|准确率(百分比)|平均RT(微秒)| 备注|
|-------|-------|-------|----|
|AnnC++版本|96.81|2182||
|Java内存版|95.44|3273||
|Lucene磁盘版Float存储|95.42|5052||
|Lucene磁盘版Short存储|95.94|4771||
|Lucene磁盘版Byte存储|95.27|4344||
|暴力搜索|100|17219||


2. 10w图像数据测试

|参数项|参数值|
|------|------|
|向量数量(n)|10w|
|向量维数(d)|512|
|搜索返回数量(returnNum)|10|
|搜索数量(searchNum)|1000|
|AnnTree数量|10|
|Ann叶子节点数据量|64|

|测试类型|准确率(百分比)|平均RT(微秒)| 备注|
|-------|-------|-------|----|
|Java内存版|97.88|1326||
|Lucene磁盘版Float存储|97.96|10161||
|Lucene磁盘版Short存储|97.88|6635|对比Float存储能够压缩到接近原来的50%|
|Lucene磁盘版Byte存储|93.63|4416|对比Float存储能够压缩到接近原来的25%|
|暴力搜索|100|80169||
