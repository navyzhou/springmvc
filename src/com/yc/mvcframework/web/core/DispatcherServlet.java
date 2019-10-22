package com.yc.mvcframework.web.core;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.yc.mvcframework.web.annotation.YCResponseBody;

public class DispatcherServlet extends HttpServlet{
	private static final long serialVersionUID = 4019733454350449138L;
	private MyFrameworkCore myFrameworkCore = null;

	@Override
	public void init(ServletConfig config) throws ServletException {
		// 读取配置文件
		String configName = "application.properties";
		String temp = config.getInitParameter("contextConfigLocation");
		if (!StringUtil.checkNull(temp)) {
			configName = temp;
		}
		myFrameworkCore = new MyFrameworkCore(configName);
	}

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		doPost(request, response);
	}

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String url = request.getRequestURI(); // 获取请求的资源地址  /myspringmvc/dept/finds
		String contextPath = request.getContextPath(); // 获取当前的姓名名称  /myspringmvc

		// 得到请求路径
		url = url.replace(contextPath, "").replaceAll("/+", "/"); //   /dept/finds 并将多个/替换成一个 /
		if (url.contains("?")) {
			url = url.substring(0, url.indexOf("?"));
		}
		
		// 根据请求路径获取对应的处理方法
		HandleMapperInfo mapperInfo = myFrameworkCore.getMapper(url);

		if (mapperInfo == null) { // 如果没有次路径的映射，则放过去
			handleStaticResource(request.getServletContext().getRealPath("") + url.substring(1), response); // 调用处理静态资源的方法
			return;
		}

		// 如果有则此方法，则激活此方法，则需要处理参数
		try {
			Method method = mapperInfo.getMethod();
			Object[] args = HandleRequest.handle(request, method, response); // 获取该方法对应形参的值
			
			Object obj = method.invoke(mapperInfo.getObj(), args); // 激活运行次方法，获取返回值
			
			if (method.isAnnotationPresent(YCResponseBody.class)) { // 说明要以json格式返回
				HandleResponse.sendJson(response, obj);
			} else { // 否则内部转发页面
				request.getRequestDispatcher(String.valueOf(obj)).forward(request, response);
			}
		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 处理静态资源的方法
	 * @param url 要处理的静态资源
	 * @throws IOException 
	 */
	private void handleStaticResource(String url, HttpServletResponse response) throws IOException {
		// 读取这个静态资源文件
		File file = new  File(url);
		if (!file.exists() || !file.isFile()) {
			HandleResponse.send404(response, url);
			return;
		}
		FileInputStream fis = null;
		// 读取文件
		try {
			fis = new FileInputStream(file);
			byte[] bt=new byte[fis.available()];
			fis.read(bt);
			HandleResponse.sendData(response, bt);
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (fis != null) {
				fis.close();
			}
		}
	}
}