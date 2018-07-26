package com.ljs.rpc.client;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 自定义注解类
 */
@Retention(RetentionPolicy.RUNTIME) // 运行时期
@Target({ ElementType.FIELD, ElementType.METHOD }) // 注解作用在字段上和方法上
public @interface AutowiredRpc {
	public String name() default "";
}
