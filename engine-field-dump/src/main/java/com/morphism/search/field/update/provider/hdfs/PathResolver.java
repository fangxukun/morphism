package com.morphism.search.field.update.provider.hdfs;

import com.google.common.collect.ImmutableMap;
import org.apache.commons.lang.time.DateFormatUtils;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;

import java.io.IOException;
import java.util.*;

/**
 * User: xukun.fyp
 * Date: 16/3/20
 * Time: 15:09
 *
 * 表达式格式:${resolver:param1,param2,param3}
 *        如:${date:yyyyMMdd,-1}
 */
public class PathResolver {

	public final static Map<String,? extends Resolver> resolvers = ImmutableMap.of(
			"date", new DateResolver(),
			"latest",new LatestResolver());

	public static String resolve(String path,FileSystem fs){
		StringBuilder global = new StringBuilder();
		char[] chars = path.toCharArray();

		int start = -1;
		for(int i=0;i<chars.length;i++){
			switch (chars[i]){
				case '$':
					break;
				case '{':
					start = i + 1;
					break;
				case '}':
					global.append(resolveExpr(chars, start, i,fs));
					start = -1;
					break;
				default:
					if(start == -1){
						global.append(chars[i]);
					}
			}
		}

		return global.toString();
	}

	private static String resolveExpr(char[] expr,int start,int end,FileSystem fs){
		String resolver = null;
		List<String> params = new ArrayList<>();

		int lastIdx = start;
		for(int i=start;i<end;i++){
			switch (expr[i]){
				case ':':
					resolver = String.valueOf(expr,start,i-start);
					lastIdx = i + 1;
					break;
				case ',':
					params.add(String.valueOf(expr,lastIdx,i-lastIdx));
					lastIdx = i + 1;
					break;
				default:
			}
		}
		params.add(String.valueOf(expr,lastIdx,end - lastIdx));
		String fullName = String.valueOf(expr, 0, start - 2);

		if(resolver == null){
			resolver = String.valueOf(expr,start,end-start);
		}
		return resolvers.get(resolver).resolve(params,fullName,fs);
	}

	private static class DateResolver implements Resolver{

		@Override
		public String resolve(List<String> params, String fullPath, FileSystem fs) {
			String format = params.get(0);
			int delta = Integer.valueOf(params.get(1));

			Date date = new Date(System.currentTimeMillis() + 86400 * 1000 * delta);
			return DateFormatUtils.format(date, format);
		}

		public String apply(List<String> input) {
			int delta = Integer.valueOf(input.get(1));
			Date date = new Date(System.currentTimeMillis() + 86400 * 1000 * delta);
			System.out.println(input.get(input.size()-1));
			return DateFormatUtils.format(date, input.get(0));
		}
	}

	private static class LatestResolver implements Resolver{

		@Override
		public String resolve(List<String> params, String fullPath, FileSystem fs) {
			try{
				List<Path> subPaths = listSubPath(fullPath, fs);
				Collections.sort(subPaths, new Comparator<Path>() {
					@Override
					public int compare(Path o1, Path o2) {
						return o2.getName().compareTo(o1.getName());
					}
				});
				return subPaths.get(0).getName();
			}catch (Exception e){
				throw new RuntimeException(e);
			}
		}
	}

	private static List<Path> listSubPath(String path,FileSystem fs) throws IOException {
		List<Path> result = new ArrayList<>();

		FileStatus[] files = fs.listStatus(new Path(path));

		for(FileStatus file : files){
			result.add(file.getPath());
		}
		return result;
	}


	interface Resolver{
		String resolve(List<String> params,String fullPath,FileSystem fs);
	}
}
