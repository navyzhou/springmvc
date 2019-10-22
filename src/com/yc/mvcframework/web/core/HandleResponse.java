package com.yc.mvcframework.web.core;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class HandleResponse {
	/**
	 * 404报错
	 * @param response
	 * @throws IOException 
	 */
	protected static void send404(HttpServletResponse response, String url) throws IOException {
		PrintWriter out = response.getWriter();
		out.print("<h1>HTTP/1.1 404 File Not Found!</h1>");
		out.flush();
	}

	/**
	 * 返回字节数据
	 * @param response
	 * @param bt
	 * @throws IOException 
	 */
	protected static void sendData(HttpServletResponse response, byte[] bt) throws IOException {
		ServletOutputStream sos = response.getOutputStream();
		sos.write(bt);
		sos.flush();
	}
	
	/**
	 * 以json格式返回数据
	 * @param response
	 * @param obj
	 * @throws IOException
	 */
	protected static void sendJson(HttpServletResponse response, Object obj) throws IOException {
		PrintWriter out = response.getWriter();
		Gson gson = new GsonBuilder().serializeNulls().create();
		out.print(gson.toJson(obj));
		out.flush();
	}
}
