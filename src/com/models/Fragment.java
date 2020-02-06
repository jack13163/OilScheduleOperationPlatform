package com.models;

/**
 * 基因片段
 *
 * @author Administrator
 */
public class Fragment {
    private int ds;
    private int tank;
    private double volume;
    private double speed;

    public int getDs() {
        return ds;
    }

    public void setDs(int ds) {
        this.ds = ds;
    }

    public int getTank() {
        return tank;
    }

    public void setTank(int tank) {
        this.tank = tank;
    }

    public double getVolume() {
        return volume;
    }

    public void setVolume(double volume) {
        this.volume = volume;
    }

    public double getSpeed() {
        return speed;
    }

    public void setSpeed(double speed) {
        this.speed = speed;
    }

    public Fragment(int ds, int tank, double volume, double speed) {
        super();
        this.ds = ds;
        this.tank = tank;
        this.volume = volume;
        this.speed = speed;
    }

    public Fragment() {
        super();
    }

    @Override
    public String toString() {
        return tank + "," + ds;
    }

    /**
     * 反序列化
     *
     * @param str
     * @return
     */
    public static Fragment getFragment(String str) {
        if (str == null || str.equals("")) {
            return null;
        }

        String[] tmp = str.split(",");
        int ds = Integer.parseInt(tmp[0]);
        int tank = Integer.parseInt(tmp[1]);
        double volume = Double.parseDouble(tmp[2]);
        double speed = Double.parseDouble(tmp[3]);

        return new Fragment(ds, tank, volume, speed);
    }

}
