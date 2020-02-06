package com.sim.common;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ParetoHelper {

    /**
     * 非支配比较：判断输入的解是否支配参考解
     *
     * @param reference
     * @param result
     * @return
     */
    public static boolean dominanceComparison(Map<String, Double> reference, Map<String, Double> result) {
        Set<String> costNames = reference.keySet();
        List<String> list = new ArrayList<String>(costNames);
        double[] referenceValue = new double[costNames.size()];
        double[] resultValue = new double[costNames.size()];

        for (int i = 0; i < list.size(); i++) {
            String costName = list.get(i);
            referenceValue[i] = reference.get(costName);
            resultValue[i] = result.get(costName);
        }

        if (resultValue[0] == 23) {
            System.out.println("jack");
        }

        int count1 = 0, count2 = 0;
        for (int i = 0; i < referenceValue.length; i++) {
            if (referenceValue[i] > resultValue[i]) {
                count1++;
            } else if (referenceValue[i] == resultValue[i]) {
                count2++;
            } else {
                return false;
            }
        }

        if (count1 + count2 == referenceValue.length && count1 > 0) {
            return true;
        } else {
            return false;
        }
    }
}
