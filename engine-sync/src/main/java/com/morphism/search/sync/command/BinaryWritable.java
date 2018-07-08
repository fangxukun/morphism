package com.morphism.search.sync.command;

import io.netty.buffer.ByteBufInputStream;
import io.netty.buffer.ByteBufOutputStream;

import java.io.DataOutput;
import java.io.IOException;

/**
 * User: xukun.fyp
 * Date: 17/5/10
 * Time: 15:00
 */
public interface BinaryWritable {

	void write(ByteBufOutputStream out) throws IOException;

	void readFields(ByteBufInputStream in) throws IOException;
}
