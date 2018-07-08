package com.vdian.search.sync.command;

import com.vdian.search.sync.command.common.EchoCommand;
import com.vdian.search.sync.command.list.PathListRequest;
import com.vdian.search.sync.command.list.PathListResponse;
import com.vdian.search.sync.command.path.FileSyncCommand;
import com.vdian.search.sync.command.path.PathSyncCommand;
import com.vdian.search.sync.command.path.PathSyncMetaCommand;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * User: xukun.fyp
 * Date: 17/5/8
 * Time: 17:38
 */
public class Commands {
	public static final int 						COMMAND_PATH_SYNC		=	1;		//Path同步   			client->server
	public static final int							COMMAND_PATH_SYNC_META	=	2;		//Path同步的Meta信息		server->client trigger by path_sync
	public static final int 						COMMAND_FILE_SYNC		=	3;		//具体文件同步			client->server trigger by path_sync_meta in ClientSyncHandler
	public static final int 						COMMAND_ECHO			=	4;

	public static final int							REQUEST_PATH_LIST		=	5;		//Path 目录罗列。
	public static final int 						RESPONSE_PATH_LIST		=	6;
	public static final int 						COMMAND_COMPLETE		=	7;

	public static Map<Integer,CommandFactory> 		commandFactory			=	new ConcurrentHashMap<>();


	static{
		registerCommand(new EchoCommand());
		registerCommand(new FileSyncCommand());
		registerCommand(new PathSyncCommand());
		registerCommand(new PathSyncMetaCommand());

		registerCommand(new PathListRequest());
		registerCommand(new PathListResponse());
	}


	public static Command createCommand(int commandId){
		CommandFactory factory = commandFactory.get(commandId);
		if(factory == null){
			throw new IllegalArgumentException(String.format("command:{} can not found!",commandId));
		}

		return factory.createCommand();
	}

	public static void registerCommand(Command command){
		registerCommand(command.getCommandId(),command.factory());
	}
	public static void registerCommand(int commandId,CommandFactory cf){
		if(commandFactory.containsKey(commandId)){
			String existCF = commandFactory.get(commandId).getClass().getName();
			String newCF = cf.getClass().getName();

			throw new RuntimeException(String.format("commandId:%s exist,failed to register,exist cf:%s,new cf:%s",commandId,existCF,newCF));
		}

		commandFactory.put(commandId,cf);
	}


	interface CommandFactory{
		Command createCommand();
	}
}
