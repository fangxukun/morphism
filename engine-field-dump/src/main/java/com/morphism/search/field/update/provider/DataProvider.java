package com.morphism.search.field.update.provider;

import com.morphism.search.field.update.UpdateException;

import java.io.IOException;

/**
 * User: xukun.fyp
 * Date: 17/4/11
 * Time: 19:35
 */
public interface DataProvider {
	void init() throws IOException, UpdateException;

	Iterable<Record> readRecords();
}
