package opt.jmetal.problem.storagetankshedule;

import java.util.ArrayList;

public class StorageTanker {//储油罐

    public  int    stNum;                    //储油罐编号
    private double capacity;                 //储油罐容量
    private double stOilTpye;                //原油类型
    private double stOilVolume;              //原油体积

    private double stRealtimeVolum=0;        //根据到港时间计算储油罐中有多少体积的原油
    private double chargeEndLatestTime=0;           //根据到港时间确定储油罐最晚充油结束时间
    private double dischargeEarlyTime=246;       //根据到港时间确定储油罐最早放油开始时间

    private double idleVolumn;                  //储油罐空闲的体积

    //private double[] st_message;     //储油罐单个调度任务的信息表，大小为5,
    // 分别为原油体积、充油开始时间、充油结束时间、放油开始时间、放油结束时间
    private ArrayList stMessage=new ArrayList();     //储油罐调度任务的信息表集合

    private double dischargeSpeed;    //输油管道输油速率

    //构造器
    public StorageTanker() {
        this.dischargeSpeed=1250;
    }

    public StorageTanker(int stNum,double capacity, int stOilTpye, double stOilVolume,double idleV) {
        this.stNum=stNum;
        this.capacity=capacity;
        this.stOilTpye=stOilTpye;
        this.stOilVolume=stOilVolume;
        this.dischargeSpeed=1250;
        this.idleVolumn=idleV;
    }

    public int getStNum() {
        return stNum;
    }

    public double getCapacity() {
        return capacity;
    }

    public double getDischargeSpeed() {
        return dischargeSpeed;
    }

    public double getStOilTpye() {
        return stOilTpye;
    }

    public void setStOilTpye(double stOilTpye) {
        this.stOilTpye = stOilTpye;
    }

    public double getStOilVolume() {
        return stOilVolume;
    }

    public void setStOilVolume(double stOilVolume) {
        this.stOilVolume = stOilVolume;
    }

    public ArrayList getStMessage() {
        return stMessage;
    }

    public void setStMessage(ArrayList stMessage) {
        this.stMessage.clear();//先清空原来的内容
        this.stMessage = stMessage;//再赋值新的动态数组
    }

    public void addStMessage(double[] st_message) {
        this.stMessage.add(st_message);
    }

    public double getStRealtimeVolum() {
        return stRealtimeVolum;
    }

    public void setStRealtimeVolum(double stRealtimeVolum) {
        this.stRealtimeVolum = stRealtimeVolum;
    }

    public double getDischargeEarlyTime() {
        return dischargeEarlyTime;
    }

    public void setDischargeEarlyTime(double dischargeEarlyTime) {
        this.dischargeEarlyTime = dischargeEarlyTime;
    }

    public double getChargeEndLatestTime() {
        return chargeEndLatestTime;
    }

    public void setChargeEndLatestTime(double chargeLateTime) {
        this.chargeEndLatestTime = chargeLateTime;
    }


}
