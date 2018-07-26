package com.ljs.rpc.sample.app;

import com.ljs.rpc.client.AutowiredRpc;
import com.ljs.rpc.client.RpcProxy;
import com.ljs.rpc.sample.client.HelloService;
import com.ljs.rpc.sample.client.Person;

public class HelloServiceTest {

	@AutowiredRpc
	HelloService helloService;

	public void helloTest1(RpcProxy rpcProxy) {

		// 调用代理的create方法，代理HelloService接口
		HelloService helloService = rpcProxy.create(HelloService.class);
		// 调用代理的方法，执行invoke
		String result = helloService.hello("World");
		System.out.println("服务端返回结果：");
		System.out.println(result);
	}

	public void helloTest2(RpcProxy rpcProxy) {
		HelloService helloService = rpcProxy.create(HelloService.class);
		String result = helloService.hello(new Person("Yong", "Huang"));
		System.out.println("服务端返回结果：");
		System.out.println(result);
	}

	public void helloTest3() {

		// 调用代理的create方法，代理HelloService接口
		// 调用代理的方法，执行invoke
		String result = helloService.hello("World");
		System.out.println("服务端返回结果：");
		System.out.println(result);
	}

	public void helloTest4() {
		String result = helloService.hello(new Person("Yong", "Huang"));
		System.out.println("服务端返回结果：");
		System.out.println(result);
	}
}
