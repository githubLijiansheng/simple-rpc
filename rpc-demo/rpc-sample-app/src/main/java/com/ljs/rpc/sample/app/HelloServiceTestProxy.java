package com.ljs.rpc.sample.app;

import java.lang.reflect.Field;

import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.ljs.rpc.client.AutowiredRpc;
import com.ljs.rpc.client.RpcProxy;

public class HelloServiceTestProxy {

	public static void main(String[] args) {
		@SuppressWarnings("resource")
		ClassPathXmlApplicationContext applicationContext = new ClassPathXmlApplicationContext("spring.xml");

		// HelloServiceTest helloServiceTest = new HelloServiceTest();
		// RpcProxy rpcProxy = (RpcProxy)
		RpcProxy proxy = (RpcProxy) applicationContext.getBean("rpcProxy");
		// helloServiceTest.helloTest1(rpcProxy);
		// helloServiceTest.helloTest2(rpcProxy);

		HelloServiceTest helloServerTest = (HelloServiceTest) applicationContext.getBean("helloServiceTest");
		// 属性上有注解
		Field[] fields = helloServerTest.getClass().getDeclaredFields();
		for (Field field : fields) {
			if (field.isAnnotationPresent(AutowiredRpc.class)) {
				field.setAccessible(true);
				Class<?> type = field.getType();
				Object value = proxy.create(type);
				try {
					field.set(helloServerTest, value);
				} catch (IllegalArgumentException | IllegalAccessException e) {
					e.printStackTrace();
				}
			}
		}
		helloServerTest.helloTest3();
		helloServerTest.helloTest4();
	}
}
