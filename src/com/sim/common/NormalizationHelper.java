package com.sim.common;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.ejml.data.DenseMatrix64F;
import org.uma.jmetal.util.fileinput.VectorFileUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Predicate;
import java.util.regex.Pattern;

public class NormalizationHelper {
    private static Logger logger = LogManager.getLogger(NormalizationHelper.class.getName());

    /**
     * 获取某一个问题各个目标的最大最小值
     *
     * @param problem
     * @return
     * @throws Exception
     * @deprecated 请使用FrontUtils中的getMaximumValues和getMinimumValues方法
     */
    public static double[][] getMaxMinObjectValue(String problem) {

        String Result_Path = "result/Experiment/data/";
        double[][] result = new double[2][];
        File file = new File(Result_Path);// 实验结果文件存放路径
        List<DenseMatrix64F> matrix64fs = new ArrayList<DenseMatrix64F>();

        if (file.exists()) {
            File[] algorithmsDir = file.listFiles();// 汇总各个算法的最大值和最小值
            for (File dir1 : algorithmsDir) {
                for (File dir2 : dir1.listFiles()) {
                    if (dir2.getName().equals(problem)) {// 找到问题对应的目录
                        // 过滤不需要的文件
                        Predicate<String> fileFilter = (n) -> Pattern.compile("(FUN[0-9]*.tsv)").matcher(n).matches();
                        Arrays.asList(dir2.list()).stream().filter(fileFilter).forEach(f -> {
                            String fileName = dir2.getPath() + "/" + f;
                            double[][] data = VectorFileUtils.readVectors(fileName);
                            if (data != null && data.length != 0) {
                                DenseMatrix64F dataMatrix = new DenseMatrix64F(data);
                                matrix64fs.add(dataMatrix);
                            } else {
                                logger.warn("路径 [" + fileName + "] 下的结果文件为空");
                            }
                        });
                    }
                }
            }
        } else {
            logger.fatal("请确定路径 [result/Experiment/data/] 是否存在");
            return null;
        }

        if (!matrix64fs.isEmpty()) {
            result[0] = MatrixHelper.getColMin(matrix64fs);
            result[1] = MatrixHelper.getColMax(matrix64fs);
        } else {
            logger.fatal("获取结果为空，无法执行标准化");
            return null;
        }

        return result;
    }

    /**
     * 获取某一个问题各个目标的最大最小值
     *
     * @return
     * @deprecated 请使用FrontUtils中的getMaximumValues和getMinimumValues方法
     */
    public static double[][] getMaxMinObjectValue() {

        String[] problems = getProblemList();

        double[][] result = getMaxMinObjectValue(problems[0]);
        for (int i = 1; i < problems.length; i++) {
            double[][] tmp = getMaxMinObjectValue(problems[i]);
            for (int j = 0; j < tmp[0].length; j++) {
                if (tmp[0][j] < result[0][j]) {
                    result[0][j] = tmp[0][j];
                }

                if (tmp[1][j] > result[1][j]) {
                    result[1][j] = tmp[1][j];
                }
            }
        }

        return result;
    }

    /**
     * 获取问题列表
     *
     * @return
     */
    private static String[] getProblemList() {
        // 实验结果文件存放路径
        File file = new File("result/Experiment/data/");
        return file.listFiles()[0].list();
    }


    /**
     * 获取各个目标的最大最小值
     *
     * @param outputDirectoryName
     * @param experimentBaseDirectory
     * @param outputParetoFrontFileName
     * @param outputParetoSetFileName
     * @param problems
     * @param algorithms
     * @param runs
     * @return
     */
    public static double[][] getMaxMinObjectValue(String outputDirectoryName, String experimentBaseDirectory, String outputParetoFrontFileName, String outputParetoSetFileName,
                                                  List<String> problems, List<String> algorithms, int runs) {
        double[][] result = new double[2][];
        List<DenseMatrix64F> matrix64fs = new ArrayList<DenseMatrix64F>();

        for (String algorithm : algorithms) {
            for (String problem : problems) {
                for (int i = 0; i < runs; i++) {
                    String fileName = experimentBaseDirectory + "data/" + algorithm + "/" + problem + "/"+outputParetoFrontFileName+i+".tsv";
                    double[][] data = VectorFileUtils.readVectors(fileName);
                    DenseMatrix64F dataMatrix = new DenseMatrix64F(data);
                    matrix64fs.add(dataMatrix);
                }
            }
        }

        if (!matrix64fs.isEmpty()) {
            result[0] = MatrixHelper.getColMin(matrix64fs);
            result[1] = MatrixHelper.getColMax(matrix64fs);
        } else {
            logger.fatal("获取结果为空，无法执行标准化");
            return null;
        }

        return result;
    }


    /**
     * 获取各个目标的最大最小值
     * @param outputDirectoryName
     * @param experimentBaseDirectory
     * @param fileName
     * @return
     * @deprecated 请使用FrontUtils中的getMaximumValues和getMinimumValues方法
     */
    public static double[][] getMaxMinObjectValue(String outputDirectoryName, String experimentBaseDirectory,String fileName) {
        double[][] result = new double[2][];
        List<DenseMatrix64F> matrix64fs = new ArrayList<DenseMatrix64F>();

        double[][] data = VectorFileUtils.readVectors(experimentBaseDirectory+outputDirectoryName+fileName);
        DenseMatrix64F dataMatrix = new DenseMatrix64F(data);
        matrix64fs.add(dataMatrix);

        if (!matrix64fs.isEmpty()) {
            result[0] = MatrixHelper.getColMin(matrix64fs);
            result[1] = MatrixHelper.getColMax(matrix64fs);
        } else {
            logger.fatal("获取结果为空，无法执行标准化");
            return null;
        }

        return result;
    }
}
