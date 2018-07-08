## Ann介绍:
  * [Ann介绍](https://github.com/spotify/annoy)
  * [Ann基本介绍](http://gitlab.vdian.net/search_team/search-wiki/wikis/summary/ann%E5%9F%BA%E6%9C%AC%E4%BB%8B%E7%BB%8D)
  * 在一个大的向量集合中近似找出和查询向量最近(欧氏距离,consine值)的向量集合。

## Lucene版本Ann：
  * Java版本的Ann实现，并且集成到lucene(solr)中，可以和其他条件组合搜索。

### 代码:
  * [engine-ann](http://gitlab.vdian.net/search_team/search-engine/tree/master/engine-ann)

### 使用:
  1. 工程中添加engine-ann的依赖
  ```xml
    <groupId>com.vdian.search</groupId>
    <artifactId>engine-ann</artifactId>
    <version>1.0-SNAPSHOT</version>
  ```

  2. solrConfig.xml中增加配置:
    ```xml
    <!-- 用于计算annDistance的ValueSource -->
    <valueSourceParser name="annParser" class="AnnValueSourceParser" />
    ```
    ```xml
    <!-- Ann Query的定制 -->
    <queryParser name="ann" class="AnnQParserPlugin"/>
    ```
  3. schema.xml中的配置:
    ```xml
    <!--
      Schema中FieldType,Field配置。
      class:      VectorStringField:可以通过字符串传入vector形如："2.323,4.323,0.344,4.34"
                  VectorBytesField:直接传入Bytep[]数据,BytesUtils#floatsToBytes,可以用来转换

      dataType:   BYTE/SHORT/FLOAT,原始数据都是float，在内部存储时可以转换为byte或者short可以节省存储空间(1/4,1/2)。

      docValues:  true ,都是true

      docValuesFormat:  ann 此配置DocValueFormat将使用com.vdian.search.ann.lucene.codecs.AnnDocValuesFormat

      leafNodeMaxItem:  64 tree叶子节点存储的数据量,越大索引和搜索性能越好，但是准确率会降低 建议:[32-256]

      numOfTree:        5 构建索引时build的annTree数量，数量越大准确率越高，索引性能越差，建议:[5~80]
    -->
    <fieldType class="VectorStringField" dataType="BYTE"  docValues="true" docValuesFormat="ann" leafNodeMaxItem="64" name="vector" numOfTree="5"/>

    <field docValues="true" indexed="true" multiValued="false" name="image_features" required="false" stored="true" type="vector"/>
    ```

### 代码相关:
  * 查询入口:AnnVectorQuery
  * 索引入口:AnnDocValuesFormat
  * Codec自定义:目前lucene不是很方便自定义Format，只能使用已经存在的集中形式进行定制，如DocValueFormat,PostingFormat，否则需要修改DefaultIndexingChain以及众多的代码。
  * Solr/Lucene的Schema中FileType配置透传:比较坑，基本没有方式能够传递到自定义处理类里面，目前通过全局变量来实现。
  * Merge:默认的lucene indexWriter.forceMerge,是不会识别自定义的Codec.需要在离线索引时做好设置。
  * AnnMerge:由于AnnMerge很耗费时间，目前采取的方式是merge时保存多个segment(9).搜索性能会有所的降低。(每个Mapper处理1G数据，每个shard大概需要处理20个Mapper的segment)
  * Ann存储格式:由于Ann索引和检索的都是较大的向量(128~1024维度，目前我们使用的是512维float)，每个文档vector存储需要512*4 = 2k。考虑到AnnTree中树节点也需要存储这些数据(约2k)，故最终的索引4k * 300w = 12G，内存和检索性能有压力。目前采取的Byte存储大小可以压缩到4G左右的索引量.
  * Ann构建索引树代码:FloatCaculator.
  
    

### 项目：
  * 图片搜索:
    1. 引擎:[link](http://ssp.vdian.net/meta/index.html#/manage-engine-service?metaName=engines.vision&_k=7dqnam),目前约1000w商品图片数据，3*2引擎分布,每台机器约350w数据
    2. Dump:微店计算搜索/图片搜索
    3. 同款内部页面：[link](http://ssp.pre.vdian.net/meta/index.html#/image-search?_k=wozmdr)
    4. 商品图片特征数据:@夏威，@俊君


### 引擎端性能压测结果:
  * 待补充
