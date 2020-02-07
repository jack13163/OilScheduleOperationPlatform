package opt.easyjmetal.problem.schedule.operation;

/**
 * 链表节点
 *
 * @param
 * @author Administrator
 */
public class ListNode {
    Operation item;
    ListNode next;

    public ListNode() {
        this.item = null;
        next = null;
    }

    public ListNode(Operation item) {
        this.item = item;
        next = null;
    }

    public ListNode(Operation item, ListNode node) {
        this.item = item;
        this.next = node;
    }

    public Operation getItem() {
        return this.item;
    }

    public void setItem(Operation item) {
        this.item = item;
    }

    /**
     * 设置下一个节点
     */
    public void setNext(ListNode next) {
        this.next = next;
    }

    /**
     * 获得下一个节点
     */
    public ListNode getNext() {
        return next;
    }
}
