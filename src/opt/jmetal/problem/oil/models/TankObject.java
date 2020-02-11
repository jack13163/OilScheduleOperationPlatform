package opt.jmetal.problem.oil.models;

import opt.jmetal.problem.oil.statemode.*;

import java.io.Serializable;

/**
 * 供油罐
 */
public class TankObject implements OilTankState, Serializable {
    private static final long serialVersionUID = 1L;

    // state
    public OilTankState emptyStatue;
    public OilTankState inputStatue;
    public OilTankState outputStatue;
    public OilTankState stayStatue;
    public OilTankState readyStatue;
    public OilTankState hotingPipeState;
    public OilTankState hotReadyState;

    private OilTankState status;

    private String tankName;

    public boolean Conflict = false;

    private double capacity;

    private double volume;

    private int oiltype;

    private int assign;

    private boolean maintenance;

    public int getAssign() {
        return assign;
    }

    public void setAssign(int assign) {
        this.assign = assign;
    }

    public String getTankName() {
        return tankName;
    }

    public void setTankName(String tankName) {
        this.tankName = tankName;
    }

    public OilTankState getStatus() {
        return status;
    }

    public void setStatus(OilTankState status) {
        this.status = status;
    }

    public double getCapacity() {
        return capacity;
    }

    public void setCapacity(double capacity) {
        this.capacity = capacity;
    }

    public double getVolume() {
        return volume;
    }

    public void setVolume(double volume) {
        this.volume = volume;
    }

    public int getOiltype() {
        return oiltype;
    }

    public void setOiltype(int oiltype) {
        this.oiltype = oiltype;
    }

    public TankObject() {
        emptyStatue = new EmptyState(this);
        inputStatue = new InputState(this);
        outputStatue = new OutputState(this);
        stayStatue = new StayState(this);
        readyStatue = new ReadyState(this);
        hotingPipeState = new HotingPipeState(this);
        hotReadyState = new HotReadyState(this);
    }

    @Override
    public void chargingStart() throws StateException {
        this.status.chargingStart();
    }

    @Override
    public void chargingEnd() throws StateException {
        this.status.chargingEnd();
    }

    @Override
    public void feedingStart() throws StateException {
        this.status.feedingStart();
    }

    @Override
    public void feedingEnd() throws StateException {
        this.status.feedingEnd();
    }

    @Override
    public void stayEnd() throws StateException {
        this.status.stayEnd();
    }

    @Override
    public void hotStart() throws StateException {
        this.status.hotStart();
    }

    @Override
    public void hotEnd() throws StateException {
        this.status.hotEnd();
    }

    public boolean isMaintenance() {
        return maintenance;
    }

    public void setMaintenance(boolean maintenance) {
        this.maintenance = maintenance;
    }
}