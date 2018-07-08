package com.vdian.engine.server.engine.data;

import com.google.common.base.Charsets;
import com.google.common.collect.Lists;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.apache.solr.core.CoreContainer;
import org.apache.solr.core.SolrCore;
import org.apache.solr.search.SolrIndexSearcher;
import org.apache.solr.util.RefCounted;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Properties;

/**
 * User: xukun.fyp
 * Date: 17/5/18
 * Time: 16:49
 */
public class Indexes {
	public static final String 			INDEX_DIR_DATE_POSTFIX		=	"yyyyMMddHHmmss";
	public static final String 			INDEX_DIR_FORMAT			=	"index.%s";

	public static final String 			FILE_NAME_INDEX_PROP		=	"index.properties";
	public static final String			KEY_INDEX_PROP				=	"index";
	public static final String 			INDEX_KV_FORMAT				=	"index=%s";

	/**
	 * data目录下面新的index目录，以时间后缀区分
	 * @param dataPath
	 * @return
	 * @throws IOException
	 */
	public static String newIndexDirName(Path dataPath) throws IOException {
		String datePostfix = DateFormatUtils.format(Calendar.getInstance(),INDEX_DIR_DATE_POSTFIX);
		String newDirName = String.format(INDEX_DIR_FORMAT,datePostfix);
		assert Files.list(dataPath).noneMatch(path -> StringUtils.equals(newDirName,path.getFileName().toString()));

		return newDirName;
	}

	public static String indexPropertiesPath(String dataPath){
		return Paths.get(dataPath,FILE_NAME_INDEX_PROP).toString();
	}

	public static String readIndexFromProperties(Path indexPropPath) throws IOException {
		try(InputStream is = Files.newInputStream(indexPropPath)){
			Properties indexProp = new Properties();
			indexProp.load(is);
			return (String)indexProp.get(KEY_INDEX_PROP);
		}
	}

	public static void switchIndex(SolrCore core,String newIndexDirName) throws IOException {
		String indexVal = newIndexDirName;

		Path indexPropPath = Paths.get(indexPropertiesPath(core.getDataDir()));
		Files.deleteIfExists(indexPropPath);

		byte[] indexKV = String.format(INDEX_KV_FORMAT,indexVal).getBytes(Charsets.UTF_8);
		Files.write(indexPropPath, indexKV);

		CoreContainer container = core.getCoreDescriptor().getCoreContainer();
		container.reload(core.getName());
	}


	public static boolean existDocs(SolrCore core){
		RefCounted<SolrIndexSearcher> holder = core.getSearcher();
		try{
			SolrIndexSearcher searcher = holder.get();
			long numOfDocs = searcher.numDocs();

			return numOfDocs > 0 ? true : false;
		}finally {
			holder.decref();
		}
	}


	public static void main(String[] args){
		List<String> list = Lists.newArrayList("a002","a003","a001","a005");
		System.out.println(list.stream().sorted().findFirst().get());
	}

	public static String tempPath(SolrCore core){
		return Paths.get(core.getDataDir(),".temp").toString();
	}
}
