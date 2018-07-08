package com.morphism.search.field.update.test;

import org.apache.commons.lang.math.RandomUtils;
import org.apache.solr.common.SolrInputDocument;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * User: xukun.fyp
 * Date: 17/3/29
 * Time: 14:13
 */
public class DocumentGenerator {
	public final static AtomicInteger sequence			=	new AtomicInteger(0);

	public static SolrInputDocument newDocument(){
		SolrInputDocument document = new SolrInputDocument();
		document.setField("item_id", sequence.incrementAndGet());
		document.setField("add_time", System.currentTimeMillis());
		document.setField("update_time", System.currentTimeMillis());
		document.setField("item_title", newItemTitle());
		document.setField("status", RandomUtils.nextInt(4));
		document.setField("seller_id",RandomUtils.nextInt(100));
		document.setField("imgs",newImages());
		document.setField("price",RandomUtils.nextInt(100000));
		document.setField("stock",RandomUtils.nextInt(1000));

		return document;
	}

	private static String[] keywordSeeds = new String[]{
			"箱包皮具","热销女包","男包","牛津","纺涤","纶软","深灰色","军绿色","手机袋","男士包袋",
			"2016","平底","小白鞋","一脚蹬","懒人鞋","厚底","休闲","单鞋","乐福鞋","女鞋红",
			"奔驰","专车","专用","氙气灯","高端大气","上档次"
	};

	private static String newItemTitle(){
		int wordCount = RandomUtils.nextInt(5) + 5;
		StringBuilder builder = new StringBuilder();
		for(int i=0;i<wordCount;i++){
			builder.append(keywordSeeds[RandomUtils.nextInt(keywordSeeds.length)]);
		}
		return builder.toString();
	}

	private static List<String> newImages(){
		List<String> result = new ArrayList<>();
		result.add("vshop253724520-1457332887633-8331676.jpg?w=800&h=800");
		result.add("vshop257882701-1418735667-570279.jpg?w=800&h=800");
		result.add("vshop165995790-1402497247-621012.jpg?w=640&h=640");

		return result;
	}

}
