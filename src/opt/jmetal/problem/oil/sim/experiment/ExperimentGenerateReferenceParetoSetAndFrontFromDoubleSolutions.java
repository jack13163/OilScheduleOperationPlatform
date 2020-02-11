package opt.jmetal.problem.oil.sim.experiment;

import opt.jmetal.solution.DoubleSolution;
import opt.jmetal.util.archive.impl.NonDominatedSolutionListArchive;
import opt.jmetal.util.experiment.Experiment;
import opt.jmetal.util.experiment.component.GenerateReferenceParetoSetAndFrontFromDoubleSolutions;
import opt.jmetal.util.experiment.util.ExperimentAlgorithm;
import opt.jmetal.util.experiment.util.ExperimentProblem;
import opt.jmetal.util.fileoutput.SolutionListOutput;
import opt.jmetal.util.front.Front;
import opt.jmetal.util.front.imp.ArrayFront;
import opt.jmetal.util.solutionattribute.impl.GenericSolutionAttribute;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ExperimentGenerateReferenceParetoSetAndFrontFromDoubleSolutions
        extends GenerateReferenceParetoSetAndFrontFromDoubleSolutions {

    public ExperimentGenerateReferenceParetoSetAndFrontFromDoubleSolutions(Experiment<?, ?> experimentConfiguration) {
        super(experimentConfiguration);
    }

    @Override
    public void run() throws IOException {
        // 生成实验的参考平面
        String outputDirectoryName = experiment.getReferenceFrontDirectory();
        createOutputDirectory(outputDirectoryName);

        // 1.写入策略构成的参考平面到目录：result/Experiment/PF/
        List<DoubleSolution> nonDominatedSolutions = getNonDominatedSolutions();
        writeReferenceFrontFile(outputDirectoryName, nonDominatedSolutions);
        writeReferenceSetFile(outputDirectoryName, nonDominatedSolutions);

        // 2.写入每个策略每种算法的参考平面
        for (ExperimentProblem<?> problem : experiment.getProblemList()) {
            // 2.1 获取某一个问题的非支配解集
            nonDominatedSolutions = getNonDominatedSolutions(problem.getTag());

            // 2.2 写入每个算法构成的参考平面到目录
            writeFilesWithTheSolutionsContributedByEachAlgorithm(outputDirectoryName, problem, nonDominatedSolutions);
        }
    }

    /**
     * 生成PF
     *
     * @param outputDirectoryName
     * @param experimentBaseDirectory
     * @param outputParetoFrontFileName
     * @param problems
     * @param algorithms
     * @param runs
     * @throws IOException
     */
    public void runAnalysis(String outputDirectoryName, String experimentBaseDirectory, String outputParetoFrontFileName, String outputParetoSetFileName,
                            List<String> problems, List<String> algorithms, int runs) throws IOException {
        // 生成实验的参考平面
        createOutputDirectory(experimentBaseDirectory + outputDirectoryName);

        // 1.写入策略构成的参考平面到目录：result/Experiment/PF/
        NonDominatedSolutionListArchive<DoubleSolution> nonDominatedSolutionArchive = new NonDominatedSolutionListArchive<DoubleSolution>();

        // 将所有的策略得到的结果汇集在一起
        for (String problem : problems) {
            for (String algorithm : algorithms) {
                // 问题目录结构为：result/data/algorithm/problem
                String problemDirectory = experimentBaseDirectory + "/data/"
                        + algorithm + "/" + problem;

                for (int r = 0; r < runs; r++) {
                    String frontFileName = problemDirectory + "/" + outputParetoFrontFileName + r
                            + ".tsv";
                    String paretoSetFileName = problemDirectory + "/" + outputParetoFrontFileName + r
                            + ".tsv";
                    Front frontWithObjectiveValues = new ArrayFront(frontFileName);
                    Front frontWithVariableValues = new ArrayFront(paretoSetFileName);
                    List<DoubleSolution> solutionList = createSolutionListFrontFiles(algorithm,
                            frontWithVariableValues, frontWithObjectiveValues);
                    if (solutionList != null && !solutionList.isEmpty()) {
                        for (DoubleSolution solution : solutionList) {
                            nonDominatedSolutionArchive.add(solution);
                        }
                    }
                }
            }
        }

        List<DoubleSolution> nonDominatedSolutions = nonDominatedSolutionArchive.getSolutionList();

        writeReferenceFrontFile(experimentBaseDirectory + outputDirectoryName, nonDominatedSolutions);
        writeReferenceSetFile(experimentBaseDirectory + outputDirectoryName, nonDominatedSolutions);

        // 2.写入每个策略每种算法的参考平面
        for (String problem : problems) {
            // 2.1 获取某一个问题的非支配解集
            nonDominatedSolutions = GenerateReferenceParetoSetAndFrontFromDoubleSolutions.getNonDominatedSolutions(problem, algorithms, runs,
                    experimentBaseDirectory, outputParetoFrontFileName, outputParetoSetFileName);

            for (String algorithm : algorithms) {
                // 2.2 写入每个算法构成的参考平面到目录
                GenericSolutionAttribute<DoubleSolution, String> solutionAttribute = new GenericSolutionAttribute<DoubleSolution, String>();

                List<DoubleSolution> solutionsPerAlgorithm = new ArrayList<>();
                for (DoubleSolution solution : nonDominatedSolutions) {
                    if (algorithm.equals(solutionAttribute.getAttribute(solution))) {
                        solutionsPerAlgorithm.add(solution);
                    }
                }

                new SolutionListOutput(solutionsPerAlgorithm).printObjectivesToFile(
                        experimentBaseDirectory + outputDirectoryName + "/" + problem + "." + algorithm + ".rf");
                new SolutionListOutput(solutionsPerAlgorithm).printVariablesToFile(
                        experimentBaseDirectory + outputDirectoryName + "/" + problem + "." + algorithm + ".rs");
            }
        }
    }

    protected static void writeReferenceFrontFile(String outputDirectoryName, List<DoubleSolution> nonDominatedSolutions)
            throws IOException {
        new SolutionListOutput(nonDominatedSolutions).printObjectivesToFile(outputDirectoryName + "/oilschedule.pf");
    }

    protected static void writeReferenceSetFile(String outputDirectoryName, List<DoubleSolution> nonDominatedSolutions)
            throws IOException {
        new SolutionListOutput(nonDominatedSolutions).printVariablesToFile(outputDirectoryName + "/oilschedule.ps");
    }

    protected List<DoubleSolution> getNonDominatedSolutions() throws FileNotFoundException {
        NonDominatedSolutionListArchive<DoubleSolution> nonDominatedSolutionArchive = new NonDominatedSolutionListArchive<DoubleSolution>();

        // 将所有的策略得到的结果汇集在一起
        for (ExperimentProblem<?> problem : experiment.getProblemList()) {
            for (ExperimentAlgorithm<?, ?> algorithm : experiment.getAlgorithmList()) {
                // 问题目录结构为：result/data/algorithm/problem
                String problemDirectory = experiment.getExperimentBaseDirectory() + "/data/"
                        + algorithm.getAlgorithmTag() + "/" + problem.getTag();

                for (int r = 0; r < experiment.getIndependentRuns(); r++) {
                    String frontFileName = problemDirectory + "/" + experiment.getOutputParetoFrontFileName() + r
                            + ".tsv";
                    String paretoSetFileName = problemDirectory + "/" + experiment.getOutputParetoSetFileName() + r
                            + ".tsv";
                    Front frontWithObjectiveValues = new ArrayFront(frontFileName);
                    Front frontWithVariableValues = new ArrayFront(paretoSetFileName);
                    List<DoubleSolution> solutionList = createSolutionListFrontFiles(algorithm.getAlgorithmTag(),
                            frontWithVariableValues, frontWithObjectiveValues);
                    if (solutionList != null && !solutionList.isEmpty()) {
                        // 允许部分算法跑不出结果
                        for (DoubleSolution solution : solutionList) {
                            nonDominatedSolutionArchive.add(solution);
                        }
                    }
                }
            }
        }

        return nonDominatedSolutionArchive.getSolutionList();
    }
}
