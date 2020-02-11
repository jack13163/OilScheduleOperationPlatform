package opt.jmetal.problem.oil.sim.common;

import opt.jmetal.problem.oil.sim.operation.OperationType;

public class SimulationMessage {

    // 说明来者何人
    private String tank;
    // 说明所为何事
    private OperationType type;
    // 来自哪里
    private int ds;

    public String getTank() {
        return tank;
    }

    public void setTank(String tank) {
        this.tank = tank;
    }

    public OperationType getType() {
        return type;
    }

    public void setType(OperationType type) {
        this.type = type;
    }

    public int getDS() {
        return ds;
    }

    public void setDS(int ds) {
        this.ds = ds;
    }
}
