package opt.easyjmetal.algorithm.util.statistics;

import opt.easyjmetal.util.JMetalLogger;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class TTest {
    private static final String DEFAULT_LATEX_DIRECTORY = "latex";
    private static final String resultBaseDirectory_ = "result/easyjmetal";

    private List<String> indicList_;
    private List<String> algorithmNameList_;
    private List<String> problemList_;

    private double alpha_ = 0.05;// 置信度0.95

    private int[][][] better;

    public TTest(String[] algorithmNameList_,
                 String[] problemList_, String[] indicList_) {
        if (algorithmNameList_.length < 2) {
            JMetalLogger.logger.info("请至少输入两种算法");
        }
        this.indicList_ = Arrays.asList(indicList_);
        this.algorithmNameList_ = Arrays.asList(algorithmNameList_);
        this.problemList_ = Arrays.asList(problemList_);
    }

    public void run() {
        try {
            List<List<List<List<Double>>>> data = readDataFromFiles();
            computeDataStatistics(data);
            generateLatexScript(data);
        } catch (IOException ex) {
            JMetalLogger.logger.info("请生成指标值后再执行生成表格操作: " + ex.getMessage());
        }
    }

    /**
     * 读取指标值
     *
     * @return
     * @throws IOException
     */
    private List<List<List<List<Double>>>> readDataFromFiles() throws IOException {
        List<List<List<List<Double>>>> data = new ArrayList<List<List<List<Double>>>>(indicList_.size());

        for (int indicator = 0; indicator < indicList_.size(); indicator++) {
            // A data vector per problem
            data.add(indicator, new ArrayList<List<List<Double>>>());
            for (int problem = 0; problem < problemList_.size(); problem++) {
                data.get(indicator).add(problem, new ArrayList<List<Double>>());

                for (int algorithm = 0; algorithm < algorithmNameList_.size(); algorithm++) {
                    data.get(indicator).get(problem).add(algorithm, new ArrayList<Double>());

                    // 目录结构：result/data/algorithm/problem/indicator
                    String directory = resultBaseDirectory_;
                    directory += "/data/";
                    directory += "/" + algorithmNameList_.get(algorithm);
                    directory += "/" + problemList_.get(problem);
                    directory += "/" + indicList_.get(indicator);
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
        better = new int[indicatorListSize][][];

        int problemListSize = problemList_.size();
        for (int indicator = 0; indicator < indicatorListSize; indicator++) {
            // A data vector per problem
            better[indicator] = new int[problemListSize][];

            int algorithmListSize = algorithmNameList_.size();
            for (int problem = 0; problem < problemListSize; problem++) {
                better[indicator][problem] = new int[algorithmListSize];

                for (int algorithm = 1; algorithm < algorithmListSize; algorithm++) {
                    // 将algorithm[0]和algorithm[1],algorithm[2]...对比
                    double pValue = computeStatistics(data.get(indicator).get(problem).get(0),
                            data.get(indicator).get(problem).get(algorithm));

                    double mean_ours = Statistics.MeanValue(data.get(indicator).get(problem).get(0));
                    double mean_reference = Statistics.MeanValue(data.get(indicator).get(problem).get(algorithm));

                    // HV越大越好，其他指标越小越好
                    if (!indicList_.get(indicator).equals("HV")) {
                        // 判断差异性是否显著
                        if (pValue < alpha_) {
                            if (mean_ours < mean_reference) {
                                better[indicator][problem][algorithm] = 1;
                            } else if (mean_ours > mean_reference) {
                                better[indicator][problem][algorithm] = -1;
                            }
                        } else {
                            better[indicator][problem][algorithm] = 0;
                        }
                    } else {
                        // 判断差异性是否显著
                        if (pValue < alpha_) {
                            if (mean_ours > mean_reference) {
                                better[indicator][problem][algorithm] = 1;
                            } else if (mean_ours < mean_reference) {
                                better[indicator][problem][algorithm] = -1;
                            }
                        } else {
                            better[indicator][problem][algorithm] = 0;
                        }
                    }
                }
            }
        }
    }

    private void generateLatexScript(List<List<List<List<Double>>>> data) throws IOException {
        String latexDirectoryName = resultBaseDirectory_ + "/" + DEFAULT_LATEX_DIRECTORY;
        File latexOutput;
        latexOutput = new File(latexDirectoryName);
        if (!latexOutput.exists()) {
            new File(latexDirectoryName).mkdirs();
            JMetalLogger.logger.info("Creating " + latexDirectoryName + " directory");
        }
        for (int i = 0; i < indicList_.size(); i++) {
            String latexFile = latexDirectoryName + "/" + "TTest" + indicList_.get(i) + ".tex";
            printHeaderLatexCommands(latexFile);
            printData(latexFile, i, "TTest");
            printEndLatexCommands(latexFile);
        }
    }

    /**
     * Computes pValue
     *
     * @param ours
     * @param reference
     * @return
     */
    private double computeStatistics(List<Double> ours, List<Double> reference) {
        Double[] ours_array = new Double[ours.size()];
        Double[] reference_array = new Double[reference.size()];
        ours.toArray(ours_array);
        reference.toArray(reference_array);

        double pValue = Statistics.TTest(ArrayUtils.toPrimitive(ours_array), ArrayUtils.toPrimitive(reference_array));
        return pValue;
    }

    void printHeaderLatexCommands(String fileName) throws IOException {
        try (FileWriter os = new FileWriter(fileName, false)) {
            os.write("\\documentclass{article}" + "\n");
            os.write("\\usepackage{colortbl}" + "\n");
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
            os.write("\\hline\n");

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
                os.write(problemList_.get(i).replace("_", "\\_") + " & & ");
                // 比较第一个算法与其他算法的性能
                for (int j = 1; j < algorithmNameList_.size(); j++) {
                    String m;
                    if(better[indicatorIndex][i][j] == 0){
                        m = "=";
                    }else if(better[indicatorIndex][i][j] > 0){
                        m = "+";
                    }else{
                        m = "-";
                    }

                    os.write("$" + m + "$");
                    if(j < algorithmNameList_.size() - 1){
                        os.write(" & ");
                    }else{
                        os.write(" \\\\\n");
                    }
                }
            }

            // close table
            os.write("\\hline" + "\n");
            os.write("\\end{tabular}" + "\n");
            os.write("\\end{scriptsize}" + "\n");
            os.write("\\end{table}" + "\n");
        }
    }
}
