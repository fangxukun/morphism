package com.vdian.search.netty.protocol;

import com.google.common.base.Charsets;
import org.apache.solr.common.util.ContentStream;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;

/**
 * User: xukun.fyp
 * Date: 17/3/28
 * Time: 20:07
 */
public class ContentStreamWrap implements ContentStream {
	private SolrProtocol.ContentStream			stream;

	public ContentStreamWrap(SolrProtocol.ContentStream stream) {
		this.stream = stream;
	}

	@Override
	public String getName() {
		return stream.getName();
	}

	@Override
	public String getSourceInfo() {
		return stream.getSourceInfo();
	}

	@Override
	public String getContentType() {
		return stream.getContentType();
	}

	@Override
	public Long getSize() {
		return stream.getSize();
	}

	@Override
	public InputStream getStream() throws IOException {
		return stream.getStream().newInput();
	}

	@Override
	public Reader getReader() throws IOException {
		return new InputStreamReader(getStream(), Charsets.UTF_8);
	}
}
