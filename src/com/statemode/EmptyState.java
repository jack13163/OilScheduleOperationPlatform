package com.statemode;

import java.io.Serializable;

import com.models.TankObject;

public class EmptyState implements OilTankState, Serializable {
    private static final long serialVersionUID = 1L;

    private TankObject tankKeeper;

    public EmptyState(TankObject tankKeeper) {
        this.tankKeeper = tankKeeper;
    }

    @Override
    public void chargingStart() {
        tankKeeper.setStatus(tankKeeper.inputStatue);
    }

    @Override
    public void chargingEnd() throws StateException {
        tankKeeper.Conflict = true;
        throw new StateException("Tank" + tankKeeper.getTankName() + " is empty can not do chargingEnd operation.");
    }

    @Override
    public void feedingStart() throws StateException {
        tankKeeper.Conflict = true;
        throw new StateException("Tank" + tankKeeper.getTankName() + " is empty can not do feedingStart operation.");
    }

    @Override
    public void feedingEnd() throws StateException {
        tankKeeper.Conflict = true;
        throw new StateException("Tank" + tankKeeper.getTankName() + " is empty can not do feedingEnd operation.");
    }

    @Override
    public void stayEnd() throws StateException {
        tankKeeper.Conflict = true;
        throw new StateException("Tank" + tankKeeper.getTankName() + " is empty can not do stayEnd operation.");
    }

    @Override
    public void hotStart() throws StateException {
        tankKeeper.Conflict = true;
        throw new StateException("Tank" + tankKeeper.getTankName() + " is empty can not do hotStart operation.");
    }

    @Override
    public void hotEnd() throws StateException {
        tankKeeper.Conflict = true;
        throw new StateException("Tank" + tankKeeper.getTankName() + " is empty can not do hotEnd operation.");
    }

    @Override
    public String toString() {
        return "[Tank " + tankKeeper.getTankName() + "]: EmptyState";
    }

}
