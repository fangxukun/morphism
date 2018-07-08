package com.vdian.engine.server.servlet;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * User: xukun.fyp
 * Date: 17/5/2
 * Time: 10:22
 */
public class StatusCheck extends HttpServlet {

	public static AtomicBoolean RUNNING = new AtomicBoolean(false);

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		if(RUNNING.get()){
			resp.getWriter().write("OK");
		}else{
			resp.getWriter().write("STARTING");
		}
	}


}