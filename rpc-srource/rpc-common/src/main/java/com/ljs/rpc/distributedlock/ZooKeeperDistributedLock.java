package com.ljs.rpc.distributedlock;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;

import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooDefs;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.data.Stat;

/**
 * 基于走zookeeper实现分布式锁
 * 
 * @author Administrator
 *
 */
public class ZooKeeperDistributedLock implements Lock, Watcher {
	private ZooKeeper zk;
	// 根节点
	private static final String ROOT_LOCK = "/locks";
	// 竞争的资源
	private String lockName;
	// 前一个锁
	private String wait_lock;
	// 当前锁
	private String current_lock;
	// 计数器
	private volatile CountDownLatch countDownLatch;
	private int sessionTimeout = 30000;
	private List<Exception> exceptionList = new ArrayList<Exception>();

	/**
	 * 配置分布式锁
	 * 
	 * @param config
	 *            连接的url
	 * @param lockName
	 *            竞争资源
	 * @throws InterruptedException
	 * @throws KeeperException
	 */
	public ZooKeeperDistributedLock(String config, String lockName) {
		this.lockName = lockName;
		try {
			zk = new ZooKeeper(config, sessionTimeout, this);
//			synchronized (zk) {
				Stat stat = zk.exists(ROOT_LOCK, false);
				if (stat == null) {
					// 如果根节点不存在，则创建根节点
					zk.create(ROOT_LOCK, new byte[0], ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
				}
//			}
		} catch (IOException | KeeperException | InterruptedException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void process(WatchedEvent event) {
		if (this.countDownLatch != null) {
			countDownLatch.countDown();
		}
	}

	@Override
	public void lock() {
		if (exceptionList.size() > 0) {
			throw new LockException(exceptionList.get(0));
		}
		try {
			if (this.tryLock()) {
				System.out.println(Thread.currentThread().getName() + " " + lockName + "获得了锁");
				return;
			} else {
				// 等待锁
				waitForLock(wait_lock, sessionTimeout);
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (KeeperException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void lockInterruptibly() throws InterruptedException {

	}

	@Override
	public boolean tryLock() {
		try {
			String splitStr = "_lock_";
			if (lockName.contains(splitStr)) {
				throw new LockException("锁名有误");
			}
			// 创建临时有序节点
			current_lock = zk.create(ROOT_LOCK + "/" + lockName + splitStr, new byte[0], ZooDefs.Ids.OPEN_ACL_UNSAFE,
					CreateMode.EPHEMERAL_SEQUENTIAL);
			System.out.println(current_lock + " 已经创建");
			// 取所有子节点
			List<String> subNodes = zk.getChildren(ROOT_LOCK, false);
			// 取出所有lockName的锁
			List<String> lockObjects = new ArrayList<String>();
			for (String node : subNodes) {
				String _node = node.split(splitStr)[0];
				if (_node.equals(lockName)) {
					lockObjects.add(node);
				}
			}
			Collections.sort(lockObjects);
			System.out.println(Thread.currentThread().getName() + " 的锁是 " + current_lock);
			// 若当前节点为最小节点，则获取锁成功
			if (current_lock.equals(ROOT_LOCK + "/" + lockObjects.get(0))) {
				return true;
			}

			// 若不是最小节点，则找到自己的前一个节点
			String prevNode = current_lock.substring(current_lock.lastIndexOf("/") + 1);
			wait_lock = lockObjects.get(Collections.binarySearch(lockObjects, prevNode) - 1);
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (KeeperException e) {
			e.printStackTrace();
		}
		return false;
	}

	@Override
	public boolean tryLock(long time, TimeUnit unit) throws InterruptedException {
		return false;
	}

	@Override
	public void unlock() {
		try {
			System.out.println("释放锁 " + current_lock);
			zk.delete(current_lock, -1);
			current_lock = null;
			zk.close();
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (KeeperException e) {
			e.printStackTrace();
		}
	}

	@Override
	public Condition newCondition() {
		return null;
	}

	// 等待锁
	private boolean waitForLock(String prev, long waitTime) throws KeeperException, InterruptedException {
		Stat stat = zk.exists(ROOT_LOCK + "/" + prev, true);

		if (stat != null) {
			System.out.println(Thread.currentThread().getName() + "等待锁 " + ROOT_LOCK + "/" + prev);
			this.countDownLatch = new CountDownLatch(1);
			// 计数等待，若等到前一个节点消失，则precess中进行countDown，停止等待，获取锁
			this.countDownLatch.await(waitTime, TimeUnit.MILLISECONDS);
			this.countDownLatch = null;
			System.out.println(Thread.currentThread().getName() + " 等到了锁");
		}
		return true;
	}

	public class LockException extends RuntimeException {
		private static final long serialVersionUID = 1L;

		public LockException(String e) {
			super(e);
		}

		public LockException(Exception e) {
			super(e);
		}
	}
}
