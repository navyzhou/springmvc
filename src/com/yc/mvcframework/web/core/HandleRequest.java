package com.yc.mvcframework.web.core;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.yc.mvcframework.web.annotation.YCRequestParam;

public class HandleRequest {
	/**
	 * 处理请求的方法
	 * @param request 请求
	 * @param method 处理该请求的方法
	 * @throws IllegalAccessException 
	 * @throws InstantiationException 
	 */
	public static Object[] handle(HttpServletRequest request, Method method,HttpServletResponse response) throws InstantiationException, IllegalAccessException {
		int count = method.getParameterCount();
		if (count <= 0) { // 如果没有参数
			return null;
		}
		Parameter[] params = method.getParameters();
		Object[] objs = new Object[count]; // 用来存放方法中的参数

		// 注意：因为编译后参数名称用arg0, arg1存储的，而不是原有的名称，为了获取原有的名称打开Eclipse编译器中的设置。但是此功能必须jdk1.8+才支持
		// 项目上右击 -> Properties -> Java Compiler 弹出框的右侧最下面 Store information about method parameters(usable via reflection) 勾上
		String pname = null; // 形参名称
		YCRequestParam ycqp = null; // 注解
		String paramValue = null; // 参数值
		String typeName = null;
		int index = 0; // 参数索引值
		Map<String, String[]> paramsMap = null;
		Map<String, String> map = null;

		Class<?> clazz = null; 
		Field[] fields = null;
		Object instance = null;
		String attrName = null;

		for (Parameter param : params) {
			pname = param.getName(); // 获取形参名称
			typeName = param.getType().getSimpleName();

			// 判断次参数上有没有@YCRequestParam注解
			ycqp = param.getAnnotation(YCRequestParam.class);

			if (ycqp != null) {
				pname = ycqp.value();
			}
			
			// 根据注解中指定的名称或形参名从请求中获取参数
			paramValue = request.getParameter(pname);
			
			if ("Integer".equals(typeName)) {
				objs[index] = Integer.valueOf(paramValue);
			} else if ("int".equals(typeName)) {
				objs[index] = Integer.parseInt(paramValue);
			} else if ("Double".equals(typeName)) {
				objs[index] = Double.valueOf(paramValue);
			} else if ("double".equals(typeName)) {
				objs[index] = Double.parseDouble(paramValue);
			} else if ("Float".equals(typeName)) {
				objs[index] = Float.valueOf(paramValue);
			} else if ("float".equals(typeName)) {
				objs[index] = Float.parseFloat(paramValue);
			} else if ("String".equals(typeName)) {
				objs[index] = paramValue;
			} else if ("Map".equals(typeName)) { // 如果是map
				paramsMap = request.getParameterMap();
				map = new HashMap<String, String>();
				for (Entry<String,String[]> entry : paramsMap.entrySet()) {
					map.put(entry.getKey(), entry.getValue()[0]);
				}
				objs[index] = map;
			} else if("HttpSession".equals(typeName)) {
				objs[index] = request.getSession();
			} else if("ServletRequest".equals(typeName) || "HttpServletRequest".equals(typeName)) {
				objs[index] = request;
			} else if("ServletContext".equals(typeName)) {
				objs[index] = request.getServletContext();
			} else if("ServletResponse".equals(typeName) || "HttpServletResponse".equals(typeName)) {
				objs[index] = response;
			} else { // 否则当成实体类对象
				clazz = param.getType(); // 获取参数的类型信息
				fields = clazz.getDeclaredFields();
				
				instance = clazz.newInstance(); // 实例化对象

				for (Field field : fields) { // 循环所有属性注值
					field.setAccessible(true); // 强吻属性
					attrName = field.getName(); // 获取属性名
					paramValue = request.getParameter(attrName);

					if (paramValue == null) { 
						continue;
					}
					// 如果不为空，则注值
					typeName = field.getType().getSimpleName();

					if ("Integer".equals(typeName)) {
						field.set(instance, Integer.valueOf(paramValue));
					} else if ("int".equals(typeName)) {
						field.set(instance, Integer.parseInt(paramValue));
					} else if ("Double".equals(typeName)) {
						field.set(instance, Double.valueOf(paramValue));
					} else if ("double".equals(typeName)) {
						field.set(instance, Double.parseDouble(paramValue));
					} else if ("Float".equals(typeName)) {
						field.set(instance, Float.valueOf(paramValue));
					} else if ("float".equals(typeName)) {
						field.set(instance, Float.parseFloat(paramValue));
					} else {
						field.set(instance, paramValue);
					}
				}
				objs[index] = instance;
			}
			index ++;
		}
		return objs;
	}
}
