package com.morphism.engine.update.dv;

import org.apache.commons.lang3.tuple.Pair;

import java.util.Iterator;

/**
 * User: xukun.fyp
 * Date: 16/12/3
 * Time: 13:30
 */
public class FieldInputIterator<Key,Val> implements Iterator<Pair<Key,Val>>{
	private String 		keyField;
	private String 		valField;


	@Override
	public boolean hasNext() {
		return false;
	}

	@Override
	public Pair<Key, Val> next() {
		return null;
	}

	@Override
	public void remove() {
		throw new UnsupportedOperationException();
	}

	public String getKeyField() {
		return keyField;
	}

	public String getValField() {
		return valField;
	}
}
