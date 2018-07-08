package com.morphism.engine.server.bootstrap;

import com.morphism.engine.server.engine.SolrEngine;
import org.apache.solr.core.CoreContainer;
import org.apache.solr.servlet.SolrDispatchFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.*;
import java.io.IOException;

/**
 * User: xukun.fyp
 * Date: 16/11/30
 * Time: 11:06
 */
public class SolrEngineFilter extends SolrDispatchFilter {
	private static final Logger				LOGGER		= LoggerFactory.getLogger(SolrEngineFilter.class);
	private SolrEngine engine;

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

