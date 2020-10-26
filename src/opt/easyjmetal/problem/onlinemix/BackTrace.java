package opt.easyjmetal.problem.onlinemix;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.Queue;

public class BackTrace implements Serializable {
    private boolean Flag;
    private int step;
    private double time;        // 当前时刻
    private double[] feedTime;  // 炼油结束时刻
    private double[] x;
    private Map<String, Queue<Oilschdule.KeyValue>> FP;
    private Object[][] TKS;
    private List<List<Double>> schedulePlan;

    public BackTrace(double[] x, int step, double time, double[] feedTime,
                     Map<String, Queue<Oilschdule.KeyValue>> FP,
                     Object[][] TKS,
                     List<List<Double>> schedulePlan) {
        this.step = step;
        this.x = x;
        this.FP = FP;
        this.time = time;
        this.feedTime = feedTime;
        this.TKS = TKS;
        this.schedulePlan = schedulePlan;
    }

    public boolean isFlag() {
        return Flag;
    }

    public double[] getFeedTime() {
        return feedTime;
    }

    public void setFeedTime(double[] feedTime) {
        this.feedTime = feedTime;
    }

    public double getTime() {
        return time;
    }

    public void setTime(double time) {
        this.time = time;
    }

    public boolean getFlag() {
        return Flag;
    }

    public void setFlag(boolean flag) {
        Flag = flag;
    }

    public void setStep(int step) {
        this.step = step;
    }

    public void setX(double[] x) {
        this.x = x;
    }

    public void setFP(Map<String, Queue<Oilschdule.KeyValue>> FP) {
        this.FP = FP;
    }

    public void setTKS(Object[][] TKS) {
        this.TKS = TKS;
    }

    public void setSchedulePlan(List<List<Double>> schedulePlan) {
        this.schedulePlan = schedulePlan;
    }

    public int getStep() {
        return step;
    }

    public double[] getX() {
        return x;
    }

    public Map<String, Queue<Oilschdule.KeyValue>> getFP() {
        return FP;
    }

    public Object[][] getTKS() {
        return TKS;
    }

    public List<List<Double>> getSchedulePlan() {
        return schedulePlan;
    }
}
