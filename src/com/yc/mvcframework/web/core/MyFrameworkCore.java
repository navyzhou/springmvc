package com.yc.mvcframework.web.core;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;

import com.yc.mvcframework.web.annotation.YCAutowired;
import com.yc.mvcframework.web.annotation.YCComponent;
import com.yc.mvcframework.web.annotation.YCController;
import com.yc.mvcframework.web.annotation.YCRequestMapping;

/**
 * 核心代码
 * 源辰信息
 * @author navy
 * @2019年10月18日
 */
@SuppressWarnings({"rawtypes","unchecked"})
public class MyFrameworkCore {
	private String contextConfigLocation; // 配置文件路径
	private String basePackage; // 扫描的基址路径

	// 扫描到的类名称
	private Set<String> classNames = new HashSet<String>();

	// 用来存放实例化后的类
	private Map<String, Object> instanceObject = new HashMap<String, Object>(); 

	// 用来存放url与method的映射
	private Map<String, HandleMapperInfo> handlerMapper = new HashMap<String, HandleMapperInfo>();

	public MyFrameworkCore(String contextConfigLocation) {
		this.contextConfigLocation = contextConfigLocation;
		doLoadConfig(); // 1. 读取配置文件
		doScanner(); // 2. 扫描包，获取类名
		doInstance(); // 3. 初始化所有类，并将其保存到IoC容器中
		doAutowired(); // 4. 执行依赖注入，即完成@Autowired
		initHandlerMapping(); // 5. 构造HandlerMapping，完成URL与Method的关联
	}

	/**
	 * 1. 读取配置文件
	 */
	private void doLoadConfig() {
		InputStream is = null;
		try {
			is = this.getClass().getClassLoader().getResourceAsStream(contextConfigLocation);
			Properties properties = new Properties();
			properties.load(is);
			basePackage = properties.getProperty("basePackage");
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (is != null) {
				try {
					is.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	/**
	 * 2. 扫描相关类
	 * @param basePackage 要扫描的包的基址路径
	 */
	private void doScanner() {
		if (StringUtil.checkNull(basePackage)) {
			throw new RuntimeException("配置文件读取失败，请配置 contextConfigLocation 参数...");
		}

		URL url = this.getClass().getClassLoader().getResource(basePackage.replaceAll("\\.", "/")); // 将包路径中的.替换成文件路径
		File dist = new File(url.getFile());
		getClassInfo(basePackage, dist); // 获取该文件夹下的所有文件和子目录中的文件
	}

	/**
	 * 获取给定文件下的所有文件及其子文件中的文件
	 * @param dist
	 */
	private void getClassInfo(String basePath, File dist) {
		// 获取该文件夹下的所有文件和子目录中的文件
		for (File fl : dist.listFiles()) {
			if (fl.isDirectory()) { // 如果是目录则递归
				getClassInfo(basePath + "." + fl.getName(),fl);
			} else if (fl.isFile()) { // 则将这个类信息保存起来
				classNames.add(basePath + "." + fl.getName().replace(".class", ""));
			}
		} 
	}

	/**
	 * 3. 初始化所有类，并将其保存到IoC容器中
	 */
	private void doInstance() {
		if (classNames.isEmpty()) {
			return;
		}

		Class<?> cls = null;
		Class<?>[] interfaces = null;
		String temp;
		String beanName = null;
		Object instance = null;
		try {
			for (String className : classNames) {
				cls = Class.forName(className);

				// 判断该类上是否有@YCController、@YCComponet注解，如果有才需要实例化，如果没有则不需要
				beanName = toLowerCase(cls.getSimpleName());
				
				// 由于自动装配的时候首先是根据类型注入，然后是根据名称注入，而名字如果用户指定了，则优先使用用户指定，否则就是将类的名字的第一个字母转成小写
				if (cls.isAnnotationPresent(YCController.class)) {
					instanceObject.put(beanName, cls.newInstance());
				} else if (cls.isAnnotationPresent(YCComponent.class)) {
					instance = cls.newInstance();
					temp = cls.getAnnotation(YCComponent.class).value(); // 如果有指定名字
					if (!StringUtil.checkNull(temp)) {
						beanName = temp;
					}
					instanceObject.put(beanName, instance);

					// 如果这个类上面有实现其他接口，为了方便到时候可以将该实现类注入给对应的接口，我们也可以在此将该实现类与接口名也关联起来
					// 到时候可以通过接口名直接找到对应的实现类注入，我这里不考虑根据名称注值。
					interfaces = cls.getInterfaces();
					for (Class<?> itfs : interfaces) { // 循环所有的类型
						instanceObject.put(itfs.getSimpleName(), instance);
					}
				} else {
					continue;
				}
			}
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 将字符串的首字母大写
	 * @param name
	 * @return
	 */
	private String toLowerCase(String name) {
		char[] chs = name.toCharArray();
		chs[0] += 32;
		return String.valueOf(chs);
	}

	/**
	 * 4. 执行依赖注入，即完成@Autowired
	 * @throws IllegalAccessException 
	 * @throws InstantiationException 
	 */
	private void doAutowired() {
		if (instanceObject.isEmpty()) {
			return;
		}

		// 将类中加了@YCAutowired注解的，把的需要的值注入进去
		Field[] fields = null;
		YCAutowired yca = null;
		String beanName = null;
		Class<?> cls = null;

		for (Entry<String, Object> entry : instanceObject.entrySet()) {
			cls = entry.getValue().getClass(); // 获取当前类
			fields = cls.getDeclaredFields(); // 获取当前类中所有的属性

			for (Field field : fields) { // 循环每个属性，判断上面是否有@YCAutowired注解
				if (!field.isAnnotationPresent(YCAutowired.class)) { // 说明此属性上没有这个注解
					continue;
				}

				yca = field.getAnnotation(YCAutowired.class); // 获取此属性上的这个注解
				beanName = yca.value().trim(); // 获取此注解上配置的名称

				// 因为属性是私有的，所有需要 强吻
				field.setAccessible(true);
				if (!StringUtil.checkNull(beanName)) { // 如果不为空，则优先根据指定的名字注入
					if (!instanceObject.containsKey(beanName)) { // 如果没有该类型
						throw new RuntimeException(cls.getName() + "." + field.getName() + " 注值失败，没有对应的实体类 " + beanName);
					}

					try {
						field.set(entry.getValue(), instanceObject.get(beanName));
						continue;
					} catch (IllegalArgumentException e) {
						e.printStackTrace();
					} catch (IllegalAccessException e) {
						e.printStackTrace();
					}
				} 

				// 根据类型注值
				beanName = field.getType().getSimpleName();

				try {
					field.set(entry.getValue(), instanceObject.get(beanName));
					continue;
				} catch (IllegalArgumentException e) {
					e.printStackTrace();
				} catch (IllegalAccessException e) {
					e.printStackTrace();
				}
			}
		}
	}

	/**
	 * 5. 构造HandlerMapping，完成URL与Method的关联
	 */
	private void initHandlerMapping() {
		if (instanceObject.isEmpty()) {
			return;
		}

		Class cls = null;
		String baseUrl = ""; // 用来存放配置在控制器类上的映射路径
		Method[] methods = null;
		String url = null;
		YCRequestMapping requestMapper = null;
		for (Entry<String, Object> entry : instanceObject.entrySet()) {
			cls = entry.getValue().getClass(); // 获取对象类信息

			if (!cls.isAnnotationPresent(YCController.class)) { // 说明不是控制器，则继续下一次循环
				continue;
			}

			requestMapper = (YCRequestMapping) cls.getAnnotation(YCRequestMapping.class); // 获取这个控制器类上的@YCRequestMapping注解对象
			if (requestMapper != null) {
				baseUrl = requestMapper.value(); // 获取配置在类上的映射路径
				
				if (!baseUrl.startsWith("/")) { // 如果用户忘记加/，则帮忙加上
					baseUrl = "/" + baseUrl;
				}
			}

			// 获取这个类中的所有方法
			methods = cls.getMethods(); // 只获取当前类的公共方法
			if (methods == null || methods.length <= 0) { // 如果当前类下没有方法，则进行下一次循环
				continue;
			}

			for (Method md : methods) { // 循环所有方法
				if (!md.isAnnotationPresent(YCRequestMapping.class)) { // 如果此方法上没有@YCRequestMapping注解，则进行下次循环
					continue;
				}

				requestMapper = md.getAnnotation(YCRequestMapping.class); // 获取当前方法上的@YCRequestMapping注解

				url = requestMapper.value();
				if (!url.startsWith("/")) { // 如果用户忘记加/，则帮忙加上
					url = "/" + url;
				}
				url = baseUrl + url;
				handlerMapper.put(url.replaceAll("/+", "/"), new HandleMapperInfo(entry.getValue(), md)); // 如果有多个 / 则替换成一个
			}
		}
	}

	/**
	 * 根据路径获取方法
	 * @param url
	 * @return
	 */
	public HandleMapperInfo getMapper(String url) {
		return handlerMapper.get(url);
	}

	public Set<String> getClassNames() {
		return classNames;
	}

	public Map<String, Object> getInstanceObject() {
		return instanceObject;
	}

	public Map<String, HandleMapperInfo> getHandlerMapper() {
		return handlerMapper;
	}
}
