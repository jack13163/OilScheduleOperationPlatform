package org.uma.jmetal.util.experiment.util;

import com.sim.common.FileHelper;
import com.sim.common.ListHelper;
import com.sim.common.MathUtil;
import org.uma.jmetal.algorithm.Algorithm;
import org.uma.jmetal.solution.Solution;
import org.uma.jmetal.util.JMetalLogger;
import org.uma.jmetal.util.fileoutput.SolutionListOutput;
import org.uma.jmetal.util.fileoutput.impl.DefaultFileOutputContext;

import java.io.File;
import java.util.List;

/**
 * Class defining tasks for the execution of algorithms in parallel.
 *
 * @author Antonio J. Nebro <antonio@lcc.uma.es>
 */
public class ExperimentAlgorithm<S extends Solution<?>, Result extends List<S>> {
    private Algorithm<Result> algorithm;
    private String algorithmTag;
    private String problemTag;
    private String referenceParetoFront;
    private int runId;
    private double runTime;// 运行时间
    public static final String TIMEFILE_PATH = "result/runTimes.csv";// 运行时间文件保存的路

    /**
     * Constructor
     */
    public ExperimentAlgorithm(Algorithm<Result> algorithm, String algorithmTag, ExperimentProblem<S> problem,
                               int runId) {
        this.algorithm = algorithm;
        this.algorithmTag = algorithmTag;
        this.problemTag = problem.getTag();
        this.referenceParetoFront = problem.getReferenceFront();
        this.runId = runId;
    }

    public ExperimentAlgorithm(Algorithm<Result> algorithm, ExperimentProblem<S> problem, int runId) {
        this(algorithm, algorithm.getName(), problem, runId);
    }

    public void runAlgorithm(String experimentDir) {
        // 判断目录是否存在，若不存在则创建
        String outputDirectoryName = experimentDir + "/data/" + algorithmTag + "/" + problemTag;
        File outputDirectory = new File(outputDirectoryName);
        if (!outputDirectory.exists()) {
            boolean result = new File(outputDirectoryName).mkdirs();
            if (result) {
                JMetalLogger.logger.info("Creating " + outputDirectoryName);
            } else {
                JMetalLogger.logger.severe("Creating " + outputDirectoryName + " failed");
            }
        }

        String funFile = outputDirectoryName + "/FUN" + runId + ".tsv";
        String varFile = outputDirectoryName + "/VAR" + runId + ".tsv";
        JMetalLogger.logger.info(" Running algorithm: " + algorithmTag + ", problem: " + problemTag + ", run: " + runId
                + ", funFile: " + funFile);

        // 1.记录开始时间
        long startTime = System.currentTimeMillis();
        // 2.运行算法
        algorithm.run();
        // 3.记录结束时间
        long endTime = System.currentTimeMillis();
        // 4.计算耗时
        runTime = MathUtil.round(MathUtil.divide(endTime - startTime, 1000), 2);
        JMetalLogger.logger.info("No." + runId + " " + algorithmTag + " on " + problemTag + " speed: " + runTime + "s");
        FileHelper.appendContentToFile(TIMEFILE_PATH, runId + "," + algorithmTag + "," + problemTag + "," + runTime);

        // 5.getResult获取非支配解，并以逗号分割输出到var和fun文件
        Result population = algorithm.getResult();
        new SolutionListOutput(population).setVarFileOutputContext(new DefaultFileOutputContext(varFile, ","))
                .setFunFileOutputContext(new DefaultFileOutputContext(funFile, ",")).print();

        // 6.将solutions保存到文件，并释放
        String solutionFunList = outputDirectoryName + "/FUN" + runId + ".list";
        JMetalLogger.logger.info("Saving solutions fun to file: " + solutionFunList);

        // 保存到本地
        ListHelper.saveListToFile(solutionFunList, algorithm.getSolutions());

        algorithm.clearSolutions();
    }

    public Algorithm<Result> getAlgorithm() {
        return algorithm;
    }

    public String getAlgorithmTag() {
        return algorithmTag;
    }

    public String getProblemTag() {
        return problemTag;
    }

    public String getReferenceParetoFront() {
        return referenceParetoFront;
    }

    public int getRunId() {
        return this.runId;
    }

    public double getRunTime() {
        return runTime;
    }
}
