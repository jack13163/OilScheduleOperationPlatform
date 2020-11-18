package opt.easyjmetal.algorithm.moeas.impl.MOGWO.EntityClass;

import java.util.List;

public class Grid {//网格网格类
    private List<Double> Lower;
    private List<Double> Upper;

    public List<Double> getLower() {
        return Lower;
    }

    public void setLower(List<Double> lower) {
        Lower = lower;
    }

    public List<Double> getUpper() {
        return Upper;
    }

    public void setUpper(List<Double> upper) {
        Upper = upper;
    }
}
