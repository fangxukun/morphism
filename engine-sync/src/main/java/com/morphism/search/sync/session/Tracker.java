package com.morphism.search.sync.session;

import com.morphism.search.sync.command.BinaryWritable;

/**
 * User: xukun.fyp
 * Date: 17/5/11
 * Time: 10:15
 */
public interface Tracker extends BinaryWritable{

	long getRid();

	void setRid(long rid);

}
