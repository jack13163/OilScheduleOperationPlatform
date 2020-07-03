package opt.jmetal.problem.storagetankshedule;

public class Schedule {
    private double pOilType;            //原油类型
    private double pOilVolume;          //原油体积
    private double unloadStartTime;     //调度任务开始时间
    private double unloadEndTime;       //调度任务结束时间

    /**
     * constructor
     */
    public Schedule(int pOilType, double pOilVolume, double unloadStartTime, double unloadEndTime) {
        this.pOilType = pOilType;
        this.pOilVolume = pOilVolume;
        this.unloadStartTime = unloadStartTime;
        this.unloadEndTime = unloadEndTime;
    }

    public double getpOilType() {
        return pOilType;
    }

    public double getpOilVolume() {
        return pOilVolume;
    }

    public void setpOilVolume(double pOilVolume) {
        this.pOilVolume = pOilVolume;
    }

    public double getUnloadStartTime() {
        return unloadStartTime;
    }

    public void setUnloadStartTime(double unloadStartTime) {
        this.unloadStartTime = unloadStartTime;
    }

}
