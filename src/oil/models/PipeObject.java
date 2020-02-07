package oil.models;

import java.io.Serializable;

public class PipeObject implements Serializable {
    private static final long serialVersionUID = 1L;

    private double[] chargingSpeed;
    private double[] cost;
    private double vol;

    public double[] getChargingSpeed() {
        return chargingSpeed;
    }

    public void setChargingSpeed(double[] chargingSpeed) {
        this.chargingSpeed = chargingSpeed;
    }

    public double[] getCost() {
        return cost;
    }

    public void setCost(double[] cost) {
        this.cost = cost;
    }

    public double getVol() {
        return vol;
    }

    public void setVol(double vol) {
        this.vol = vol;
    }

    public PipeObject() {
    }

}
