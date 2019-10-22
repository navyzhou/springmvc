package com.yc.mvcframework.web.core;

import java.lang.reflect.Method;
import java.util.Arrays;

public class HandleMapperInfo {
	private Object obj; // 所在对象
	private Method method; // 对应方法
	private Object[] args; // 方法对应的参数
	
	public HandleMapperInfo(Object obj, Method method) {
		super();
		this.obj = obj;
		this.method = method;
	}

	public HandleMapperInfo() {
		super();
	}

	@Override
	public String toString() {
		return "HandleMapperInfo [obj=" + obj + ", method=" + method + ", args=" + Arrays.toString(args) + "]";
	}

	public Object getObj() {
		return obj;
	}

	public void setObj(Object obj) {
		this.obj = obj;
	}

	public Method getMethod() {
		return method;
	}

	public void setMethod(Method method) {
		this.method = method;
	}

	public Object[] getArgs() {
		return args;
	}

	public void setArgs(Object[] args) {
		this.args = args;
	}
}
