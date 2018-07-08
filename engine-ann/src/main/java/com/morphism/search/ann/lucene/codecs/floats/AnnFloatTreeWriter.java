package com.morphism.search.ann.lucene.codecs.floats;

import com.morphism.search.ann.AnnLayout;
import com.morphism.search.ann.Node;
import com.morphism.search.ann.algorithm.FloatCalculator;
import com.morphism.search.ann.lucene.ValuesAnalyzer;
import com.morphism.search.ann.lucene.codecs.AnnDocValuesConsumer;
import com.morphism.search.ann.lucene.codecs.AnnDocValuesFormat;
import com.morphism.search.ann.lucene.codecs.AnnTreeWriter;
import com.morphism.search.ann.lucene.codecs.MetaCodec;
import org.apache.lucene.codecs.DocValuesConsumer;
import org.apache.lucene.index.FieldInfo;
import org.apache.lucene.store.IndexOutput;
import org.apache.lucene.util.BytesRef;

import java.io.IOException;

/**
 * User: xukun.fyp
 * Date: 17/3/18
 * Time: 14:43
 */
public class AnnFloatTreeWriter implements AnnTreeWriter {
	protected final IndexOutput 			data;
	protected final IndexOutput				meta;
	protected final DocValuesConsumer 		delegate;
	protected final MetaCodec 				metaCodec;


	public AnnFloatTreeWriter(AnnDocValuesConsumer consumer){
		this.data = consumer.data;
		this.meta = consumer.meta;
		this.delegate = consumer.delegate;
		this.metaCodec = consumer.metaCodec;
	}


	@Override
	public void addBinaryField(FieldInfo field, Iterable<BytesRef> values,AnnLayout layout) throws IOException {
		final ValuesAnalyzer analyzer = new ValuesAnalyzer(values);
		analyzer.analysis();

		delegate.addBinaryField(field,values);

		MetaCodec.FieldCodec fieldCodec = metaCodec.addField(field.name,layout);
		fieldCodec.itemMinArray = analyzer.minArray;
		fieldCodec.itemMaxArray = analyzer.maxArray;

		for(int i=0;i<layout.numOfTree;i++){
			Node tree = FloatCalculator.makeTree(analyzer.items, layout);
			long treeStartFP = writeNode(i,tree);
			fieldCodec.addTree(i,treeStartFP,EMPTY_FLOATS,EMPTY_FLOATS);
		}
	}


	private long writeNode(int treeId,Node node) throws IOException{
		//1.left、right node
		long leftStartFP = AnnDocValuesFormat.NONE;
		if(node.left != null){
			leftStartFP = writeNode(treeId, node.left);
		}

		long rightStartFP = AnnDocValuesFormat.NONE;
		if(node.right != null){
			rightStartFP = writeNode(treeId, node.right);
		}

		long startFP = data.getFilePointer();
		data.writeVLong(leftStartFP);
		data.writeVLong(rightStartFP);

		//2.Vector
		if(node.vector == null){
			data.writeVInt(0);
		}else{
			data.writeVInt(node.vector.length);
			data.writeVInt(treeId);
			for(float f : node.vector){
				data.writeInt(Float.floatToIntBits(f));
			}
		}

		//3.PlaneOffset
		data.writeInt(Float.floatToIntBits(node.planeOffset));

		//4.Items
		if(node.docIds == null){
			data.writeVInt(0);;
		}else{
			data.writeVInt(node.docIds.length);
			for(long docId : node.docIds){
				data.writeVLong(docId);
			}
		}
		return startFP;
	}



}
