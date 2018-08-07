package com.ljs.distributedlock.test;

import java.util.HashSet;
import java.util.Random;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;

import com.ljs.rpc.distributedlock.RedisDistributedLock;
import com.ljs.rpc.distributedlock.ZooKeeperDistributedLock;

import redis.clients.jedis.HostAndPort;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisCluster;

public class RedisTest {

	static int n = 500;

	private static volatile CountDownLatch cd = new CountDownLatch(10);
	private static String key = "lock";

	public static void secskill() {
		System.out.println(--n);
	}

	// private static = null;
	// {
	//
	// Set<HostAndPort> set = new HashSet<>();
	// set.add(new HostAndPort("192.168.3.207", 7001));
	// set.add(new HostAndPort("192.168.3.207", 7002));
	// set.add(new HostAndPort("192.168.3.207", 7003));
	// set.add(new HostAndPort("192.168.3.207", 7004));
	// set.add(new HostAndPort("192.168.3.207", 7005));
	// set.add(new HostAndPort("192.168.3.207", 7006));
	// JedisCluster jedis = new JedisCluster(set);
	// }

	public static void main(String[] args) {

		Runnable runnable = new Runnable() {
			public void run() {
				Set<HostAndPort> set = new HashSet<>();
				set.add(new HostAndPort("192.168.3.207", 7001));
				set.add(new HostAndPort("192.168.3.207", 7002));
				set.add(new HostAndPort("192.168.3.207", 7003));
				set.add(new HostAndPort("192.168.3.207", 7004));
				set.add(new HostAndPort("192.168.3.207", 7005));
				set.add(new HostAndPort("192.168.3.207", 7006));
				JedisCluster jedis = new JedisCluster(set);
				String randomUUID = UUID.randomUUID().toString();
				try {
					cd.await();
					while (!RedisDistributedLock.tryGetDistributedLock(jedis, key, randomUUID, 5)) {
						System.out.println(Thread.currentThread().getName() + "未获得锁一直获取");
					}
					secskill();
					System.out.println(Thread.currentThread().getName() + "获得锁正在运行");
				} catch (InterruptedException e) {
					e.printStackTrace();
				} finally {
					boolean unlock = RedisDistributedLock.releaseDistributedLock(jedis, key, randomUUID);
					if (unlock) {
						System.out.println(Thread.currentThread().getName() + "释放锁成功");
					}
				}
			}
		};

		for (int i = 0; i < 10; i++) {
			Thread t = new Thread(runnable);
			cd.countDown();
			t.start();
		}
	}

}
