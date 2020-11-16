package opt.easyjmetal.problem.onlinemix;

public class OilSchedule {
    /**
     * 判断当前系统状态是否可调度
     * @param backTrace
     * @return
     */
    public static boolean Schedulable(BackTrace backTrace) {

        // 判断当前时间是否有蒸馏塔的炼油计划即将延误
        double[] feedTime = backTrace.getFeedTime();
        for (int i = 0; i < feedTime.length; i++) {
            if(backTrace.getTime() + Oilschdule.RT > feedTime[i]){
                return false;
            }
        }

        return true;
    }
}
