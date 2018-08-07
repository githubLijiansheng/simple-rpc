package com.ljs.distributedlock.test;

import java.util.concurrent.CountDownLatch;

import com.ljs.rpc.distributedlock.ZooKeeperDistributedLock;

public class ZookeeperTest {
	static int n = 500;

	private static volatile CountDownLatch cd = new CountDownLatch(10);

	public static void secskill() {
		System.out.println(--n);
	}

	public static void main(String[] args) {

		Runnable runnable = new Runnable() {
			public void run() {

				ZooKeeperDistributedLock lock = null;
				try {
					cd.await();
					lock = new ZooKeeperDistributedLock("127.0.0.1:2181", "test1");
					lock.lock();
					secskill();
					System.out.println(Thread.currentThread().getName() + "正在运行");
				} catch (InterruptedException e) {
					e.printStackTrace();
				} finally {
					if (lock != null) {
						lock.unlock();
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
