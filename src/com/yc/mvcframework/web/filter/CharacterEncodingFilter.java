package com.yc.mvcframework.web.filter;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebFilter;
import javax.servlet.annotation.WebInitParam;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * 编码集过滤器
 * @author navy
 */
@WebFilter(value="/*",initParams={@WebInitParam(name = "encoding", value="utf-8")})
public class CharacterEncodingFilter implements Filter {
	private String encoding = "utf-8";
	
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
		HttpServletRequest req = (HttpServletRequest) request;
		HttpServletResponse resp = (HttpServletResponse) response;
		
		req.setCharacterEncoding(encoding);
		resp.setCharacterEncoding(encoding);
		
		// 调用过滤器链中的的下一个过滤器过滤，如果过滤不通过，则需要调用次方法
		chain.doFilter(req, resp);
	}

	/**
	 * 初始化方法，用来读取配置在这个filter中的初始化参数或全局初始化参数
	 */
	public void init(FilterConfig config) throws ServletException {
		String temp = config.getInitParameter("encoding"); // 根据初始化名称获取配置在这个过滤器中的初始化参数
		if (temp == null) {
			return;
		}
		encoding = temp;
	}

	@Override
	public void destroy() {
		
	}

}
