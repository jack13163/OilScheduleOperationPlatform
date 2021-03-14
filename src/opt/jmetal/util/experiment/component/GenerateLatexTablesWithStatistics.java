package opt.jmetal.util.experiment.component;

import opt.jmetal.qualityindicator.impl.*;
import opt.jmetal.qualityindicator.impl.hypervolume.WFGHypervolume;
import opt.jmetal.util.JMetalLogger;
import opt.jmetal.util.experiment.Experiment;
import opt.jmetal.util.experiment.ExperimentComponent;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;

import java.io.*;
import java.util.*;

/**
 * This class computes a number of statistical values (mean, median, standard
 * deviation, interquartile range) from the indicator files generated after
 * executing {@link ExecuteAlgorithms} and {@link ComputeQualityIndicators}.
 * After reading the data files and calculating the values, a Latex file is
 * created containing an script that generates tables with the best and second
 * best values per indicator. The name of the file is
 * {@link Experiment #getExperimentName()}.tex, which is located by default in
 * the directory {@link Experiment #getExperimentBaseDirectory()}/latex
 * <p>
 * Although the maximum, minimum, and total number of items are also computed,
 * no tables are generated with them (this is a pending work).
 *
 * @author Antonio J. Nebro <antonio@lcc.uma.es>
 */
public class GenerateLatexTablesWithStatistics implements ExperimentComponent {
    private static final String DEFAULT_LATEX_DIRECTORY = "latex";

    private final Experiment<?, ?> experiment;

    private double[][][] mean;
    private double[][][] median;
    private double[][][] stdDeviation;
    private double[][][] iqr;
    private double[][][] max;
    private double[][][] min;
    private double[][][] numberOfValues;

    public GenerateLatexTablesWithStatistics(Experiment<?, ?> configuration) {
        this.experiment = configuration;
    }

    @Override
    public void run() throws IOException {
        List<List<List<List<Double>>>> data = readDataFromFiles();
        computeDataStatistics(data);
        generateLatexScript(data);
    }

    /**
     * 生成latex表格 【本地分析】
     *
     * @param outputDirectoryName
     * @param experimentBaseDirectory
     * @param outputParetoFrontFileName
     * @param outputParetoSetFileName
     * @param problems
     * @param algorithms
     * @param indicators
     * @param runs
     * @throws IOException
     */
    public void runAnalysis(String outputDirectoryName, String experimentBaseDirectory, String outputParetoFrontFileName, String outputParetoSetFileName,
                            List<String> problems, List<String> algorithms, List<String> indicators, int runs) throws IOException {
        List<List<List<List<Double>>>> data = new ArrayList<List<List<List<Double>>>>(indicators.size());
        JMetalLogger.logger.info("Generating Latex Tables With Statistics...");

        // 1.加载数据
        for (int i = 0; i < indicators.size(); i++) {
            // A data vector per problem
            data.add(i, new ArrayList<List<List<Double>>>());
            for (int problem = 0; problem < problems.size(); problem++) {
                data.get(i).add(problem, new ArrayList<List<Double>>());

                for (int algorithm = 0; algorithm < algorithms.size(); algorithm++) {
                    data.get(i).get(problem).add(algorithm, new ArrayList<Double>());

                    // 目录结构：result/data/algorithm/problem/indicator
                    String directory = experimentBaseDirectory + "data/" + "/" + algorithms.get(algorithm) + "/" + problems.get(problem) + "/" + indicators.get(i);
                    // Read values from data files
                    FileInputStream fis = new FileInputStream(directory);
                    InputStreamReader isr = new InputStreamReader(fis);
                    try (BufferedReader br = new BufferedReader(isr)) {
                        String aux = br.readLine();
                        while (aux != null) {
                            data.get(i).get(problem).get(algorithm).add(Double.parseDouble(aux));
                            aux = br.readLine();
                        }
                    }
                }
            }
        }

        // 2.计算统计结果
        int indicatorListSize = indicators.size();
        mean = new double[indicatorListSize][][];
        median = new double[indicatorListSize][][];
        stdDeviation = new double[indicatorListSize][][];
        iqr = new double[indicatorListSize][][];
        min = new double[indicatorListSize][][];
        max = new double[indicatorListSize][][];
        numberOfValues = new double[indicatorListSize][][];

        int problemListSize = problems.size();
        for (int indicator = 0; indicator < indicatorListSize; indicator++) {
            // A data vector per problem
            mean[indicator] = new double[problemListSize][];
            median[indicator] = new double[problemListSize][];
            stdDeviation[indicator] = new double[problemListSize][];
            iqr[indicator] = new double[problemListSize][];
            min[indicator] = new double[problemListSize][];
            max[indicator] = new double[problemListSize][];
            numberOfValues[indicator] = new double[problemListSize][];

            int algorithmListSize = algorithms.size();
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

        // 3.生成latex表格脚本
        String latexDirectoryName = experimentBaseDirectory + "/" + DEFAULT_LATEX_DIRECTORY;
        File latexOutput;
        latexOutput = new File(latexDirectoryName);
        if (!latexOutput.exists()) {
            new File(latexDirectoryName).mkdirs();
            JMetalLogger.logger.info("Creating " + latexDirectoryName + " directory");
        }
        // System.out.println("Experiment name: " + experimentName_);
        String latexFile = latexDirectoryName + "/" + "oilschedule.tex";
        String excelFilePath = experimentBaseDirectory + "excel/";
        File excelFileDir = new File(excelFilePath);
        if(!excelFileDir.exists()) {
            excelFileDir.mkdirs();
        }
        String excelFile = excelFilePath + "oilschedule.csv";
        printHeaderLatexCommands(latexFile);
        resetFile(excelFile);
        for (int i = 0; i < indicators.size(); i++) {
            printData2(latexFile, i, mean, stdDeviation, "Mean and Standard Deviation", indicators, algorithms, problems);
            printData2(latexFile, i, median, iqr, "Median and Interquartile Range", indicators, algorithms, problems);
            //输出到excel
            printExcel(excelFile, i, median, stdDeviation, "Mean and Standard Deviation", indicators, algorithms, problems);
        }
        printEndLatexCommands(latexFile);
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
     * 读取指标值
     *
     * @return
     * @throws IOException
     */
    private List<List<List<List<Double>>>> readDataFromFiles() throws IOException {
        List<List<List<List<Double>>>> data = new ArrayList<List<List<List<Double>>>>(
                experiment.getIndicatorList().size());

        for (int indicator = 0; indicator < experiment.getIndicatorList().size(); indicator++) {
            // A data vector per problem
            data.add(indicator, new ArrayList<List<List<Double>>>());
            for (int problem = 0; problem < experiment.getProblemList().size(); problem++) {
                data.get(indicator).add(problem, new ArrayList<List<Double>>());

                for (int algorithm = 0; algorithm < experiment.getAlgorithmList().size(); algorithm++) {
                    data.get(indicator).get(problem).add(algorithm, new ArrayList<Double>());

                    // 目录结构：result/data/algorithm/problem/indicator
                    String directory = experiment.getExperimentBaseDirectory();
                    directory += "/data/";
                    directory += "/" + experiment.getAlgorithmList().get(algorithm).getAlgorithmTag();
                    directory += "/" + experiment.getProblemList().get(problem).getTag();
                    directory += "/" + experiment.getIndicatorList().get(indicator).getName();
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
        int indicatorListSize = experiment.getIndicatorList().size();
        mean = new double[indicatorListSize][][];
        median = new double[indicatorListSize][][];
        stdDeviation = new double[indicatorListSize][][];
        iqr = new double[indicatorListSize][][];
        min = new double[indicatorListSize][][];
        max = new double[indicatorListSize][][];
        numberOfValues = new double[indicatorListSize][][];

        int problemListSize = experiment.getProblemList().size();
        for (int indicator = 0; indicator < indicatorListSize; indicator++) {
            // A data vector per problem
            mean[indicator] = new double[problemListSize][];
            median[indicator] = new double[problemListSize][];
            stdDeviation[indicator] = new double[problemListSize][];
            iqr[indicator] = new double[problemListSize][];
            min[indicator] = new double[problemListSize][];
            max[indicator] = new double[problemListSize][];
            numberOfValues[indicator] = new double[problemListSize][];

            int algorithmListSize = experiment.getAlgorithmList().size();
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

    private void generateLatexScript(List<List<List<List<Double>>>> data) throws IOException {
        String latexDirectoryName = experiment.getExperimentBaseDirectory() + "/" + DEFAULT_LATEX_DIRECTORY;
        File latexOutput;
        latexOutput = new File(latexDirectoryName);
        if (!latexOutput.exists()) {
            new File(latexDirectoryName).mkdirs();
            JMetalLogger.logger.info("Creating " + latexDirectoryName + " directory");
        }
        // System.out.println("Experiment name: " + experimentName_);
        String latexFile = latexDirectoryName + "/" + experiment.getExperimentName() + ".tex";
        printHeaderLatexCommands(latexFile);
        for (int i = 0; i < experiment.getIndicatorList().size(); i++) {
            printData(latexFile, i, mean, stdDeviation, "Mean and Standard Deviation");
            printData(latexFile, i, median, iqr, "Median and Interquartile Range");
        }
        printEndLatexCommands(latexFile);
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
            os.write("\\title{" + fileName + "}" + "\n");
            os.write("\\usepackage{colortbl}" + "\n");
            os.write("\\usepackage[table*]{xcolor}" + "\n");
            os.write("\\xdefinecolor{gray95}{gray}{0.65}" + "\n");
            os.write("\\xdefinecolor{gray25}{gray}{0.8}" + "\n");
            os.write("\\author{A.J. Nebro}" + "\n");
            os.write("\\begin{document}" + "\n");
            os.write("\\maketitle" + "\n");
            os.write("\\section{Tables}" + "\n");
        }
    }

    void printEndLatexCommands(String fileName) throws IOException {
        try (FileWriter os = new FileWriter(fileName, true)) {
            os.write("\\end{document}" + "\n");
        }
    }

    private void printData(String latexFile, int indicatorIndex, double[][][] centralTendency, double[][][] dispersion,
                           String caption) throws IOException {
        // Generate header of the table
        try (FileWriter os = new FileWriter(latexFile, true)) {
            os.write("\n");
            os.write("\\begin{table}" + "\n");
            os.write("\\caption{" + experiment.getIndicatorList().get(indicatorIndex).getName() + ". " + caption + "}"
                    + "\n");
            os.write("\\label{table: " + experiment.getIndicatorList().get(indicatorIndex).getName() + "}" + "\n");
            os.write("\\centering" + "\n");
            os.write("\\begin{scriptsize}" + "\n");
            os.write("\\begin{tabular}{l");

            // calculate the number of columns
            os.write(StringUtils.repeat("l", experiment.getAlgorithmList().size()));
            os.write("}\n");
            os.write("\\hline");

            // write table head
            for (int i = -1; i < experiment.getAlgorithmList().size(); i++) {
                if (i == -1) {
                    os.write(" & ");
                } else if (i == (experiment.getAlgorithmList().size() - 1)) {
                    os.write(" " + experiment.getAlgorithmList().get(i).getAlgorithmTag() + "\\\\" + "\n");
                } else {
                    os.write("" + experiment.getAlgorithmList().get(i).getAlgorithmTag() + " & ");
                }
            }
            os.write("\\hline \n");

            // write lines
            for (int i = 0; i < experiment.getProblemList().size(); i++) {
                // find the best value and second best value
                double bestCentralTendencyValue;
                double bestDispersionValue;
                double secondBestCentralTendencyValue;
                double secondBestDispersionValue;
                int bestIndex = -1;
                int secondBestIndex = -1;

                if (experiment.getIndicatorList().get(indicatorIndex).isTheLowerTheIndicatorValueTheBetter()) {
                    bestCentralTendencyValue = Double.MAX_VALUE;
                    bestDispersionValue = Double.MAX_VALUE;
                    secondBestCentralTendencyValue = Double.MAX_VALUE;
                    secondBestDispersionValue = Double.MAX_VALUE;
                    for (int j = 0; j < (experiment.getAlgorithmList().size()); j++) {
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
                    for (int j = 0; j < (experiment.getAlgorithmList().size()); j++) {
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

                os.write(experiment.getProblemList().get(i).getTag().replace("_", "\\_") + " & ");
                for (int j = 0; j < (experiment.getAlgorithmList().size() - 1); j++) {
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
                if (bestIndex == (experiment.getAlgorithmList().size() - 1)) {
                    os.write("\\cellcolor{gray95}");
                }
                if (secondBestIndex == (experiment.getAlgorithmList().size() - 1)) {
                    os.write("\\cellcolor{gray25}");
                }
                String m = String.format(Locale.ENGLISH, "%10.2e",
                        centralTendency[indicatorIndex][i][experiment.getAlgorithmList().size() - 1]);
                String s = String.format(Locale.ENGLISH, "%8.1e",
                        dispersion[indicatorIndex][i][experiment.getAlgorithmList().size() - 1]);
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
     * 本地分析
     *
     * @param latexFile
     * @param indicatorIndex
     * @param centralTendency
     * @param dispersion
     * @param caption
     * @param indicators
     * @param algorithms
     * @param problems
     * @throws IOException
     */
    private void printData2(String latexFile, int indicatorIndex, double[][][] centralTendency, double[][][] dispersion,
                            String caption, List<String> indicators, List<String> algorithms, List<String> problems) throws IOException {
        // Generate header of the table
        try (FileWriter os = new FileWriter(latexFile, true)) {
            os.write("\n");
            os.write("\\begin{table}" + "\n");
            os.write("\\caption{" + indicators.get(indicatorIndex) + ". " + caption + "}"
                    + "\n");
            os.write("\\label{table: " + indicators.get(indicatorIndex) + "}" + "\n");
            os.write("\\centering" + "\n");
            os.write("\\begin{scriptsize}" + "\n");
            os.write("\\begin{tabular}{l");

            // calculate the number of columns
            os.write(StringUtils.repeat("l", algorithms.size()));
            os.write("}\n");
            os.write("\\hline");

            // write table head
            for (int i = -1; i < algorithms.size(); i++) {
                if (i == -1) {
                    os.write(" & ");
                } else if (i == (algorithms.size() - 1)) {
                    os.write(" " + algorithms.get(i) + "\\\\" + "\n");
                } else {
                    os.write("" + algorithms.get(i) + " & ");
                }
            }
            os.write("\\hline \n");

            // write lines
            for (int i = 0; i < problems.size(); i++) {
                // find the best value and second best value
                double bestCentralTendencyValue;
                double bestDispersionValue;
                double secondBestCentralTendencyValue;
                double secondBestDispersionValue;
                int bestIndex = -1;
                int secondBestIndex = -1;

                GenericIndicator<?> indicator = null;
                switch (indicators.get(indicatorIndex)) {
                    case "HV":
                        indicator = new WFGHypervolume<>();
                        break;
                    case "IGD":
                        indicator = new InvertedGenerationalDistance<>();
                        break;
                    case "IGD+":
                        indicator = new InvertedGenerationalDistancePlus<>();
                        break;
                    case "GSPREAD":
                        indicator = new GeneralizedSpread<>();
                        break;
                    case "GD":
                        indicator = new GenerationalDistance<>();
                        break;
                    case "EP":
                        indicator = new Epsilon<>();
                        break;
                    default:
                        indicator = new WFGHypervolume<>();
                        break;
                }

                // 部分指标越大越好，这里需要判断
                if (indicator.isTheLowerTheIndicatorValueTheBetter()) {
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

                os.write(problems.get(i).replace("_", "\\_") + " & ");
                for (int j = 0; j < (algorithms.size() - 1); j++) {
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
                if (bestIndex == (algorithms.size() - 1)) {
                    os.write("\\cellcolor{gray95}");
                }
                if (secondBestIndex == (algorithms.size() - 1)) {
                    os.write("\\cellcolor{gray25}");
                }
                String m = String.format(Locale.ENGLISH, "%10.2e",
                        centralTendency[indicatorIndex][i][algorithms.size() - 1]);
                String s = String.format(Locale.ENGLISH, "%8.1e",
                        dispersion[indicatorIndex][i][algorithms.size() - 1]);
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
    private void printExcel(String excelPath, int indicatorIndex, double[][][] centralTendency, double[][][] dispersion,
                            String caption, List<String> indicators, List<String> algorithms, List<String> problems) throws IOException {
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

                GenericIndicator<?> indicator = null;
                switch (indicators.get(indicatorIndex)) {
                    case "HV":
                        indicator = new WFGHypervolume<>();
                        break;
                    case "IGD":
                        indicator = new InvertedGenerationalDistance<>();
                        break;
                    case "IGD+":
                        indicator = new InvertedGenerationalDistancePlus<>();
                        break;
                    case "GSPREAD":
                        indicator = new GeneralizedSpread<>();
                        break;
                    case "GD":
                        indicator = new GenerationalDistance<>();
                        break;
                    case "EP":
                        indicator = new Epsilon<>();
                        break;
                    default:
                        indicator = new WFGHypervolume<>();
                        break;
                }

                // 部分指标越大越好，这里需要判断
                if (indicator.isTheLowerTheIndicatorValueTheBetter()) {
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
