package opt.jmetal.util.experiment.component;

import oil.sim.common.NormalizationHelper;
import opt.jmetal.qualityindicator.QualityIndicator;
import opt.jmetal.qualityindicator.impl.*;
import opt.jmetal.qualityindicator.impl.hypervolume.WFGHypervolume;
import opt.jmetal.solution.Solution;
import opt.jmetal.util.JMetalException;
import opt.jmetal.util.JMetalLogger;
import opt.jmetal.util.experiment.Experiment;
import opt.jmetal.util.experiment.ExperimentComponent;
import opt.jmetal.util.experiment.util.ExperimentAlgorithm;
import opt.jmetal.util.experiment.util.ExperimentProblem;
import opt.jmetal.util.front.Front;
import opt.jmetal.util.front.imp.ArrayFront;
import opt.jmetal.util.front.util.FrontNormalizer;
import opt.jmetal.util.front.util.FrontUtils;
import opt.jmetal.util.point.PointSolution;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;

/**
 * This class computes the {@link QualityIndicator}s of an experiment. Once the
 * algorithms of an experiment have been executed through running an instance of
 * class {@link ExecuteAlgorithms}, the list of indicators in obtained from the
 * {@link ExperimentComponent #getIndicatorsList()} method. Then, for every
 * combination algorithm + problem, the indicators are applied to all the FUN
 * files and the resulting values are store in a file called as
 * {@link QualityIndicator #getName()}, which is located in the same directory
 * of the FUN files.
 *
 * @author Antonio J. Nebro <antonio@lcc.uma.es>
 */
public class ComputeQualityIndicators<S extends Solution<?>, Result extends List<S>> implements ExperimentComponent {
    protected final Experiment<S, Result> experiment;
    protected static Logger logger = LogManager.getLogger(ComputeQualityIndicators.class.getName());

    public ComputeQualityIndicators(Experiment<S, Result> experiment) {
        this.experiment = experiment;
    }

    @Override
    public void run() throws IOException {
        experiment.removeDuplicatedAlgorithms();
        resetIndicatorFiles();

        for (GenericIndicator<S> indicator : experiment.getIndicatorList()) {
            JMetalLogger.logger.info("Computing indicator: " + indicator.getName());

            for (ExperimentAlgorithm<?, Result> algorithm : experiment.getAlgorithmList()) {
                String algorithmDirectory = experiment.getExperimentBaseDirectory() + "/data/"
                        + algorithm.getAlgorithmTag();
                for (ExperimentProblem<?> problem : experiment.getProblemList()) {
                    // 问题目录结构为：data/algorithm/problem
                    String problemDirectory = algorithmDirectory + "/" + problem.getTag();
                    // 参考前沿目录结构：result/Experiment/PF/problemName.pf
                    String referenceFrontName = experiment.getReferenceFrontDirectory() + "/"
                            + problem.getReferenceFront();
                    JMetalLogger.logger.info("RF: " + referenceFrontName);
                    // 1. 读取参考前沿面
                    Front referenceFront = new ArrayFront(referenceFrontName);

                    // 2. 根据所有运行结果中的最大值和最小值标准化
                    double[][] maxminvalue = NormalizationHelper.getMaxMinObjectValue(problem.getTag());
                    FrontNormalizer frontNormalizer = new FrontNormalizer(maxminvalue[0], maxminvalue[1]);
                    Front normalizedReferenceFront = frontNormalizer.normalize(referenceFront);

                    // 问题目录结构为：data/algorithm/problem/indeicator
                    String qualityIndicatorFile = problemDirectory + "/" + indicator.getName();
                    // 3. 以规范化后的整个运行结果所产生的的解为参考平面
                    indicator.setReferenceParetoFront(normalizedReferenceFront);
                    for (int run = 0; run < experiment.getIndependentRuns(); run++) {
                        // 问题目录结构为：data/algorithm/problem/Fun*.tsv
                        String frontFileName = problemDirectory + "/" + experiment.getOutputParetoFrontFileName() + run
                                + ".tsv";
                        // 4. 读取当前算法针对当前问题的第run次运行结果的前沿
                        Front front = new ArrayFront(frontFileName);

                        if (front.getNumberOfPoints() == 0) {
                            logger.warn(frontFileName + " 文件为空，无法计算其指标");
                            continue;
                        } else {
                            // 5. 标准化运行结果前沿
                            Front normalizedFront = frontNormalizer.normalize(front);
                            List<PointSolution> normalizedPopulation = FrontUtils
                                    .convertFrontToSolutionList(normalizedFront);
                            // 6. 计算指标值
                            Double indicatorValue = (Double) indicator.evaluate((List<S>) normalizedPopulation);
                            JMetalLogger.logger.info(indicator.getName() + ": " + indicatorValue);
                            // 7. 写入指标值到当前目录下的指标文件中
                            writeQualityIndicatorValueToFile(indicatorValue, qualityIndicatorFile);
                        }
                    }
                }
            }
        }
        findBestIndicatorFronts(experiment);
        writeSummaryFile(experiment);
    }

    /**
     * 输出指标值
     *
     * @param indicatorValue
     * @param qualityIndicatorFile
     */
    protected void writeQualityIndicatorValueToFile(Double indicatorValue, String qualityIndicatorFile) {

        try (FileWriter os = new FileWriter(qualityIndicatorFile, true)) {
            os.write("" + indicatorValue + "\n");
        } catch (IOException ex) {
            throw new JMetalException("Error writing indicator file" + ex);
        }
    }

    public void findBestIndicatorFronts(Experiment<?, Result> experiment) throws IOException {
        for (GenericIndicator<?> indicator : experiment.getIndicatorList()) {
            for (ExperimentAlgorithm<?, Result> algorithm : experiment.getAlgorithmList()) {
                String algorithmDirectory;
                algorithmDirectory = experiment.getExperimentBaseDirectory() + "/data/" + algorithm.getAlgorithmTag();

                for (ExperimentProblem<?> problem : experiment.getProblemList()) {
                    String indicatorFileName = algorithmDirectory + "/" + problem.getTag() + "/" + indicator.getName();
                    Path indicatorFile = Paths.get(indicatorFileName);
                    if (indicatorFile == null) {
                        throw new JMetalException("Indicator file " + indicator.getName() + " doesn't exist");
                    }

                    List<String> fileArray;
                    fileArray = Files.readAllLines(indicatorFile, StandardCharsets.UTF_8);

                    List<Pair<Double, Integer>> list = new ArrayList<>();

                    for (int i = 0; i < fileArray.size(); i++) {
                        Pair<Double, Integer> pair = new ImmutablePair<>(Double.parseDouble(fileArray.get(i)), i);
                        list.add(pair);
                    }

                    Collections.sort(list, new Comparator<Pair<Double, Integer>>() {
                        @Override
                        public int compare(Pair<Double, Integer> pair1, Pair<Double, Integer> pair2) {
                            if (Math.abs(pair1.getLeft()) > Math.abs(pair2.getLeft())) {
                                return 1;
                            } else if (Math.abs(pair1.getLeft()) < Math.abs(pair2.getLeft())) {
                                return -1;
                            } else {
                                return 0;
                            }
                        }
                    });
                    String bestFunFileName;
                    String bestVarFileName;
                    String medianFunFileName;
                    String medianVarFileName;

                    String outputDirectory = algorithmDirectory + "/" + problem.getTag();

                    bestFunFileName = outputDirectory + "/BEST_" + indicator.getName() + "_FUN.tsv";
                    bestVarFileName = outputDirectory + "/BEST_" + indicator.getName() + "_VAR.tsv";
                    medianFunFileName = outputDirectory + "/MEDIAN_" + indicator.getName() + "_FUN.tsv";
                    medianVarFileName = outputDirectory + "/MEDIAN_" + indicator.getName() + "_VAR.tsv";
                    if (indicator.isTheLowerTheIndicatorValueTheBetter()) {
                        String bestFunFile = outputDirectory + "/" + experiment.getOutputParetoFrontFileName()
                                + list.get(0).getRight() + ".tsv";
                        String bestVarFile = outputDirectory + "/" + experiment.getOutputParetoSetFileName()
                                + list.get(0).getRight() + ".tsv";

                        Files.copy(Paths.get(bestFunFile), Paths.get(bestFunFileName), REPLACE_EXISTING);
                        Files.copy(Paths.get(bestVarFile), Paths.get(bestVarFileName), REPLACE_EXISTING);
                    } else {
                        String bestFunFile = outputDirectory + "/" + experiment.getOutputParetoFrontFileName()
                                + list.get(list.size() - 1).getRight() + ".tsv";
                        String bestVarFile = outputDirectory + "/" + experiment.getOutputParetoSetFileName()
                                + list.get(list.size() - 1).getRight() + ".tsv";

                        Files.copy(Paths.get(bestFunFile), Paths.get(bestFunFileName), REPLACE_EXISTING);
                        Files.copy(Paths.get(bestVarFile), Paths.get(bestVarFileName), REPLACE_EXISTING);
                    }

                    int medianIndex = list.size() / 2;
                    String medianFunFile = outputDirectory + "/" + experiment.getOutputParetoFrontFileName()
                            + list.get(medianIndex).getRight() + ".tsv";
                    String medianVarFile = outputDirectory + "/" + experiment.getOutputParetoSetFileName()
                            + list.get(medianIndex).getRight() + ".tsv";

                    Files.copy(Paths.get(medianFunFile), Paths.get(medianFunFileName), REPLACE_EXISTING);
                    Files.copy(Paths.get(medianVarFile), Paths.get(medianVarFileName), REPLACE_EXISTING);
                }
            }
        }
    }

    /**
     * Deletes the files containing the indicator values if the exist.
     */
    protected void resetIndicatorFiles() {
        for (GenericIndicator<S> indicator : experiment.getIndicatorList()) {
            for (ExperimentAlgorithm<?, Result> algorithm : experiment.getAlgorithmList()) {
                for (ExperimentProblem<?> problem : experiment.getProblemList()) {
                    String algorithmDirectory;
                    algorithmDirectory = experiment.getExperimentBaseDirectory() + "/data/"
                            + algorithm.getAlgorithmTag();
                    String problemDirectory = algorithmDirectory + "/" + problem.getTag();
                    String qualityIndicatorFile = problemDirectory + "/" + indicator.getName();

                    resetFile(qualityIndicatorFile);
                }
            }
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
     * 输出汇总信息
     *
     * @param experiment
     */
    protected void writeSummaryFile(Experiment<S, Result> experiment) {
        JMetalLogger.logger.info("Writing experiment summary file");
        String headerOfCSVFile = "Algorithm,Problem,IndicatorName,ExecutionId,IndicatorValue";
        String csvFileName = this.experiment.getExperimentBaseDirectory() + "/QualityIndicatorSummary.csv";
        resetFile(csvFileName);

        try (FileWriter os = new FileWriter(csvFileName, true)) {
            os.write("" + headerOfCSVFile + "\n");

            for (GenericIndicator<?> indicator : experiment.getIndicatorList()) {
                for (ExperimentAlgorithm<?, Result> algorithm : experiment.getAlgorithmList()) {
                    String algorithmDirectory;
                    algorithmDirectory = experiment.getExperimentBaseDirectory() + "/data/"
                            + algorithm.getAlgorithmTag();

                    for (ExperimentProblem<?> problem : experiment.getProblemList()) {
                        String indicatorFileName = algorithmDirectory + "/" + problem.getTag() + "/"
                                + indicator.getName();
                        Path indicatorFile = Paths.get(indicatorFileName);
                        if (indicatorFile == null) {
                            throw new JMetalException("Indicator file " + indicator.getName() + " doesn't exist");
                        }
                        System.out.println("-----");
                        System.out.println(indicatorFileName);

                        List<String> fileArray;
                        fileArray = Files.readAllLines(indicatorFile, StandardCharsets.UTF_8);
                        System.out.println(fileArray);
                        System.out.println("++++++");

                        for (int i = 0; i < fileArray.size(); i++) {
                            String row = algorithm.getAlgorithmTag() + "," + problem.getTag() + ","
                                    + indicator.getName() + "," + i + "," + fileArray.get(i);
                            os.write("" + row + "\n");
                        }
                    }
                }
            }
        } catch (IOException ex) {
            throw new JMetalException("Error writing indicator file" + ex);
        }
    }

    /**
     * 分析实验结果【离线分析】
     *
     * @param outputDirectoryName
     * @param experimentBaseDirectory
     * @param outputParetoFrontFileName
     * @param outputParetoSetFileName
     * @param problems
     * @param algorithms
     * @param runs
     */
    public void runAnalysis(String outputDirectoryName, String experimentBaseDirectory, String outputParetoFrontFileName, String outputParetoSetFileName,
                            List<String> problems, List<String> algorithms, List<String> indicators, int runs, int popSize, int evaluation) {
        // 清除之前的内容
        for (String indicator : indicators) {
            for (String algorithm : algorithms) {
                for (String problem : problems) {
                    String qualityIndicatorFile = experimentBaseDirectory + "/data/" + algorithm + "/" + problem + "/" + indicator;
                    resetFile(qualityIndicatorFile);
                }
            }
        }

        // 1. 读取参考前沿面
        JMetalLogger.logger.info(experimentBaseDirectory + outputDirectoryName + "oilschedule.pf");

        try {
            Front referenceFront = new ArrayFront(experimentBaseDirectory + outputDirectoryName + "oilschedule.pf");
            FrontNormalizer frontNormalizer = new FrontNormalizer(FrontUtils.getMinimumValues(referenceFront), FrontUtils.getMaximumValues(referenceFront));
            Front normalizedReferenceFront = frontNormalizer.normalize(referenceFront);

            for (String ind : indicators) {
                // 3. 以规范化后的整个运行结果所产生的的解为参考平面
                GenericIndicator<S> indicator = null;
                switch (ind) {
                    case "HV":
                        indicator = new WFGHypervolume<>(normalizedReferenceFront);
                        break;
                    case "IGD":
                        indicator = new InvertedGenerationalDistance<>(normalizedReferenceFront);
                        break;
                    case "IGD+":
                        indicator = new InvertedGenerationalDistancePlus<>(normalizedReferenceFront);
                        break;
                    case "GSPREAD":
                        indicator = new GeneralizedSpread<>(normalizedReferenceFront);
                        break;
                    case "GD":
                        indicator = new GenerationalDistance<>(normalizedReferenceFront);
                        break;
                    case "EP":
                        indicator = new Epsilon<>(normalizedReferenceFront);
                        break;
                    default:
                        indicator = new WFGHypervolume<>(normalizedReferenceFront);
                        break;
                }

                for (String problem : problems) {
                    for (String algorithm : algorithms) {
                        String dirName = experimentBaseDirectory + "data/" + algorithm + "/" + problem + "/";
                        for (int r = 0; r < runs; r++) {
                            JMetalLogger.logger.info("Computing " + algorithm + " on " + problem + " run: " + r);

                            // 1.标准化运行结果前沿，最终结果的指标值
                            Front front = new ArrayFront(dirName + outputParetoFrontFileName + r + ".tsv");
                            if (front.getNumberOfPoints() == 0) {
                                JMetalLogger.logger.severe("运行结果为空，无法计算其指标");
                                continue;
                            } else {
                                Front normalizedFront = frontNormalizer.normalize(front);
                                List<PointSolution> normalizedPopulation = FrontUtils.convertFrontToSolutionList(normalizedFront);
                                Double indicatorValue = (Double) indicator.evaluate((List<S>) normalizedPopulation);
                                String qualityIndicatorFile = experimentBaseDirectory + "data/" + algorithm + "/" + problem + "/" + indicator.getName();
                                writeQualityIndicatorValueToFile(indicatorValue, qualityIndicatorFile);
                            }

                            // 2.随迭代而产生的指标值
                            int iterations = (int) Math.ceil(evaluation / popSize);
                            String solutionFunListFilePath = dirName + "FUN" + r + ".list";
                            List<S> solutionFunList = (List<S>) FrontUtils
                                    .convertFrontToSolutionList(new ArrayFront(solutionFunListFilePath));
                            String qualityIndicatorFile = dirName + indicator.getName() + ".r" + r;
                            resetFile(qualityIndicatorFile);
                            for (int i = 0; i < iterations; i++) {

                                List<S> solutionList = solutionFunList.subList(i * popSize, (i + 1) * popSize);
                                Front normalizedFront = frontNormalizer.normalize(new ArrayFront(solutionList));
                                List<PointSolution> normalizedPopulation = FrontUtils.convertFrontToSolutionList(normalizedFront);
                                Double indicatorValue = (Double) indicator.evaluate((List<S>) normalizedPopulation);
                                writeQualityIndicatorValueToFile(indicatorValue, qualityIndicatorFile);
                            }
                        }
                    }
                }
            }

            // 输出汇总信息
            JMetalLogger.logger.info("Writing experiment summary file");
            String headerOfCSVFile = "Algorithm,Problem,IndicatorName,ExecutionId,IndicatorValue";
            String csvFileName = experimentBaseDirectory + "QualityIndicatorSummary.csv";
            resetFile(csvFileName);
            FileWriter os = new FileWriter(csvFileName, true);
            os.write("" + headerOfCSVFile + "\n");

            for (String algorithm : algorithms) {
                String algorithmDirectory;
                algorithmDirectory = experimentBaseDirectory + "data/" + algorithm;

                for (String problem : problems) {
                    for (String indicator : indicators) {
                        String indicatorFileName = algorithmDirectory + "/" + problem + "/" + indicator;
                        Path indicatorFile = Paths.get(indicatorFileName);
                        if (indicatorFile == null) {
                            throw new JMetalException("Indicator file " + indicator + " doesn't exist");
                        }
                        System.out.println("-----");
                        System.out.println(indicatorFileName);
                        List<String> fileArray = Files.readAllLines(indicatorFile, StandardCharsets.UTF_8);
                        System.out.println(fileArray);
                        System.out.println("++++++");

                        for (int i = 0; i < fileArray.size(); i++) {
                            String row = algorithm + "," + problem + "," + indicator + "," + i + "," + fileArray.get(i);
                            os.write("" + row + "\n");
                        }
                    }
                }
            }
            os.flush();
            os.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
