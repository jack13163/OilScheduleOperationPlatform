package com.sim.common;

import org.apache.commons.lang3.ArrayUtils;
import org.uma.jmetal.util.fileinput.VectorFileUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 计算C指标，分析不同算法或者不同策略下所得到结果的相对优劣
 */
public class CMetrics {
    public static void main(String[] args) {
        String path = "result/Experiment/data/%s/%s/FUN%d.tsv";
        String algorithm = "NSGAII";
        String[] policies = {"EDF_TSS","EDF_PS","BT"};

        computeC(path,algorithm,policies,0);
    }

    /**
     * 计算C指标值，并输出统计结果
     * @param path
     * @param algorithm
     * @param policies
     * @param runId
     */
    public static void computeC(String path,String algorithm,String[] policies,int runId){
        //数据
        Map<String, List<Double[]>> dataMap = new HashMap<>();

        for (int i = 0; i < policies.length; i++) {
            if(!dataMap.containsKey(policies[i])){
                List<Double[]> dataList = new ArrayList<>();
                dataMap.put(policies[i],dataList);
            }

            String filePath = String.format(path,algorithm,policies[i],runId);
            if(new File(filePath).exists()) {
                double[][] data = VectorFileUtils.readVectors(filePath);

                for (int j = 0; j < data.length; j++) {
                    dataMap.get(policies[i]).add(ArrayUtils.toObject(data[j]));
                }
            }else{
                System.out.println(filePath + ": 文件不存在");
                return;
            }
        }

        //输出C指标
        System.out.println("------------------------------C指标计算结果如下--------------------------");
        for (String i : dataMap.keySet()) {
            for (String j : dataMap.keySet()) {
                if(!i.equals(j)) {
                    List<Double[]> dataSet1 = dataMap.get(i);
                    List<Double[]> dataSet2 = dataMap.get(j);
                    System.out.print(String.format("C(%S,%S)=%f\t\t",i,j,C(dataSet1,dataSet2)));
                }
            }
            System.out.println();
        }
    }

    /**
     * 非支配比较：判断输入的解是否支配参考解
     *
     * @param x
     * @param y
     * @return
     */
    public static boolean dominanceComparison(double[] x, double[] y) {

        int count1 = 0, count2 = 0;
        for (int i = 0; i < x.length; i++) {
            if (x[i] < y[i]) {
                count1++;
            } else if (x[i] == y[i]) {
                count2++;
            } else {
                return false;
            }
        }

        if (count1 + count2 == y.length && count1 > 0) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * 非支配比较：判断集合中是否存在一个解支配当前解
     *
     * @param individual
     * @param solutionSet
     * @return
     */
    public static boolean existDominanceSolution(double[] individual, List<Double[]> solutionSet) {

        boolean flag = false;
        for (int i = 0; i < solutionSet.size(); i++) {
            // 若集合中任何一个解支配当前解，则返回真
            if (dominanceComparison(ArrayUtils.toPrimitive(solutionSet.get(i)), individual)) {
                flag = true;
                break;
            }
        }

        return flag;
    }


    /**
     * 非支配比较：判断集合之间的支配计数值
     *
     * @param A
     * @param B
     * @return
     */
    public static int dominanceCount(List<Double[]> A, List<Double[]> B) {

        int count = 0;
        for (int i = 0; i < B.size(); i++) {
            if (existDominanceSolution(ArrayUtils.toPrimitive(B.get(i)), A)) {
                count++;
            }
        }

        return count;
    }

    /**
     * 非支配比较：判断集合之间的支配计数值
     *
     * @param A
     * @param B
     * @return
     */
    public static double C(List<Double[]> A, List<Double[]> B) {
        return 1.0*dominanceCount(A, B)/B.size();
    }
}
