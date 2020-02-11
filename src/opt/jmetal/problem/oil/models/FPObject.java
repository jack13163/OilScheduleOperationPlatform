package opt.jmetal.problem.oil.models;

import java.io.Serializable;

public class FPObject implements Serializable {

    private static final long serialVersionUID = 1L;

    private int oiltype;
    private double volume;
    private int site;

    public int getOiltype() {
        return oiltype;
    }

    public void setOiltype(int oiltype) {
        this.oiltype = oiltype;
    }

    public double getVolume() {
        return volume;
    }

    public void setVolume(double volume) {
        this.volume = volume;
    }

    public int getSite() {
        return site;
    }

    public void setSite(int site) {
        this.site = site;
    }

    public FPObject() {
    }
}
