package com.sim.common;

public class ThreadHelper {

	/**
	 * 获取所有的线程信息
	 * 
	 * @return
	 */
	public static Thread[] findAllThreads() {
		ThreadGroup group = Thread.currentThread().getThreadGroup();
		ThreadGroup topGroup = group;

		while (group != null) {
			topGroup = group;
			group = group.getParent();
		}

		int estimatedSize = topGroup.activeCount();
		Thread[] slackList = new Thread[estimatedSize];

		topGroup.enumerate(slackList);
		return (slackList);
	}
}
