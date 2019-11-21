package com.analysis;


import com.sim.common.MathUtil;
import org.apache.commons.lang3.ArrayUtils;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

public class RunTimeAnalysis {

    public static void main(String[] args) {

        List<String> problems = Arrays.asList("EDF_PS", "EDF_TSS");
        List<String> algorithms = Arrays.asList("NSGAII", "NSGAIII", "SPEA2");
        GenerateRunTimeReport(problems, algorithms);
    }

    /**
     * 生成运行时间
     * @param problems
     * @param algorithms
     */
    public static void GenerateRunTimeReport(List<String> problems, List<String> algorithms){

        String fileDir = "result/runTimes.csv";
        // 初始化
        Map<String, Map<String, List<Double>>> map = new TreeMap<>();
        for (int i = 0; i < problems.size(); i++) {
            Map<String, List<Double>> tmp = new TreeMap<>();
            map.put(problems.get(i), tmp);
            for (int j = 0; j < algorithms.size(); j++) {
                List<Double> data = new ArrayList<>();
                tmp.put(algorithms.get(j), data);
            }
        }

        StringBuilder stringBuilder = new StringBuilder();

        try {
            // 读取文件
            List<String> vectorStrList = Files.readAllLines(Paths.get(fileDir));
            for (String line : vectorStrList) {
                String[] tmp = line.split(",");
                map.get(tmp[2]).get(tmp[1]).add(Double.parseDouble(tmp[3]));
            }

            // 输出表头
            stringBuilder.append("," + algorithms.stream().collect(Collectors.joining(",")) + "\n");

            // 将数据临时存放在数组中，起到排序的作用，方便后面的输出
            if (!map.get(problems.get(0)).get(algorithms.get(0)).isEmpty()) {
                double[][][] data = new double[map.get(problems.get(0)).get(algorithms.get(0)).size()][problems.size()][algorithms.size()];

                for (int i = 0; i < problems.size(); i++) {
                    for (int j = 0; j < algorithms.size(); j++) {
                        List<Double> td = map.get(problems.get(i)).get(algorithms.get(j));
                        for (int k = 0; k < td.size(); k++) {
                            data[k][i][j] = td.get(k);
                        }
                    }
                }

                // 输出数组中的值
                for (int i = 0; i < data.length; i++) {
                    for (int j = 0; j < data[i].length; j++) {
                        stringBuilder.append(problems.get(j) + "," + Arrays.asList(ArrayUtils.toObject(data[i][j])).stream()
                                .map(e->e.toString())
                                .collect(Collectors.joining(",")) + "\n");
                    }
                }
            }

            // 输出到文件
            System.out.println(stringBuilder.toString());
            String csvPath = "result/Experiment/excel/";
            File file = new File(csvPath);
            if (!file.exists()) {
                file.mkdirs();// 不存在，则创建目录
            }
            String csvFile = csvPath + "times.csv";
            FileWriter writer = new FileWriter(csvFile);
            writer.write(stringBuilder.toString());
            writer.flush();
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 早期版本，已淘汰
     */
    private static void test() {
        String fileDir = "result/runTimes.csv";
        Map<String, List<Double>> map = getDataSet(fileDir);

        // 计算均值和方差
        for (String key : map.keySet()) {
            List<Double> tmp = map.get(key);
            double[] result = RunTimeAnalysis.getAverageAndStandardDevition(tmp);
            System.out.println("[" + key + "]  mean:" + result[0] + "  std:" + result[1]);
        }

        // 输出csv
        String csvPath = "result/Experiment/excel/";
        File file = new File(csvPath);
        if (!file.exists()) {
            file.mkdirs();// 不存在，则创建目录
        }
        String csvFile = csvPath + "times.csv";
        StringBuilder stringBuilder = new StringBuilder();

        // 添加标题
        String titles = map.keySet().stream().collect(Collectors.joining(","));
        stringBuilder.append(titles + "\n");

        // 行列转换
        List<List<Double>> data = new LinkedList<>();
        for (List<Double> cols : map.values()) {
            data.add(cols);
        }
        double[][] change = null;
        if (!data.isEmpty() && !data.get(0).isEmpty()) {
            change = new double[data.get(0).size()][data.size()];
            for (int i = 0; i < data.get(0).size(); i++) {
                for (int j = 0; j < data.size(); j++) {
                    change[i][j] = data.get(j).get(i);
                }
            }
        }

        // 写入文件
        for (int i = 0; i < change.length; i++) {
            for (int j = 0; j < change[i].length; j++) {
                if (j < change[i].length - 1) {
                    stringBuilder.append(change[i][j] + ",");
                } else {
                    stringBuilder.append(change[i][j] + "\n");
                }
            }
        }

        try {
            FileWriter writer = new FileWriter(csvFile);
            writer.write(stringBuilder.toString());
            writer.flush();
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 获取数据
     *
     * @param fileDir
     */
    public static Map<String, List<Double>> getDataSet(String fileDir) {
        Map<String, List<Double>> map = new TreeMap<String, List<Double>>();

        try {
            List<String> vectorStrList = Files.readAllLines(Paths.get(fileDir));

            for (String line : vectorStrList) {
                String[] tmp = line.split(",");
                String key = tmp[2] + " " + tmp[1];// 按照key准备数据

                if (!map.containsKey(key)) {
                    List<Double> dataList = new ArrayList<>();
                    map.put(key, dataList);
                }

                Double value = Double.parseDouble(tmp[3]);
                map.get(key).add(value);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return map;
    }

    /**
     * 获取均值和标准差
     *
     * @param data
     * @return
     */
    public static double[] getAverageAndStandardDevition(List<Double> data) {
        double sum = 0.0;

        for (int i = 0; i < data.size(); i++) {
            sum += data.get(i);
        }
        double average = MathUtil.round(sum / data.size(), 2);

        sum = 0.0;
        for (int i = 0; i < data.size(); i++) {
            sum += (data.get(i) - average) * (data.get(i) - average);
        }
        double std = MathUtil.round(Math.sqrt(sum / data.size()), 4);

        return new double[]{average, std};
    }
}
