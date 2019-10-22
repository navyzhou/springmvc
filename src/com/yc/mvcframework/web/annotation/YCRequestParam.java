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
@Target({ElementType.PARAMETER}) // 用在形参上
@Retention(RetentionPolicy.RUNTIME) // 运行时有效
public @interface YCRequestParam {
	String value() default "";
}
