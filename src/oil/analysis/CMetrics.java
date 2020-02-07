package oil.analysis;

import org.apache.commons.lang3.ArrayUtils;
import opt.jmetal.util.fileinput.VectorFileUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * 计算C指标，分析不同算法或者不同策略下所得到结果的相对优劣
 */
public class CMetrics {
    public static void main(String[] args) {

        List<String> algorithms = Arrays.asList("NSGAII", "NSGAIII", "SPEA2");
        List<String> problems = Arrays.asList("EDF_TSS", "EDF_PS");

        calculateCMetrics(algorithms, problems);
    }

    /**
     * 计算C指标
     *
     * @param algorithms
     * @param problems
     */
    public static void calculateCMetrics(List<String> algorithms, List<String> problems) {
        String path = "result/Experiment/PF/";

        // 读取数据
        Map<String, Map<String, List<Double[]>>> map = new HashMap<>();
        for (int i = 0; i < algorithms.size(); i++) {
            Map<String, List<Double[]>> algo = new HashMap<>();
            map.put(algorithms.get(i), algo);
            for (int j = 0; j < problems.size(); j++) {
                String fileName = problems.get(j) + "." + algorithms.get(i) + ".rf";
                List<Double[]> data = readVectors(path + fileName);
                algo.put(problems.get(j), data);
            }
        }

        // 比较策略
        Map<String, List<Double[]>> algorithmMap = new HashMap<>();
        for (int i = 0; i < algorithms.size(); i++) {
            List<Double[]> dataSet = new ArrayList<>();
            for (int j = 0; j < problems.size(); j++) {
                dataSet.addAll(map.get(algorithms.get(i)).get(problems.get(j)));
            }
            algorithmMap.put(algorithms.get(i), dataSet);
        }

        // 比较算法
        Map<String, List<Double[]>> problemMap = new HashMap<>();
        for (int i = 0; i < problems.size(); i++) {
            List<Double[]> dataSet = new ArrayList<>();
            for (int j = 0; j < algorithms.size(); j++) {
                dataSet.addAll(map.get(algorithms.get(j)).get(problems.get(i)));
            }
            problemMap.put(problems.get(i), dataSet);
        }

        //输出C指标
        System.out.println("------------------------------算法对比C指标--------------------------");
        caculateC(algorithmMap);
        System.out.println("------------------------------问题C指标--------------------------");
        caculateC(problemMap);
    }

    /**
     * 根据数据字典计算C指标
     *
     * @param map
     */
    public static void caculateC(Map<String, List<Double[]>> map) {

        for (String i : map.keySet()) {
            for (String j : map.keySet()) {
                if (!i.equals(j)) {
                    List<Double[]> dataSet1 = map.get(i);
                    List<Double[]> dataSet2 = map.get(j);
                    System.out.print(String.format("C(%S,%S)=%f\t\t", i, j, C(dataSet1, dataSet2)));
                }
            }
            System.out.println();
        }
    }

    /**
     * 读取文件
     *
     * @param filePath
     * @return
     */
    public static List<Double[]> readVectors(String filePath) {

        List<Double[]> referenceVectors;
        String path = filePath;

        List<String> vectorStrList = null;
        try {
            vectorStrList = Files.readAllLines(Paths.get(path));
        } catch (IOException e) {
            e.printStackTrace();
        }
        referenceVectors = new ArrayList<>();
        for (int i = 0; i < vectorStrList.size(); i++) {
            String vectorStr = vectorStrList.get(i);
            String[] objectArray = vectorStr.split("[\\s,]+");// 匹配空格或者逗号
            referenceVectors.add(new Double[objectArray.length]);
            for (int j = 0; j < objectArray.length; j++) {
                referenceVectors.get(i)[j] = Double.parseDouble(objectArray[j]);
            }
        }

        return referenceVectors;
    }

    /**
     * 查找指定目录下的参考平面文件
     *
     * @param pattern
     * @param dirPath
     * @return
     */
    public static List<String> findFiles(String pattern, String dirPath) {
        File dir = new File(dirPath);
        Predicate<String> fileFilter = (n) -> Pattern.compile("(" + pattern + ".*.rf)").matcher(n).matches();
        List<String> files = Arrays.asList(dir.list()).stream().filter(fileFilter).collect(Collectors.toList());
        return files;
    }


    /**
     * 计算C指标值，并输出统计结果
     *
     * @param path
     * @param algorithm
     * @param policies
     * @param runId
     */
    public static void problemCompute(String path, String algorithm, String[] policies, int runId) {
        //数据
        Map<String, List<Double[]>> dataMap = new HashMap<>();

        for (int i = 0; i < policies.length; i++) {
            if (!dataMap.containsKey(policies[i])) {
                List<Double[]> dataList = new ArrayList<>();
                dataMap.put(policies[i], dataList);
            }

            String filePath = String.format(path, algorithm, policies[i], runId);
            if (new File(filePath).exists()) {
                double[][] data = VectorFileUtils.readVectors(filePath);

                for (int j = 0; j < data.length; j++) {
                    dataMap.get(policies[i]).add(ArrayUtils.toObject(data[j]));
                }
            } else {
                System.out.println(filePath + ": 文件不存在");
                return;
            }
        }

        //输出C指标
        System.out.println("------------------------------C指标计算结果如下--------------------------");
        caculateC(dataMap);
    }

    /**
     * 计算C指标值，并输出统计结果
     *
     * @param path
     * @param algorithm
     * @param policies
     * @param runId
     */
    public static void AlgorithmCompute(String path, String algorithm, String[] policies, int runId) {
        //数据
        Map<String, List<Double[]>> dataMap = new HashMap<>();

        for (int i = 0; i < policies.length; i++) {
            if (!dataMap.containsKey(policies[i])) {
                List<Double[]> dataList = new ArrayList<>();
                dataMap.put(policies[i], dataList);
            }

            String filePath = String.format(path, algorithm, policies[i], runId);
            if (new File(filePath).exists()) {
                double[][] data = VectorFileUtils.readVectors(filePath);

                for (int j = 0; j < data.length; j++) {
                    dataMap.get(policies[i]).add(ArrayUtils.toObject(data[j]));
                }
            } else {
                System.out.println(filePath + ": 文件不存在");
                return;
            }
        }

        //输出C指标
        System.out.println("------------------------------C指标计算结果如下--------------------------");
        for (String i : dataMap.keySet()) {
            for (String j : dataMap.keySet()) {
                if (!i.equals(j)) {
                    List<Double[]> dataSet1 = dataMap.get(i);
                    List<Double[]> dataSet2 = dataMap.get(j);
                    System.out.print(String.format("C(%S,%S)=%f\t\t", i, j, C(dataSet1, dataSet2)));
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
        return 1.0 * dominanceCount(A, B) / B.size();
    }
}
