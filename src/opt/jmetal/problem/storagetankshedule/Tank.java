package opt.jmetal.problem.storagetankshedule;

public class Tank {
    private int[]    ToilType;             //油轮中的原油类型
    private double[] ToilVolum;            //油轮装载的原油以及体积使用一个数组存储
    private double unloadSpeed;            //卸油速率
    private double ARRIVAL_TIME;           //油轮到港时间
    private double volunm=0;              //油轮中剩余原油体积
    //private double staticVolumn=0;        //油轮到港时的原油量

    /**
     * 构造器
     */
    public Tank(double unloadSpeed, double ARRIVAL_TIME,int len/*油轮中原油种类数量*/) {
        this.unloadSpeed = unloadSpeed;
        this.ARRIVAL_TIME = ARRIVAL_TIME;
        this.ToilType=new int[len];
        this.ToilVolum=new double[len];
        for(int i=0;i<this.ToilVolum.length;i++){
            this.volunm+=this.ToilVolum[i];
        }
    }

    public double getUnloadSpeed() {
        return unloadSpeed;
    }

    public double getARRIVAL_TIME() {
        return ARRIVAL_TIME;
    }

    public int[] getToilType() { return ToilType; }

    public void setToilType(int[] toilType) { ToilType = toilType; }

    public double[] getToilVolum() { return ToilVolum; }

    public void setToilVolum(double[] toilVolum) { ToilVolum = toilVolum; }

    public void setARRIVAL_TIME(double ARRIVAL_TIME) {
        this.ARRIVAL_TIME = ARRIVAL_TIME;
    }

    public double getVolunm() {
        this.volunm=0;
        for(int i=0;i<this.ToilVolum.length;i++){
            this.volunm+=this.ToilVolum[i];
        }
        return volunm;
    }
}
