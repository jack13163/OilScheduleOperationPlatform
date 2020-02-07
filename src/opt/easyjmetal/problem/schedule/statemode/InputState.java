package opt.easyjmetal.problem.schedule.statemode;

import opt.easyjmetal.problem.schedule.models.TankObject;

import java.io.Serializable;

public class InputState implements OilTankState, Serializable {
    private static final long serialVersionUID = 1L;

    private TankObject tankKeeper;

    public InputState(TankObject tankKeeper) {
        this.tankKeeper = tankKeeper;
    }

    @Override
    public void chargingStart() throws StateException {
        tankKeeper.Conflict = true;
        throw new StateException(
                "Tank" + tankKeeper.getTankName() + " is inputing can not do chargingStart operation.");
    }

    @Override
    public void chargingEnd() throws StateException {
        tankKeeper.setStatus(tankKeeper.stayStatue);
    }

    @Override
    public void feedingStart() throws StateException {
        tankKeeper.Conflict = true;
        throw new StateException("Tank" + tankKeeper.getTankName() + " is inputing can not do feedingStart operation.");
    }

    @Override
    public void feedingEnd() throws StateException {
        tankKeeper.Conflict = true;
        throw new StateException("Tank" + tankKeeper.getTankName() + " is inputing can not do feedingEnd operation.");
    }

    @Override
    public void stayEnd() throws StateException {
        tankKeeper.Conflict = true;
        throw new StateException("Tank" + tankKeeper.getTankName() + " is inputing can not do stayEnd operation.");
    }

    @Override
    public void hotStart() throws StateException {
        tankKeeper.Conflict = true;
        throw new StateException("Tank" + tankKeeper.getTankName() + " is inputing can not do hotStart operation.");
    }

    @Override
    public void hotEnd() throws StateException {
        tankKeeper.Conflict = true;
        throw new StateException("Tank" + tankKeeper.getTankName() + " is inputing can not do hotEnd operation.");
    }

    @Override
    public String toString() {
        return "[Tank " + tankKeeper.getTankName() + "]: InputState";
    }

}
