package com.vdian.search.engine.api.helloworld;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

/**
 * User: xukun.fyp
 * Date: 17/4/28
 * Time: 10:22
 */
@Path("helloworld")
public class HelloWorld {

	@GET
	@Produces("text/plain")
	public String getHello(){
		return "hello world!";
	}
	@GET
	@Path("/v2")
	@Produces("text/plain")
	public String getHelloV2(){
		return "hello world V2!";
	}
}
