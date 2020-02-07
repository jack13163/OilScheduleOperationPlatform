package oil.analysis;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Pattern;

public class MatlabScriptHelper {
    public static void Generate5DPlotMatlabScript(String filePath) throws IOException {
        String tmp = FileUtils.readFileToString(new File("data/plot5Dtemplate.txt"), "UTF-8");
        String data = FileUtils.readFileToString(new File(filePath), "UTF-8");
        String result = Pattern.compile("@data@").matcher(tmp).replaceFirst(data);
        String dirName = filePath.substring(0, filePath.indexOf("PF")) + "Matlab/";
        String fileName = dirName + "plot5D.m";
        FileUtils.forceMkdir(new File(dirName));
        FileUtils.writeStringToFile(new File(fileName), result, "UTF-8");
    }

    public static void GenerateBoxPlotMatlabScript(String filePath) throws IOException {
        String dataStr = FileUtils.readFileToString(new File(filePath), "UTF-8");
        String data = Pattern.compile("(?<=,)([a-zA-Z_0-9]+)(?=,)").matcher(dataStr).replaceAll("'$1'");
        String tmp = FileUtils.readFileToString(new File("data/boxplottemplate.txt"), "UTF-8");
        String result = Pattern.compile("@data@").matcher(tmp).replaceFirst(data);
        String dirName = filePath.substring(0, filePath.indexOf("/")) + "/Experiment/Matlab/";
        String fileName = dirName + "runTimeCompare.m";
        FileUtils.forceMkdir(new File(dirName));
        FileUtils.writeStringToFile(new File(fileName), result, "UTF-8");
    }

    /**
     * 生成指标收敛曲线对比的Matlab脚本
     *
     * @param experimentDir 实验文件路径
     * @param problems
     * @param algorithms
     * @param indicators
     * @throws IOException
     */
    public static void GenerateConvergenceMatlabScript(String experimentDir, List<String> problems,
                                                       List<String> algorithms, List<String> indicators) throws IOException {
        // 算法对比
        for (String problem : problems) {
            for (String indicator : indicators) {
                List<List<String>> data = new LinkedList<List<String>>();
                StringBuilder stringBuilder = new StringBuilder();

                for (String algorithm : algorithms) {
                    String qualityIndicatorFile = experimentDir + "/data/" + algorithm + "/" + problem + "/" + indicator
                            + ".r0";
                    List<String> col = FileUtils.readLines(new File(qualityIndicatorFile), "UTF-8");
                    data.add(col);
                }

                int rows = data.get(0).size();
                int cols = data.size();
                for (int i = 0; i < rows; i++) {
                    for (int j = 0; j < cols; j++) {
                        stringBuilder.append(data.get(j).get(i));
                        if (j < cols - 1) {
                            stringBuilder.append(",");
                        }
                    }
                    stringBuilder.append("\n");
                }
                String tmp = FileUtils.readFileToString(new File("data/algorithmcomparetemplate.txt"), "UTF-8");
                String result = Pattern.compile("@data@").matcher(tmp).replaceFirst(stringBuilder.toString());
                result = Pattern.compile("@problem@").matcher(result).replaceFirst(problem);
                result = Pattern.compile("@indicator@").matcher(result).replaceFirst(indicator);
                result = Pattern.compile("@algorithms@").matcher(result)
                        .replaceFirst(StringUtils.join(algorithms, ","));

                String dirName = experimentDir + "Matlab/";
                String fileName = dirName + problem + "_" + indicator + ".m";
                FileUtils.forceMkdir(new File(dirName));
                FileUtils.writeStringToFile(new File(fileName), result, "UTF-8");
            }
        }

        // 策略对比
        for (String algorithm : algorithms) {
            for (String indicator : indicators) {
                List<List<String>> data = new LinkedList<List<String>>();
                StringBuilder stringBuilder = new StringBuilder();

                for (String problem : problems) {
                    String qualityIndicatorFile = experimentDir + "/data/" + algorithm + "/" + problem + "/" + indicator
                            + ".r0";
                    List<String> col = FileUtils.readLines(new File(qualityIndicatorFile), "UTF-8");
                    data.add(col);
                }

                int rows = data.get(0).size();
                int cols = data.size();
                for (int i = 0; i < rows; i++) {
                    for (int j = 0; j < cols; j++) {
                        stringBuilder.append(data.get(j).get(i));
                        if (j < cols - 1) {
                            stringBuilder.append(",");
                        }
                    }
                    stringBuilder.append("\n");
                }

                String tmp = FileUtils.readFileToString(new File("data/problemcomparetemplate.txt"), "UTF-8");
                String result = Pattern.compile("@data@").matcher(tmp).replaceFirst(stringBuilder.toString());
                result = Pattern.compile("@algorithm@").matcher(result).replaceFirst(algorithm);
                result = Pattern.compile("@indicator@").matcher(result).replaceFirst(indicator);
                result = Pattern.compile("@problems@").matcher(result).replaceFirst(StringUtils.join(problems, ","));

                String dirName = experimentDir + "Matlab/";
                String fileName = dirName + algorithm + "_" + indicator + ".m";
                FileUtils.forceMkdir(new File(dirName));
                FileUtils.writeStringToFile(new File(fileName), result, "UTF-8");
            }
        }
    }

    public static void main(String[] args) {
        try {
            Generate5DPlotMatlabScript("result/Experiment/PF/oilschedule.pf");
            GenerateBoxPlotMatlabScript("result/runTimes.csv");
            List<String> problems = Arrays.asList("EDF_PS", "EDF_TSS");
            List<String> algorithms = Arrays.asList("NSGAII", "NSGAIII");
            List<String> indicators = Arrays.asList("EP", "HV", "IGD+");
            GenerateConvergenceMatlabScript("result/Experiment/", problems, algorithms, indicators);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}