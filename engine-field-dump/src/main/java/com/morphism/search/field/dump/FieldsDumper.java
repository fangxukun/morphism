package com.morphism.search.field.dump;

import com.morphism.search.coord.curator.CuratorAccessApi;
import com.morphism.search.coord.curator.Paths;
import com.morphism.search.coord.curator.joint.CommonSteps;
import com.morphism.search.coord.curator.joint.Function;
import com.morphism.search.coord.curator.joint.Joint;
import com.morphism.search.coord.curator.joint.PathData;
import com.morphism.search.field.update.FieldsUpdater;
import org.apache.solr.core.CoreContainer;
import org.apache.solr.core.SolrCore;

import java.io.IOException;

/**
 * User: xukun.fyp
 * Date: 17/4/11
 * Time: 16:08
 */
public class FieldsDumper {
	private final CuratorAccessApi 				api;
	private final Joint							joint;
	private final CoreContainer	 				container;
	private final String 						serviceName;


	public FieldsDumper(String connectString, String namespace, CoreContainer container, String serviceName) throws IOException {
		this.api = new CuratorAccessApi(connectString,namespace);
		this.joint = new Joint(api, Paths.getUpdateJointPath(serviceName), CommonSteps.STEP_2);
		this.container = container;
		this.serviceName = serviceName;
	}


	public void init() throws Exception {
		this.joint.listen(CommonSteps.STEP_1, new Function<PathData>() {
			FieldsUpdater 				updater;
			boolean 					running;

			@Override
			public String call(PathData context) throws Exception {
				try{
					running = true;
					String jsonString = context.getPayload();
					DumpLayout layout = DumpLayout.fromJsonString(jsonString);
					SolrCore core = container.getCore(layout.coreName);
					if(core == null){
						throw new IllegalStateException(String.format("core %s not exist in serviceName:",layout.coreName));
					}

					updater = new FieldsUpdater(layout,core);
					updater.init();
					return updater.updateDocValues();
				}finally {
					running = false;
				}
			}

			@Override
			public String stop() {
				if(running){
					return updater.stop();
				}
				return null;
			}

			@Override
			public boolean isRunning() {
				return running;
			}
		});
	}
}

