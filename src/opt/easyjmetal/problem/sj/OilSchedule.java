package opt.easyjmetal.problem.sj;

import java.util.ArrayList;
import java.util.List;

public class OilSchedule {
    public static boolean Schedulable(int[][] FPORDER, double[] DSFR, int PIPEFR, int RT, double[][] TKS, double PET, double[] FP, List<Integer> UD){
 //判断当前系统状态是否可调度
        int DSN;
        boolean flag;
        double[][] UFDR=new double[UD.size()][2];
        try {
            for (int i = 0; i < UFDR.length; i++) {
                UFDR[i][0] = UD.get(i);
                UFDR[i][1] = DSFR[i];
            }
        }catch(Exception e){
            e.printStackTrace();
        }
        TestFun.sort(UFDR,new int[]{1});    //蒸馏塔按照炼油速率升序排序
        double available[][]=new double[DSFR.length][2];//存储各个蒸馏塔可用的油量（厂内油量）

        for(int i=0;i<UFDR.length;i++){         //计算各个蒸馏塔的可用油量
            DSN=(int)(UFDR[i][0]);   //获得蒸馏塔序号
            int COTN1=FPORDER[DSN-1][0];  //蒸馏塔DSN所炼原油型1
            int COTN2=FPORDER[DSN-1][1];  //蒸馏塔DSN所炼原油类型2
            for(int j=0;j< TKS.length;j++){
                if((int)(TKS[j][1])==COTN1){
                    if(TKS[j][4]<=PET && TKS[j][5]>PET){  //当前时刻PET。系统中各个炼油塔的总量
                        available[DSN-1][0]=available[DSN-1][0]+TKS[j][2]-(PET-TKS[j][4])*DSFR[DSN-1];
                    }
                    else{
                        available[DSN-1][0]=available[DSN-1][0]+TKS[j][2];
                    }
                }
                else if((int)(TKS[j][1])==COTN2){
                    if(TKS[j][4]<=PET && TKS[j][5]>PET){  //当前时刻PET。系统中各个炼油塔的总量
                        available[DSN-1][1]=available[DSN-1][1]+TKS[j][2]-(PET-TKS[j][4])*DSFR[DSN-1];
                    }
                    else{
                        available[DSN-1][1]=available[DSN-1][1]+TKS[j][2];
                    }
                }
            }
        }

        double[] total=new double[FPORDER.length]; //存储各个蒸馏塔可用所有原油类型总量

        for(int i=0;i<available.length;i++){
            if(FP[FPORDER[i][0]-1]!=0){
                total[i]=available[i][0];//蒸馏塔需要转运此种原油
            }else{
                total[i]=available[i][0]+available[i][1];//蒸馏塔不需要转运第一类原油
            }
        }

        //***********油量约束**************//
        int K=UFDR.length;  //蒸馏塔数量
        int[] tag=new int[K]; //初始化tag数组均为0
       for(int i=0;i<K;i++){
            double arfai=RT*UFDR[i][1]; //静置时间内蒸馏塔i需要原油量
            DSN=(int)(UFDR[i][0]);  //获得蒸馏塔编号

            if((i==K-1 && total[DSN-1]>=4*K*arfai) || total[DSN-1]>=2*K*arfai){  //统计下一个周期不需要的油罐数量
                tag[i]=1;
            }
            else if((i==K-1 && total[DSN-1]+1<2*K*arfai) || total[DSN-1]+1<K*arfai){  //判断是否满足最低油量约束
                flag=false;  //最低油量约束不足
                return flag;
            }
        }
        //********在调度周期内进行运油操作*******//
        List<Double> time=new ArrayList<Double>();
        double beta;
        double cur=PET;
        for(int i=0;i<K;i++){
            beta=available[i][0];  //场内蒸馏塔i所需要的油量
            double arfai=RT*UFDR[i][1];//驻留时间炼油量
            //转运该蒸馏塔所需要炼的原油
            if(tag[i]==0){ //油品正常转运
                time.add(cur);
                if(beta>0 && beta<K*arfai && available[i][1]>=K*arfai-beta){
                    time.add(cur+(beta/PIPEFR));
                }
                cur=cur+K*arfai/PIPEFR;
            }
        }

        //*******供油罐可用时间ideltime*******//
        double[][] TKSN=new double[TKS.length][6];
        for(int i=0;i<TKS.length;i++){
            for(int j=0;j<TKS[0].length;j++){
                TKSN[i][j]=TKS[i][j];
            }
        }
        TestFun.sort(TKSN,new int[]{5});
        double[] ideltime=new double[TKSN.length];
        for(int i=0;i<TKSN.length;i++){
            ideltime[i]=TKSN[i][5];
        }
        int count=0; //不可用的供油罐个数

        for(int i=0;i<time.size();i++){
            if(ideltime[i]>time.get(i)){
                count++;
            }
        }
        //******判断供油罐是否在需要时可以使用**********//
        if(count==0){
            flag=true;
        }else{
            flag=false;
        }
        return flag;
    }
}
