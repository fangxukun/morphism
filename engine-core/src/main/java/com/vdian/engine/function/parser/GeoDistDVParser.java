package com.vdian.engine.function.parser;

import com.spatial4j.core.context.SpatialContext;
import com.spatial4j.core.shape.Point;
import org.apache.lucene.queries.function.ValueSource;
import org.apache.lucene.spatial.serialized.SerializedDVStrategy;
import org.apache.solr.common.params.SpatialParams;
import org.apache.solr.schema.FieldType;
import org.apache.solr.schema.RptWithGeometrySpatialField;
import org.apache.solr.search.FunctionQParser;
import org.apache.solr.search.SyntaxError;
import org.apache.solr.search.ValueSourceParser;
import org.apache.solr.util.DistanceUnits;
import org.apache.solr.util.SpatialUtils;

/**
 * User: xukun.fyp
 * Date: 16/2/26
 * Time: 10:07
 * 参考GeoDistValueSourceParser，由于SpatialRecursivePrefixTreeFieldType计算geodist的时候会加载sField中的所有数据(Point)并缓存，
 * 考虑到VSearch使用softCommit来实现实时性，会频繁导致缓存失效(秒级别失效),重新加载缓存很耗时，故通过另外的RptWithGeometrySpatialField
 * 来作为location字段，存储是将地理位置作为BinaryDocValue存储。
 */
public class GeoDistDVParser extends ValueSourceParser {

	@Override
	public ValueSource parse(FunctionQParser fp) throws SyntaxError {
		Point queryPoint = parsePoint(fp);
		if(queryPoint == null){
			throw new SyntaxError("geodistdv - not enough parameters:pt");
		}

		String sField = fp.getParam(SpatialParams.FIELD);
		if(sField == null){
			throw new SyntaxError("geodistdv - not enough parameters:sfield");
		}
		FieldType fieldType = fp.getReq().getSchema().getField(sField).getType();
		if(!(fieldType instanceof RptWithGeometrySpatialField)){
			throw new SyntaxError("geodistdv - can only support RptWithGeometrySpatialField");
		}

		SerializedDVStrategy strategy = ((RptWithGeometrySpatialField)fieldType).getStrategy(sField).getGeometryStrategy();
		DistanceUnits distanceUnits = ((RptWithGeometrySpatialField)fieldType).getDistanceUnits();
		return strategy.makeDistanceValueSource(queryPoint, distanceUnits.multiplierFromDegreesToThisUnit());
	}


	private Point parsePoint(FunctionQParser fp) throws SyntaxError {
		String ptStr = fp.getParam(SpatialParams.POINT);
		if(ptStr == null){
			return null;
		}
		return SpatialUtils.parsePointSolrException(ptStr, SpatialContext.GEO);
	}
}
