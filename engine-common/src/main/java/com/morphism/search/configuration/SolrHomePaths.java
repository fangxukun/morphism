package com.morphism.search.configuration;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.time.DateFormatUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 * User: xukun.fyp
 * Date: 16/12/1
 * Time: 11:30
 */
public class SolrHomePaths {
	public static final String 			CURRENT		=	"current";


	public static Path currentTimePath(Path solrHome){
		String subTimePath = DateFormatUtils.format(System.currentTimeMillis(), "yyyyMMddHHmmss") + "_backup";
		return solrHome.resolve(subTimePath);
	}

	public static Path currentTempPath(Path solrHome){
		return solrHome.resolve("temp");
	}

	/**
	 * delete redundant backup path and switch current solrHome with newTempPath
	 * @param solrHome
	 * @param newPath
	 * @throws IOException
	 */
	public static void switchPath(Path solrHome,Path newPath) throws IOException{
		Path current = solrHome.resolve(SolrHomePaths.CURRENT);

		//1.delete redundant backup path
		deleteBackupPath(3, newPath.getParent());

		//2. move solrHome to backup. move newPath to solrHome
		try{
			if(Files.exists(current)){
				Path backupPath = currentTimePath(solrHome);
				Files.move(current,backupPath, StandardCopyOption.ATOMIC_MOVE);
			}
			Files.move(newPath,current,StandardCopyOption.ATOMIC_MOVE);
		}finally {
			if(!Files.exists(current)){
				throw new RuntimeException(String.format("switch path failed,solrHome:%s,newPath:%s",solrHome,newPath));
			}
		}
	}

	private static void deleteBackupPath(int retainSize,Path parentPath) throws IOException{
		Iterator<Path> iterator = Files.newDirectoryStream(parentPath).iterator();
		List<String> subPaths = new ArrayList();
		while(iterator.hasNext()){
			Path path = iterator.next();
			String name = path.getFileName().toString();
			if (name.endsWith("backup")){
				subPaths.add(path.toString());
			}
		}

		if(subPaths.size() > retainSize){
			Collections.sort(subPaths);
			for(int i=0;i < subPaths.size() - retainSize;i++){
				FileUtils.deleteDirectory(Paths.get(subPaths.get(i)).toFile());
			}
		}
	}


	public static Path solrXml(Path solrHome){
		return Paths.get(solrHome.toString(),"solr.xml");
	}

	public static Path corePath(Path solrHome,String coreName){
		return Paths.get(solrHome.toString(),coreName);
	}

	public static Path corePropertiesPath(Path solrHome,String coreName){
		return Paths.get(solrHome.toString(),coreName,"core.properties");
	}

	public static Path coreConfPath(Path solrHome,String coreName){
		return Paths.get(solrHome.toString(),coreName,"conf");
	}
	public static Path schemaPath(Path solrHome,String coreName){
		return Paths.get(solrHome.toString(),coreName,"conf","schema.xml");
	}

	public static Path solrConfigPath(Path solrHome,String coreName){
		return Paths.get(solrHome.toString(),coreName,"conf","solrconfig.xml");
	}

}
