package opt.easyjmetal.problem.onlinemix;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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

        // 按照油罐进行分组
        Map<Double, List<List<Double>>> collect = backTrace.getSchedulePlan().stream()
                .sorted((e1, e2) -> (int) Math.ceil(Math.abs(e1.get(2) - e2.get(2))))   // 排序
                .collect(Collectors.groupingBy(e -> e.get(1)));                         // 整理

        for (Double d : collect.keySet()) {
            List<List<Double>> lists = collect.get(d);
            for (int i = 0; i < lists.size() - 1; i++) {
                if(lists.get(i).get(3) > lists.get(i + 1).get(2)){
                    return false;
                }
            }
        }
        return true;
    }
}
