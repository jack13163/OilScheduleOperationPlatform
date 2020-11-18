package opt.easyjmetal.algorithm.moeas.impl.MOGWO.EntityClass;

import java.util.List;

public class Wolf {
    private double V;
    private List<Double> Position;
    private List<Double> fit;
    private List<Double>  BestPos;
    private List<Double>  BestFit;
    private boolean       Dominated;
    private int           GridIndex;

    public int getGridIndex() {
        return GridIndex;
    }

    public void setGridIndex(int gridIndex) {
        GridIndex = gridIndex;
    }

    public List<Integer> getGridsubIndex() {
        return GridsubIndex;
    }

    public void setGridsubIndex(List<Integer> gridsubIndex) {
        GridsubIndex = gridsubIndex;
    }

    private List<Integer> GridsubIndex;

    public boolean getDominated() {
        return Dominated;
    }

    public void setDominated(boolean dominated) {
        Dominated = dominated;
    }

    public double getV() {
        return V;
    }

    public void setV(double v) {
        V = v;
    }

    public List<Double> getPosition() {
        return Position;
    }

    public void setPosition(List<Double> position) {
        Position = position;
    }

    public List<Double> getFit() {
        return fit;
    }

    public void setFit(List<Double> fit) {
        this.fit = fit;
    }

    public List<Double> getBestPos() {
        return BestPos;
    }

    public void setBestPos(List<Double> bestPos) {
        BestPos = bestPos;
    }

    public List<Double> getBestFit() {
        return BestFit;
    }

    public void setBestFit(List<Double> bestFit) {
        BestFit = bestFit;
    }
}
