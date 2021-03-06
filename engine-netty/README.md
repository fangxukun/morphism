## Netty-Solr
  SolrNetty客户端与服务端，包含query与Update部分。

## 使用:

## 性能对比:
1. 100w文档数据。
```
  引擎内存都限定为Xmx=500M.
  0.线程数量:1
  netty
  ----------------------------------------------------------------
  queryCount:			10000
  queryCost:			135759ms
  query RT:			13.5759ms   
  avgNumFound:		239655
  cost:				135763
  qps:				73.65777
  
  Http-Client-PerThread
  ----------------------------------------------------------------
  queryCount:			10000
  queryCost:			147813ms
  query RT:			14.7813ms
  avgNumFound:		238620
  cost:				148054
  qps:				67.54292
  
  Http-Client-PerQuery
  ----------------------------------------------------------------
  queryCount:			10000
  queryCost:			137697ms
  query RT:			13.7697ms
  avgNumFound:		238620
  cost:				137701
  qps:				72.62112
  
  
  1.线程数量:5
  
  netty
  ----------------------------------------------------------------
  queryCount:			10000
  queryCost:			163870ms
  query RT:			16.387ms
  avgNumFound:		239655
  cost:				32843
  qps:				304.47888
  
  Http-Client-PerThread
  ----------------------------------------------------------------
  queryCount:			10000
  queryCost:			172632ms
  query RT:			17.2632ms
  avgNumFound:		238620
  cost:				34949
  qps:				286.13123
  
  Http-Client-PerQuery
  ----------------------------------------------------------------
  queryCount:			10000
  queryCost:			170180ms
  query RT:			17.018ms
  avgNumFound:		238620
  cost:				34110
  qps:				293.16916
  
  
  
  2. 线程数量:10
  
  netty
  ----------------------------------------------------------------
  queryCount:			10000
  queryCost:			279026ms
  query RT:			27.9026ms
  avgNumFound:		239655
  cost:				28084
  qps:				356.07465
  
  
  Http-Client-PerThread
  ----------------------------------------------------------------
  queryCount:			10000
  queryCost:			267351ms
  query RT:			26.7351ms
  avgNumFound:		238620
  cost:				27229
  qps:				367.2555
  
  Http-Client-PerQuery
  ----------------------------------------------------------------
  queryCount:			10000
  queryCost:			310469ms
  query RT:			31.0469ms
  avgNumFound:		238620
  cost:				31301
  qps:				319.4786
  
  3. 线程数量:15
  netty
  ----------------------------------------------------------------
  queryCount:			9990
  queryCost:			389421ms
  query RT:			38.981083ms
  avgNumFound:		239655
  cost:				26158
  qps:				381.90994
  
  Http-Client-PerThread
  ----------------------------------------------------------------
  queryCount:			9990
  queryCost:			403291ms
  query RT:			40.36947ms
  avgNumFound:		238620
  cost:				27482
  qps:				363.51065
  
  Http-Client-PerQuery
  ----------------------------------------------------------------
  queryCount:			9990
  queryCost:			484018ms
  query RT:			48.45025ms
  avgNumFound:		238620
  cost:				33103
  qps:				301.78534
```

