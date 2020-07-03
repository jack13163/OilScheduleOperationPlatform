package opt.jmetal.problem.storagetankshedule;

import opt.jmetal.problem.impl.AbstractDoubleProblem;
import opt.jmetal.solution.DoubleSolution;

import java.util.ArrayList;
import java.util.List;

/**
 * 1、生成一定长度的编码
 * 2、传入编码，输出调度的一个完整的调度计划
 * 3、计算目标值
 */
public class Problem extends AbstractDoubleProblem {
    /**
     * 油轮卸载原油选择储油罐的顺序
     */
    private ArrayList chosedSt=new ArrayList();

    //油轮选择储油罐对应的油量
    private ArrayList chosedStVolumn=new ArrayList();
    //油轮选择储油罐对应的原油类型
    private ArrayList chosedStVolumnType=new ArrayList();

    /**
     * 被选择的油轮
     */
    private ArrayList chosedTank=new ArrayList();

    /**
     * 编码
     */
    private double[] variable;

    /**
     * 油轮集合
     */
    private Tank[] t=new Tank[2];

    /**
     * 储油罐集合
     */
    private StorageTanker[] st=new StorageTanker[12];

    /**
     * 输油管道任务队列
     */
    private Schedule[] s=new Schedule[8];

    /**
     * the oilMixSpend matrix罐底原油混合成本系数
     */
    static double[][] oilMixMatrix={{0,0,0,0,0,0,0},{0,0,11,12,13,10,15},
            {0,11,0,11,12,13,10},{0,12,11,0,10,12,13},{0,13,12,10,0,11,12},
            {0,10,13,12,11,0,11},{0,15,10,13,12,11,0}};

    /**
     * 存储每个油罐的调度信息表，加载的原油类型顺序
     */
    private ArrayList[] stoTank=new ArrayList[12];

    /**
     * 记录油轮切换的次数
     */
    private double tankSwitch=0;

    /**
     * 存储四个目标值
     */
    double[] objective=new double[4];

    /**
     * 构造器
     */
    public Problem(){
        int objs = 4;
        int vars = 20;
        setNumberOfVariables(vars);
        setNumberOfObjectives(objs);
        setName("StorageTankSchedule");

        List<Double> lowerLimit = new ArrayList<>();
        List<Double> upperLimit = new ArrayList<>();

        for (int i = 0; i < vars; i++) {
            lowerLimit.add(0.0);
            upperLimit.add(1.0);
        }

        setLowerLimit(lowerLimit);
        setUpperLimit(upperLimit);
    }

    /**
     * 根据编码执行调度计划
     * @param valiable
     */
    public void excuteSchedule(List<Double> valiable){

        this.variable=valiable.stream().mapToDouble(Double::doubleValue).toArray();
        for(int i=0;i<stoTank.length;i++){//初始化储油罐的原油类型的调度信息表
            stoTank[i]=new ArrayList();
        }

        //油轮1
        t[0]=new Tank(2950,30,1);
        int[] temp0={5};
        t[0].setToilType(temp0);
        double[] temp1={65000};
        t[0].setToilVolum(temp1);
        //油轮2
        t[1]=new Tank(3400,65,3);
        int[] temp2={2,6,3};
        t[1].setToilType(temp2);
        double[] temp3={55000,60000,135000};
        t[1].setToilVolum(temp3);

        //储油罐集合
        st[0] =new StorageTanker(0,40000,1,40000,0);
        stoTank[0].add(1);
        st[1] =new StorageTanker(1,40000,1,20000,20000);
        stoTank[1].add(1);
        st[2] =new StorageTanker(2,40000,4,40000,0);
        stoTank[2].add(4);
        st[3] =new StorageTanker(3,40000,4,40000,0);
        stoTank[3].add(4);
        st[4] =new StorageTanker(4,40000,4,22000,18000);
        stoTank[4].add(4);
        st[5] =new StorageTanker(5,40000,5,16000,24000);
        stoTank[5].add(5);
        st[6] =new StorageTanker(6,40000,0,0,40000);
        st[7] =new StorageTanker(7,40000,0,0,40000);
        st[8] =new StorageTanker(8,40000,0,0,40000);
        st[9] =new StorageTanker(9,40000,0,0,40000);
        st[10]=new StorageTanker(10,40000,0,0,40000);
        st[11]=new StorageTanker(11,40000,0,0,40000);

        //输油管道任务队列
        s[0]=new Schedule(1,24000,0,  19.2);
        s[1]=new Schedule(4,68000, 19.2, 73.6);
        s[2]=new Schedule(5,16000, 73.6,  86.4);
        s[3]=new Schedule(1,36000, 86.4,  115.2);
        s[4]=new Schedule(4,34000, 115.2, 142.4);
        s[5]=new Schedule(5,50000, 142.4,  182.4);
        s[6]=new Schedule(2,36000,  182.4, 211.2);
        s[7]=new Schedule(3,34000, 211.2,  238.4);

        preProcessing(st,s);//调度预处理
        ExcuteDecition(variable,t,st,s);//根据编码执行调度
    }

    public void preProcessing(StorageTanker[] st/*初始状态的储油罐信息*/,Schedule[] s/*输油管道的调度计划信息*/) {
        /**步骤：
         * 1、遍历输油管道的调度计划，取调度计划的第i个调度任务的原油类型以及需要的原油体积；
         * 2、遍历储油罐集合，将具有该种原油的储油罐放入一个动态数组中，并取其中最小原油体积的储油罐满足调度计划；
         * 3、更新储油罐的状态信息和输油管道调度计划的状态信息；
         * 4、判断储油罐中是否还存在输油管道需要的原油，有，回到步骤1，没有，结束；
         *
         * 注：预调度过程中，每个储油罐充油的开始和结束时间都为0
         */



        for(int i=0;i<s.length;i++){
            //选中调度计划的需要的原油的储油罐
            ArrayList<StorageTanker> al=new ArrayList<StorageTanker>();
            for(int j=0;j<st.length;j++){
                if(st[j].getStOilTpye()==s[i].getpOilType()&&st[j].getStOilVolume()>0){ //选中具有最小原油体积的储油罐
                    al.add(st[j]);
                }
            }

            //选中储油罐后开始调度
            while(al.size()>0&&s[i].getpOilVolume()>0){  //储油罐集合不为0，并且调度计划还需要油
                int flag=al.size();   //找出集合中最小原油体积的储油罐
                double cal=40001;      //找出最小原油体积
                for(int f=0;f<al.size();f++){
                    if(al.get(f).getStOilVolume()<cal){
                        cal=al.get(f).getStOilVolume();
                        flag=f;
                    }
                }
                //开始调度，判断大小关系
                double temp=0;
                temp=cal>s[i].getpOilVolume()?s[i].getpOilVolume():cal;

                //更新状态信息

                //存储储油罐调度信息的数组
                //储油罐原油体积，充油开始时间，充油结束时间，放油开始时间，放油结束时间
                double[] stMessageArray=new double[5];
                stMessageArray[0]=temp;
                stMessageArray[1]=0;
                stMessageArray[2]=0;
                stMessageArray[3]=s[i].getUnloadStartTime();
                stMessageArray[4]=s[i].getUnloadStartTime()+temp/1250;
                al.get(flag).setStOilVolume(al.get(flag).getStOilVolume()-temp);
                al.get(flag).addStMessage(stMessageArray);

                if(al.get(flag).getStOilVolume()<=0){              //如果储油罐为空，卸载该油罐
                    al.remove(flag);
                }

                //输油管道原油体积，开始时间
                s[i].setpOilVolume(s[i].getpOilVolume()-temp);
                s[i].setUnloadStartTime(s[i].getUnloadStartTime()+temp/1250);//将开始时间后移

            }
        }
    }

    /**
     * 执行预调度之后的调度过程
     * @param variable
     * @param t
     * @param st
     * @param s
     */
    public void ExcuteDecition(double[] variable/*编码*/,Tank[] t/*油轮信息*/,StorageTanker[] st/*储油罐状态信息*/,
                               Schedule[] s/*输油管道调度计划*/) {
        /**
         1、扫描油轮i，判断是否为空？空，获得下一油轮；否，得到其原油信息以及到港时刻t；
         2、扫描输油管调度计划，获得第一个计划，其原油存在油罐i中；判断该计划是否存在，存在，取该原油类型约束；否，取油轮第一个原油类型约束
         #(这里出问题啦，装满原油的储油罐也能放入可选储油罐集合）#
         3、扫描储油罐集合。以时刻t和原油类型为约束获得可用的储油罐，储油罐或为空，或为存在满足原油类型约束的储油罐（ 还要满足时间约束）
         （存在原油的储油罐的分析：标记存在原油的最晚需要时间Wt，则可供原油的时间为(Wt-t)）；
         （得到到港时间t，扫描每一个储油罐，
         A、	扫描储油罐，将 放油结束时刻 小于 到港时间t 对应的 调度记录stMessage.get() 删掉；
         B、	重新扫描储油罐集合，判断调度记录表是否为空，空，加入备选集合；否则，执行下一步判断；
         C、	判断 到港时间t 是否在充油结束最晚时间和放油开始最早时间之间（t<Wt-7)？是，加入备选储油罐集合；否，扫描下一个储油罐。）
         4、根据编码选择储油罐；
         5、确定转运的原油体积；
         A）	获得最靠近t的放油开始时刻lateTime=min(以往调度记录开始放油时刻，对应任务开始放油时刻，无对应任务的话设为246；
         B）	原油转运体积=min（储油罐空闲容积，油轮中该种类原油的体积，unloaSpeed*(lateTime-t)，输油调度任务需要的原油任务体积量））
         6、更新系统信息。
         A）	输油管道：输油任务的体积、开始时间；
         B）	油轮：油轮到港时间，对应原油体积的体积；
         C）	储油罐：原油体积，对应的充油时间段和放油时间段；
         7、判断输油调度任务集合以及油轮集合是否为空？是，结束；否，回到第一步；

         */
        if(variable.length==0||t==null||st==null||s==null){
            System.out.println("ERROR!!!PLEASE input system message");
        }

        //根据编码执行调度
        //根据油轮到刚时间搜索空的储油罐
        int m_count=0;//用于编码遍历

        /**
         * 1、搜索油轮i，得到该油轮的可用原油类型；
         */
        for(int i=0;i<t.length;i++) {

            /**
             * 2、搜索油轮i，得到该油轮中的原油信息：原油类型和原油体积；
             */

            while (t[i].getVolunm()> 0) {//油轮不为空时

                double[] tankOil = t[i].getToilVolum();
                int[] tankOilType = t[i].getToilType();

                /**
                 * 2、搜索油轮中存在其原油的输油管道的第一个调度计划
                 *      1）定位油轮原油的位置和输油管道调度计划的的位置
                 *      2）若油轮中存在输油管道不需要的原油，则按原油顺序卸载油轮中原油到储油罐中
                 */
                int tankOilflag = tankOil.length;//定位油轮中的原油类型（有问题）
                int sflag = s.length;//定位输油管道中的调度计划
                for (int s_count = s.length - 1; s_count >= 0; s_count--) {
                    for (int T_Count = 0; T_Count < tankOilType.length; T_Count++) {
                        if (s[s_count].getpOilType() == tankOilType[T_Count] && s[s_count].getpOilVolume() > 0) {//预先满足调度计划需要的原油
                            tankOilflag = T_Count; //找到油轮中原油的编号
                            sflag = s_count; //找到调度计划编号
                        }
                    }
                }

                double dischargeStartTime;//储油罐放油开始时间
                double time = t[i].getARRIVAL_TIME();//油轮到港卸油开始时间
                int needlessFlag=0;           //标志位，needlessFlag==1表示该原油输油管道不需要

                if (tankOilflag == tankOil.length) {//油轮中不存在输油管道需要的原油，取第一个原油
                    needlessFlag=1;
                    for (int T_Count = tankOil.length-1; T_Count >= 0; T_Count--) {
                        if (tankOil[T_Count] > 0) {
                            tankOilflag = T_Count;//原油种类
                        }
                    }
                    dischargeStartTime = 246;//修改之前是246
                }else{
                    dischargeStartTime =s[sflag].getUnloadStartTime();//获得输油管道需要储油罐充油的开始时间
                }

                //记录选取的原油卸载的种类
                chosedStVolumnType.add(tankOilType[tankOilflag]);

                /**
                 * 3、搜索备选储油罐集合
                 * 包括空闲的储油罐和存有同种原油tankOilType[flag0]的储油罐
                 */
                ArrayList<StorageTanker> usedST=new ArrayList<StorageTanker>();
                for(int j=0;j<st.length;j++){//遍历储油罐集合

                    //存放储油罐调度信息的动态数组
                    ArrayList stMessage=st[j].getStMessage();

                    //先根据油轮到港时间删除储油罐的调度信息
                    for(int smCon=0;smCon<stMessage.size();smCon++){
                        double[] messTemp=(double[])stMessage.get(smCon);
                        if(time>messTemp[4]){//删掉大于储油罐中已经执行完充油放油的调度信息
                            stMessage.remove(smCon);
                            smCon--;
                        }
                    }
                    //st[j].setStMessage(stMessage);

                    //存放储油罐的每个调度信息
                    double stRealtimeVolum=0;//表示油轮到港时储油罐中还存在的原油
                    double tempDischargeEarlyTime=dischargeStartTime;
                    double tempChargeEndLatestTime=0;

                    for(int smCon=0;smCon<st[j].getStMessage().size();smCon++) {//获得充油最晚结束时间和放油最早开始时间
                        double[] messTemp = (double[]) st[j].getStMessage().get(smCon);
                        if(messTemp[2]>tempChargeEndLatestTime){//获得储油罐充油最晚结束时间
                            tempChargeEndLatestTime=messTemp[2];
                        }
                        if(messTemp[3]<tempDischargeEarlyTime){//获得储油罐放油最早开始时间
                            tempDischargeEarlyTime=messTemp[3];
                        }
                        stRealtimeVolum+=messTemp[0];
                    }

                    st[j].setDischargeEarlyTime(tempDischargeEarlyTime);     //设置最早开始放油时间
                    st[j].setChargeEndLatestTime(tempChargeEndLatestTime);   //设置最晚结束充油时间
                    st[j].setStRealtimeVolum(stRealtimeVolum);               //将储油罐的实时原油体积放到罐的信息

                    //如果调度信息表为空，证明此刻该储油罐处于空闲状态
                    //如果储油罐处于充油结束时间与放油开始时间之间，并且该储油罐有同一种原油，则是可选的
                    if(st[j].getStMessage().size()==0){
                        usedST.add(st[j]);
                    }else{
                        //油轮到港时间处于最晚结束充油时间和最早开始放油时间之间，并且储油罐还有空闲的容量和存有同种原油
                        if(time>=st[j].getChargeEndLatestTime()&&time<=(st[j].getDischargeEarlyTime()-7)&&st[j].getStOilTpye()==tankOilType[tankOilflag]&&(st[j].getCapacity()-stRealtimeVolum)>=2000){
                            usedST.add(st[j]);
                        }
                    }
                }

                /**
                 * 4、根据编码选择储油罐,向下取整选择储油罐，
                 *    例如有五个储油罐，那么mnun=(int)m[0]*5;如果mnum==ast.size();mnum-=1;
                 */
                int mnum=0;
                mnum=(int)(usedST.size()*variable[m_count]);
                if(mnum==usedST.size()){mnum-=1;}
                m_count++;

                /**
                 * 5、	确定转运的原油体积；
                 *  A）获得最靠近t的放油开始时刻lateTime=min(以往调度记录开始放油时刻，对应任务开始放油时刻，无对应任务的话设为246；
                 *  B）原油转运体积=min（储油罐空闲容积，油轮中该种类原油的体积，unloaSpeed*(lateTime-t)，输油调度任务需要的原油任务体积量））
                 */
                double tranportVolum=usedST.get(mnum).getCapacity()-usedST.get(mnum).getStRealtimeVolum();//储油罐的空闲容量
                if(tranportVolum>tankOil[tankOilflag]){//比较储油罐空闲容量和油轮中该原油体的体积，取其小
                    tranportVolum= tankOil[tankOilflag];
                }
                if(tranportVolum>t[i].getUnloadSpeed()*(usedST.get(mnum).getDischargeEarlyTime()-time-6)){//与可卸油体积比较，取其小
                    tranportVolum=t[i].getUnloadSpeed()*(usedST.get(mnum).getDischargeEarlyTime()-time-6);
                }
                if(sflag!=s.length&&tranportVolum>s[sflag].getpOilVolume()){//如果油轮中存在输油管道需要的原油，还需要比较需要的原油体积，取其小
                    tranportVolum=s[sflag].getpOilVolume();
                }

                //将转运的油量信息记录下来
                chosedStVolumn.add(tranportVolum);

                /**
                 * 6、	更新系统信息。
                 *  A）	输油管道：输油任务的体积、开始时间；
                 *  B）	油轮：油轮到港时间，对应原油体积的体积；
                 *  C）	储油罐：原油体积，对应的充油时间段和放油时间段；
                 */
                //储油罐的一个转运的调度信息
                //原油体积、充油开始时间、充油结束时间、放油开始时间、放油结束时间
                double[] tempStMessage=new double[5];
                tempStMessage[0]=tranportVolum; //原油体积
                tempStMessage[1]=time;          //充油开始时间
                tempStMessage[2]=time+tranportVolum/t[i].getUnloadSpeed();  //充油结束时间
                //输油管道
                if(needlessFlag== 0){//如果油轮中存在调度计划需要的原油，则需要更新状态信息(这里还有点问题）
                    //输出输油管道信息
                    tempStMessage[3]=s[sflag].getUnloadStartTime();
                    tempStMessage[4]=s[sflag].getUnloadStartTime()+tranportVolum/1250;

                    //System.out.println("输油管道信息：任务"+flag1+" 体积："+tranportVolum+" 开始时间："+s[flag1].getUnloadStartTime()+" 结束时间：");

                    s[sflag].setpOilVolume(s[sflag].getpOilVolume()-tranportVolum);
                    s[sflag].setUnloadStartTime(s[sflag].getUnloadStartTime()+tranportVolum/1250);//将开始时间后移
                    //System.out.println(s[flag1].getUnloadStartTime());
                }else{
                    tempStMessage[3]=time+tranportVolum/t[i].getUnloadSpeed();//放油开始时间为充油结束时间
                    tempStMessage[4]=246;//放油结束时间
                }

                needlessFlag=0;

                //油轮
                tankOil[tankOilflag]-=tranportVolum;
                t[i].setToilVolum(tankOil);//原油类型以及原油体积的改变
                t[i].setARRIVAL_TIME(t[i].getARRIVAL_TIME()+tranportVolum/t[i].getUnloadSpeed());//后移到港时间
                //输出油轮信息
                System.out.println("temp3:"+tranportVolum);

                //储油罐
                usedST.get(mnum).setStOilTpye(tankOilType[tankOilflag]);//原油种类
                usedST.get(mnum).setStOilVolume(tranportVolum);   //原油体积
                usedST.get(mnum).addStMessage(tempStMessage);     //添加调度信息
                //输出储油罐信息 ·
                System.out.println("temp2:"+tranportVolum);

                //如果转运的原油体积大于0，将其加入储油罐调度记录
                //并记录油轮切换次数
                if(tranportVolum>0){
                    stoTank[usedST.get(mnum).getStNum()].add(tankOilType[tankOilflag]);
                }

                chosedSt.add(usedST.get(mnum).getStNum());
                chosedTank.add(i);
            }
        }
    }

    /**
     * 计算四个优化的目标值
     * @return
     */
    public double[] evalObj(){

        objective[0]=t[0].getARRIVAL_TIME()-30+t[1].getARRIVAL_TIME()-65;//卸油时间成本

        objective[1]=1;//油轮切换成本
        for(int i=1;i<chosedTank.size();i++){ //前后两次卸油油轮不一样或者选择的储油罐不同，算作一次切换
            if(chosedTank.get(i)!=chosedTank.get(i-1)||chosedSt.get(i)!=chosedSt.get(i-1)){
                objective[1]++;
            }
        }

        objective[2]=0;//储油罐混合成本
        for(int i=0;i<stoTank.length;i++){//i为储油罐的编号
            for(int j=1;j<stoTank[i].size();j++){
                objective[2]+=oilMixMatrix[(Integer)stoTank[i].get(j-1)][(Integer)stoTank[i].get(j)];//从成本系数矩阵中获取成本
            }
        }

        objective[3]=0;//储油罐使用成本
        for(int i=0;i<stoTank.length;i++){
            if(stoTank[i].size()>0){
                objective[3]++;
            }
        }
        return objective;//返回计算的目标值
    }

    public ArrayList getChosedSt() {
        return chosedSt;
    }

    public ArrayList getChosedStVolumn() {
        return chosedStVolumn;
    }

    public ArrayList getChosedStVolumnType() {
        return chosedStVolumnType;
    }

    @Override
    public void evaluate(DoubleSolution solution) {
        Problem problem=new Problem();
        problem.excuteSchedule(solution.getVariables());
        problem.getChosedSt();
        problem.getChosedStVolumn();
        problem.getChosedStVolumnType();
        double[] objs = problem.evalObj();
        for (int i = 0; i < objs.length; i++) {
            solution.setObjective(i, objs[i]);
        }
    }
}
