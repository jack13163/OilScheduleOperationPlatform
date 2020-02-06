package com.sim.operation;

/**
 * 有序列表
 *
 * @param
 * @author Administrator
 */
public class OrderedOperationList {
    // 头指针
    private ListNode header;

    public ListNode getHeader() {
        return header;
    }

    public void setHeader(ListNode node) {
        this.header = node;
    }

    public Object lock = new Object();

    /**
     * 加入元素
     *
     * @param target
     */
    public void add(Operation target) {

        synchronized (lock) {

            ListNode pre = null; // 当前节点
            ListNode cur = header; // 当前节点

            while (cur != null && target.getStart() - cur.getItem().getStart() > 0) {

                // 指针向后移动
                pre = cur;
                cur = cur.getNext();
            }

            if (pre == null) {
                header = new ListNode(target, header);
            } else {
                // 在pre和cur之间加入一个节点
                pre.setNext(new ListNode(target, cur));
            }
        }
    }

    /**
     * 移除元素
     *
     * @param target
     * @return
     */
    public Operation remove() {
        synchronized (lock) {

            ListNode node = header;
            if (header != null) {
                header = header.getNext();
                return node.getItem();
            } else {
                return null;
            }
        }
    }

    /**
     * 判断是否包含
     *
     * @param target
     * @return
     */
    public boolean contains(Operation target) {
        ListNode node = header;
        while (node != header) {

            if (target.getStart() - node.getItem().getStart() > 0)
                return false; // 如果target小于有序表的最小值，则中断查询
            else if (target.getStart() == node.getItem().getStart())
                return true;// 找到目标，返回ture
            else {
                node = node.getNext();
            }
        }
        return false;
    }

    /**
     * 获取第一个元素
     *
     * @param target
     * @return
     */
    public Operation peek() {
        if (header != null) {
            return header.getItem();
        } else {
            return null;
        }
    }

    /**
     * 判断是否为空
     *
     * @return
     */
    public boolean isEmpty() {
        if (header == null) {
            return true;
        } else {
            return false;
        }
    }

    @Override
    public String toString() {

        StringBuilder stringBuilder = new StringBuilder("OrderedOperationList [");
        ListNode node = header;
        while (node != null) {
            stringBuilder.append(node.getItem().getStart());
            if (node.getNext() != null) {
                stringBuilder.append(", ");
            }
            node = node.getNext();
        }
        stringBuilder.append("]");

        return stringBuilder.toString();
    }

}
