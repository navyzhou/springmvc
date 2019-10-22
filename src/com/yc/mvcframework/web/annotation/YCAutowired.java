package com.yc.mvcframework.web.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 源辰信息
 * @author navy
 * @2019年10月18日
 */
@Target(ElementType.FIELD) // 用在属性上
@Retention(RetentionPolicy.RUNTIME) // 运行时有效
public @interface YCAutowired {
	String value() default "";
}
