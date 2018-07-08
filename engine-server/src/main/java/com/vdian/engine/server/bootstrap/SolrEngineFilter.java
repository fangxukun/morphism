package com.vdian.engine.server.bootstrap;

import com.google.common.io.Resources;
import com.vdian.engine.server.AdminClient;
import com.vdian.engine.server.EngineContext;
import com.vdian.engine.server.engine.SolrEngine;
import com.vdian.search.commons.EngineStatus;
import com.vdian.search.configuration.EngineConfiguration;
import com.vdian.search.configuration.ResourceLoader;
import com.vdian.search.configuration.loader.LocalFileResourceLoader;
import com.vdian.search.configuration.loader.MetaClientResourceLoader;
import com.vdian.search.netty.common.ServerLayout;
import com.vdian.search.netty.server.NettySolrServer;
import org.apache.commons.lang.StringUtils;
import org.apache.solr.core.CoreContainer;
import org.apache.solr.servlet.SolrDispatchFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.*;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * User: xukun.fyp
 * Date: 16/11/30
 * Time: 11:06
 */
public class SolrEngineFilter extends SolrDispatchFilter {
	private static final Logger				LOGGER		= LoggerFactory.getLogger(SolrEngineFilter.class);
	private SolrEngine						engine;

	@Override
	public void init(FilterConfig config) throws ServletException {
		try{
			this.engine = new SolrEngine(config);
			this.engine.init();

			super.init(config);
			CoreContainer container = getCores();

			//wait until all core is load finished
			container.waitForLoadingCoresToFinish(Integer.MAX_VALUE);
			LOGGER.warn("cores are loaded complete!");
			this.engine.initCores(getCores());
		}catch (Exception e){
			throw new RuntimeException(e);
		}
	}

	@Override
	public void destroy() {
		super.destroy();
		this.engine.destroy();
	}

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
		super.doFilter(request, response, chain);
	}
}

