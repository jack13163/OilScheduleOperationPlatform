package opt.easyjmetal.statistics;

import opt.easyjmetal.util.JMetalLogger;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;

import java.io.*;
import java.util.*;

/**
 * This class computes a number of statistical values (mean, median, standard
 * deviation, interquartile range) from the indicator files generated after
 * executing readDataFromFiles and computeDataStatistics.
 * After reading the data files and calculating the values, a Latex file is
 * created containing an script that generates tables with the best and second
 * best values per indicator.
 * <p>
 * Although the maximum, minimum, and total number of items are also computed,
 * no tables are generated with them (this is a pending work).
 *
 * @author Antonio J. Nebro <antonio@lcc.uma.es>
 */
public class MeanStandardDeviation {
    private static final String DEFAULT_LATEX_DIRECTORY = "mean_std";
    private String resultBaseDirectory_;

    private List<String> indicList_;
    private List<String> algorithmNameList_;
    private List<String> problemList_;

    private double[][][] mean;
    private double[][][] median;
    private double[][][] stdDeviation;
    private double[][][] iqr;
    private double[][][] max;
    private double[][][] min;
    private double[][][] numberOfValues;

    public MeanStandardDeviation(String[] algorithmNameList_,
                                 String[] problemList_,
                                 String[] indicList_,
                                 String basePath) {
        this.indicList_ = Arrays.asList(indicList_);
        this.algorithmNameList_ = Arrays.asList(algorithmNameList_);
        this.problemList_ = Arrays.asList(problemList_);
        this.resultBaseDirectory_ = basePath;
    }

    public void run() {
        try {
            List<List<List<List<Double>>>> data = readDataFromFiles();
            computeDataStatistics(data);
            generateLatexScript();
        } catch (IOException ex) {
            JMetalLogger.logger.info("请生成指标值后再执行生成表格操作: " + ex.getMessage());
        }
    }

    /**
     * Deletes a file or directory if it does exist
     *
     * @param file
     */
    private static void resetFile(String file) {
        File f = new File(file);
        if (f.exists()) {
            JMetalLogger.logger.info("Already existing file " + file);

            if (f.isDirectory()) {
                JMetalLogger.logger.info("Deleting directory " + file);
                if (f.delete()) {
                    JMetalLogger.logger.info("Directory successfully deleted.");
                } else {
                    JMetalLogger.logger.info("Error deleting directory.");
                }
            } else {
                JMetalLogger.logger.info("Deleting file " + file);
                if (f.delete()) {
                    JMetalLogger.logger.info("File successfully deleted.");
                } else {
                    JMetalLogger.logger.info("Error deleting file.");
                }
            }
        } else {
            JMetalLogger.logger.info("File " + file + " does NOT exist.");
        }
    }

    /**
     * 从指定的路径读取指标值，目录结构：basePath/indicator/problem/algorithm.indicator
     * @return
     * @throws IOException
     */
    private List<List<List<List<Double>>>> readDataFromFiles() throws IOException {
        List<List<List<List<Double>>>> data = new ArrayList<>(indicList_.size());

        for (int indicator = 0; indicator < indicList_.size(); indicator++) {
            // A data vector per problem
            data.add(indicator, new ArrayList<>());
            for (int problem = 0; problem < problemList_.size(); problem++) {
                data.get(indicator).add(problem, new ArrayList<>());

                for (int algorithm = 0; algorithm < algorithmNameList_.size(); algorithm++) {
                    data.get(indicator).get(problem).add(algorithm, new ArrayList<>());

                    // 目录结构：basePath/indicator/problem/algorithm.indicator
                    String directory = resultBaseDirectory_ + "/indicator/" + problemList_.get(problem) + "/" + algorithmNameList_.get(algorithm) + "." + indicList_.get(indicator);
                    // Read values from data files
                    FileInputStream fis = new FileInputStream(directory);
                    InputStreamReader isr = new InputStreamReader(fis);
                    try (BufferedReader br = new BufferedReader(isr)) {
                        String aux = br.readLine();
                        while (aux != null) {
                            data.get(indicator).get(problem).get(algorithm).add(Double.parseDouble(aux));
                            aux = br.readLine();
                        }
                    }
                }
            }
        }

        return data;
    }

    private void computeDataStatistics(List<List<List<List<Double>>>> data) {
        int indicatorListSize = indicList_.size();
        mean = new double[indicatorListSize][][];
        median = new double[indicatorListSize][][];
        stdDeviation = new double[indicatorListSize][][];
        iqr = new double[indicatorListSize][][];
        min = new double[indicatorListSize][][];
        max = new double[indicatorListSize][][];
        numberOfValues = new double[indicatorListSize][][];

        int problemListSize = problemList_.size();
        for (int indicator = 0; indicator < indicatorListSize; indicator++) {
            // A data vector per problem
            mean[indicator] = new double[problemListSize][];
            median[indicator] = new double[problemListSize][];
            stdDeviation[indicator] = new double[problemListSize][];
            iqr[indicator] = new double[problemListSize][];
            min[indicator] = new double[problemListSize][];
            max[indicator] = new double[problemListSize][];
            numberOfValues[indicator] = new double[problemListSize][];

            int algorithmListSize = algorithmNameList_.size();
            for (int problem = 0; problem < problemListSize; problem++) {
                mean[indicator][problem] = new double[algorithmListSize];
                median[indicator][problem] = new double[algorithmListSize];
                stdDeviation[indicator][problem] = new double[algorithmListSize];
                iqr[indicator][problem] = new double[algorithmListSize];
                min[indicator][problem] = new double[algorithmListSize];
                max[indicator][problem] = new double[algorithmListSize];
                numberOfValues[indicator][problem] = new double[algorithmListSize];

                for (int algorithm = 0; algorithm < algorithmListSize; algorithm++) {
                    Collections.sort(data.get(indicator).get(problem).get(algorithm));

                    Map<String, Double> statValues = computeStatistics(data.get(indicator).get(problem).get(algorithm));

                    mean[indicator][problem][algorithm] = statValues.get("mean");
                    median[indicator][problem][algorithm] = statValues.get("median");
                    stdDeviation[indicator][problem][algorithm] = statValues.get("stdDeviation");
                    iqr[indicator][problem][algorithm] = statValues.get("iqr");
                    min[indicator][problem][algorithm] = statValues.get("min");
                    max[indicator][problem][algorithm] = statValues.get("max");
                    numberOfValues[indicator][problem][algorithm] = statValues.get("numberOfElements").intValue();
                }
            }
        }
    }

    private void generateLatexScript() throws IOException {
        String latexDirectoryName = resultBaseDirectory_ + DEFAULT_LATEX_DIRECTORY;
        File latexOutput;
        latexOutput = new File(latexDirectoryName);
        if (!latexOutput.exists()) {
            new File(latexDirectoryName).mkdirs();
            JMetalLogger.logger.info("Creating " + latexDirectoryName + " directory");
        }
        for (int i = 0; i < indicList_.size(); i++) {
            String latexFile = latexDirectoryName + "/" + "MeanStandardDeviation" + indicList_.get(i) + ".tex";
            printHeaderLatexCommands(latexFile);
            printData(latexFile, i, mean, stdDeviation, "Mean and Standard Deviation");
            printEndLatexCommands(latexFile);
        }
    }

    /**
     * Computes the statistical values
     *
     * @param values
     * @return
     */
    private Map<String, Double> computeStatistics(List<Double> values) {
        Map<String, Double> results = new HashMap<>();

        DescriptiveStatistics stats = new DescriptiveStatistics();
        for (Double value : values) {
            stats.addValue(value);
        }

        results.put("mean", stats.getMean());
        results.put("median", stats.getPercentile(50.0));
        results.put("stdDeviation", stats.getStandardDeviation());
        results.put("iqr", stats.getPercentile(75) - stats.getPercentile(25));
        results.put("max", stats.getMax());
        results.put("min", stats.getMean());
        results.put("numberOfElements", (double) values.size());

        return results;
    }

    void printHeaderLatexCommands(String fileName) throws IOException {
        try (FileWriter os = new FileWriter(fileName, false)) {
            os.write("\\documentclass{article}" + "\n");
            os.write("\\usepackage{colortbl}" + "\n");
            os.write("\\usepackage[table*]{xcolor}" + "\n");
            os.write("\\xdefinecolor{gray95}{gray}{0.65}" + "\n");
            os.write("\\xdefinecolor{gray25}{gray}{0.8}" + "\n");
            os.write("\\usepackage{geometry}" + "\n");
            os.write("\\usepackage{pdflscape}" + "\n");
            os.write("\\geometry{a4paper,left=2cm,right=2cm,top=1cm,bottom=1cm}" + "\n");
            os.write("\\begin{document}" + "\n");
            os.write("\\thispagestyle{empty}" + "\n");// 当前页不显示页码
            os.write("\\begin{landscape}" + "\n");
        }
    }

    void printEndLatexCommands(String fileName) throws IOException {
        try (FileWriter os = new FileWriter(fileName, true)) {
            os.write("\\end{landscape}" + "\n");
            os.write("\\end{document}" + "\n");
        }
    }

    private void printData(String latexFile, int indicatorIndex,
                           double[][][] centralTendency, double[][][] dispersion,
                           String caption) throws IOException {
        // Generate header of the table
        try (FileWriter os = new FileWriter(latexFile, true)) {
            os.write("\n");
            os.write("\\begin{table}" + "\n");
            os.write("\\caption{" + indicList_.get(indicatorIndex) + ". " + caption + "}"
                    + "\n");
            os.write("\\label{table: " + indicList_.get(indicatorIndex) + "}" + "\n");
            os.write("\\centering" + "\n");
            os.write("\\begin{scriptsize}" + "\n");
            os.write("\\begin{tabular}{l");

            // calculate the number of columns
            os.write(StringUtils.repeat("l", algorithmNameList_.size()));
            os.write("}\n");
            os.write("\\hline");

            // write table head
            for (int i = -1; i < algorithmNameList_.size(); i++) {
                if (i == -1) {
                    os.write(" & ");
                } else if (i == (algorithmNameList_.size() - 1)) {
                    os.write(" " + algorithmNameList_.get(i).replace("_", "\\_") + "\\\\" + "\n");
                } else {
                    os.write("" + algorithmNameList_.get(i).replace("_", "\\_") + " & ");
                }
            }
            os.write("\\hline \n");

            // write lines
            for (int i = 0; i < problemList_.size(); i++) {
                // find the best value and second best value
                double bestCentralTendencyValue;
                double bestDispersionValue;
                double secondBestCentralTendencyValue;
                double secondBestDispersionValue;
                int bestIndex = -1;
                int secondBestIndex = -1;

                if (!indicList_.get(indicatorIndex).equals("HV")) {
                    bestCentralTendencyValue = Double.MAX_VALUE;
                    bestDispersionValue = Double.MAX_VALUE;
                    secondBestCentralTendencyValue = Double.MAX_VALUE;
                    secondBestDispersionValue = Double.MAX_VALUE;
                    for (int j = 0; j < algorithmNameList_.size(); j++) {
                        if ((centralTendency[indicatorIndex][i][j] < bestCentralTendencyValue)
                                || ((centralTendency[indicatorIndex][i][j] == bestCentralTendencyValue)
                                && (dispersion[indicatorIndex][i][j] < bestDispersionValue))) {
                            secondBestIndex = bestIndex;
                            secondBestCentralTendencyValue = bestCentralTendencyValue;
                            secondBestDispersionValue = bestDispersionValue;
                            bestCentralTendencyValue = centralTendency[indicatorIndex][i][j];
                            bestDispersionValue = dispersion[indicatorIndex][i][j];
                            bestIndex = j;
                        } else if ((centralTendency[indicatorIndex][i][j] < secondBestCentralTendencyValue)
                                || ((centralTendency[indicatorIndex][i][j] == secondBestCentralTendencyValue)
                                && (dispersion[indicatorIndex][i][j] < secondBestDispersionValue))) {
                            secondBestIndex = j;
                            secondBestCentralTendencyValue = centralTendency[indicatorIndex][i][j];
                            secondBestDispersionValue = dispersion[indicatorIndex][i][j];
                        }
                    }
                } else {
                    bestCentralTendencyValue = Double.MIN_VALUE;
                    bestDispersionValue = Double.MIN_VALUE;
                    secondBestCentralTendencyValue = Double.MIN_VALUE;
                    secondBestDispersionValue = Double.MIN_VALUE;
                    for (int j = 0; j < algorithmNameList_.size(); j++) {
                        if ((centralTendency[indicatorIndex][i][j] > bestCentralTendencyValue)
                                || ((centralTendency[indicatorIndex][i][j] == bestCentralTendencyValue)
                                && (dispersion[indicatorIndex][i][j] < bestDispersionValue))) {
                            secondBestIndex = bestIndex;
                            secondBestCentralTendencyValue = bestCentralTendencyValue;
                            secondBestDispersionValue = bestDispersionValue;
                            bestCentralTendencyValue = centralTendency[indicatorIndex][i][j];
                            bestDispersionValue = dispersion[indicatorIndex][i][j];
                            bestIndex = j;
                        } else if ((centralTendency[indicatorIndex][i][j] > secondBestCentralTendencyValue)
                                || ((centralTendency[indicatorIndex][i][j] == secondBestCentralTendencyValue)
                                && (dispersion[indicatorIndex][i][j] < secondBestDispersionValue))) {
                            secondBestIndex = j;
                            secondBestCentralTendencyValue = centralTendency[indicatorIndex][i][j];
                            secondBestDispersionValue = dispersion[indicatorIndex][i][j];
                        }
                    }
                }

                os.write(problemList_.get(i).replace("_", "\\_") + " & ");
                for (int j = 0; j < (algorithmNameList_.size() - 1); j++) {
                    if (j == bestIndex) {
                        os.write("\\cellcolor{gray95}");
                    }
                    if (j == secondBestIndex) {
                        os.write("\\cellcolor{gray25}");
                    }

                    String m = String.format(Locale.ENGLISH, "%10.2e", centralTendency[indicatorIndex][i][j]);
                    String s = String.format(Locale.ENGLISH, "%8.1e", dispersion[indicatorIndex][i][j]);
                    os.write("$" + m + "_{" + s + "}$ & ");
                }
                if (bestIndex == (algorithmNameList_.size() - 1)) {
                    os.write("\\cellcolor{gray95}");
                }
                if (secondBestIndex == (algorithmNameList_.size() - 1)) {
                    os.write("\\cellcolor{gray25}");
                }
                String m = String.format(Locale.ENGLISH, "%10.2e",
                        centralTendency[indicatorIndex][i][algorithmNameList_.size() - 1]);
                String s = String.format(Locale.ENGLISH, "%8.1e",
                        dispersion[indicatorIndex][i][algorithmNameList_.size() - 1]);
                os.write("$" + m + "_{" + s + "}$ \\\\" + "\n");
            }

            // close table
            os.write("\\hline" + "\n");
            os.write("\\end{tabular}" + "\n");
            os.write("\\end{scriptsize}" + "\n");
            os.write("\\end{table}" + "\n");
        }
    }

    /**
     * 本地分析，生成excel
     *
     * @param excelPath
     * @param indicatorIndex
     * @param centralTendency
     * @param dispersion
     * @param caption
     * @param indicators
     * @param algorithms
     * @param problems
     * @throws IOException
     */
    private void printExcel(String excelPath, int indicatorIndex,
                            double[][][] centralTendency, double[][][] dispersion,
                            String caption, List<String> indicators,
                            List<String> algorithms, List<String> problems) throws IOException {
        // Generate header of the table
        try (FileWriter os = new FileWriter(excelPath, true)) {
            os.write(indicators.get(indicatorIndex) + " " + caption + "\n");

            // 标题
            for (int i = -1; i < algorithms.size(); i++) {
                if (i == -1) {
                    // 第一个位置
                    os.write(",");
                } else if (i == (algorithms.size() - 1)) {
                    os.write(algorithms.get(i));
                } else {
                    os.write(algorithms.get(i) + ",");
                }
            }
            os.write("\n");

            // 每一行数据
            for (int i = 0; i < problems.size(); i++) {

                os.write(problems.get(i) + ",");

                // find the best value and second best value
                double bestCentralTendencyValue;
                double bestDispersionValue;
                double secondBestCentralTendencyValue;
                double secondBestDispersionValue;
                int bestIndex = -1;
                int secondBestIndex = -1;

                // 部分指标越大越好，这里需要判断
                if (!indicList_.get(indicatorIndex).equals("HV")) {
                    bestCentralTendencyValue = Double.MAX_VALUE;
                    bestDispersionValue = Double.MAX_VALUE;
                    secondBestCentralTendencyValue = Double.MAX_VALUE;
                    secondBestDispersionValue = Double.MAX_VALUE;
                    for (int j = 0; j < (algorithms.size()); j++) {
                        if ((centralTendency[indicatorIndex][i][j] < bestCentralTendencyValue)
                                || ((centralTendency[indicatorIndex][i][j] == bestCentralTendencyValue)
                                && (dispersion[indicatorIndex][i][j] < bestDispersionValue))) {
                            secondBestIndex = bestIndex;
                            secondBestCentralTendencyValue = bestCentralTendencyValue;
                            secondBestDispersionValue = bestDispersionValue;
                            bestCentralTendencyValue = centralTendency[indicatorIndex][i][j];
                            bestDispersionValue = dispersion[indicatorIndex][i][j];
                            bestIndex = j;
                        } else if ((centralTendency[indicatorIndex][i][j] < secondBestCentralTendencyValue)
                                || ((centralTendency[indicatorIndex][i][j] == secondBestCentralTendencyValue)
                                && (dispersion[indicatorIndex][i][j] < secondBestDispersionValue))) {
                            secondBestIndex = j;
                            secondBestCentralTendencyValue = centralTendency[indicatorIndex][i][j];
                            secondBestDispersionValue = dispersion[indicatorIndex][i][j];
                        }
                    }
                } else {
                    bestCentralTendencyValue = Double.MIN_VALUE;
                    bestDispersionValue = Double.MIN_VALUE;
                    secondBestCentralTendencyValue = Double.MIN_VALUE;
                    secondBestDispersionValue = Double.MIN_VALUE;
                    for (int j = 0; j < (algorithms.size()); j++) {
                        if ((centralTendency[indicatorIndex][i][j] > bestCentralTendencyValue)
                                || ((centralTendency[indicatorIndex][i][j] == bestCentralTendencyValue)
                                && (dispersion[indicatorIndex][i][j] < bestDispersionValue))) {
                            secondBestIndex = bestIndex;
                            secondBestCentralTendencyValue = bestCentralTendencyValue;
                            secondBestDispersionValue = bestDispersionValue;
                            bestCentralTendencyValue = centralTendency[indicatorIndex][i][j];
                            bestDispersionValue = dispersion[indicatorIndex][i][j];
                            bestIndex = j;
                        } else if ((centralTendency[indicatorIndex][i][j] > secondBestCentralTendencyValue)
                                || ((centralTendency[indicatorIndex][i][j] == secondBestCentralTendencyValue)
                                && (dispersion[indicatorIndex][i][j] < secondBestDispersionValue))) {
                            secondBestIndex = j;
                            secondBestCentralTendencyValue = centralTendency[indicatorIndex][i][j];
                            secondBestDispersionValue = dispersion[indicatorIndex][i][j];
                        }
                    }
                }

                //前若干列
                for (int j = 0; j < (algorithms.size() - 1); j++) {
                    if (j == bestIndex) {
                        os.write("++");
                    }
                    if (j == secondBestIndex) {
                        os.write("+");
                    }

                    String m = String.format(Locale.ENGLISH, "%.2e", centralTendency[indicatorIndex][i][j]);
                    String s = String.format(Locale.ENGLISH, "%.1e", dispersion[indicatorIndex][i][j]);
                    os.write(m + "_" + s + ",");
                }
                //最后一列
                if (bestIndex == (algorithms.size() - 1)) {
                    os.write("++");
                }
                if (secondBestIndex == (algorithms.size() - 1)) {
                    os.write("+");
                }
                String m = String.format(Locale.ENGLISH, "%.2e",
                        centralTendency[indicatorIndex][i][algorithms.size() - 1]);
                String s = String.format(Locale.ENGLISH, "%.1e",
                        dispersion[indicatorIndex][i][algorithms.size() - 1]);
                os.write(m + "_" + s + "\n");
            }

            // close table
            os.write("\n\n\n\n");
        }
    }
}
