package opt.jmetal.problem.oil.sim.common;

public class ValuesPair {

    private int last;
    private int next;

    public int getLast() {
        return last;
    }

    public void setLast(int last) {
        this.last = last;
    }

    public int getNext() {
        return next;
    }

    public void setNext(int next) {
        this.next = next;
    }

    public ValuesPair() {
    }

    public void put(int value1, int value2) {
        last = value1;
        next = value2;
    }
}
