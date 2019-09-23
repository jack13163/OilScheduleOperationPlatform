package com.sim.common;

public class CycleQueue<T> {
	Object[] a; // 对象数组，队列最多存储a.length-1个对象
	int front; // 队首下标
	int rear; // 队尾下标

	public CycleQueue() {
		this(100); // 调用其它构造方法
	}

	public CycleQueue(int size) {
		a = new Object[size];
		front = 0;
		rear = 0;
	}

	/**
	 * 将一个对象追加到队列尾部
	 * 
	 * @param obj 对象
	 * @return 队列满时返回false,否则返回true
	 */
	public boolean enqueue(T obj) {
		if ((rear + 1) % a.length == front) {
			return false;
		}
		a[rear] = obj;
		rear = (rear + 1) % a.length;
		return true;
	}

	/**
	 * 队列头部的第一个对象出队
	 * 
	 * @return 出队的对象，队列空时返回null
	 */
	@SuppressWarnings("unchecked")
	public T dequeue() {
		if (rear == front) {
			return null;
		}
		Object obj = a[front];
		front = (front + 1) % a.length;
		return (T) obj;
	}
}