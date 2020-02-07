package opt.jmetal.util.experiment.component;

import opt.jmetal.problem.DoubleProblem;
import opt.jmetal.problem.impl.AbstractDoubleProblem;
import opt.jmetal.solution.DoubleSolution;
import opt.jmetal.solution.impl.DefaultDoubleSolution;
import opt.jmetal.util.fileoutput.SolutionListOutput;
import opt.jmetal.util.solutionattribute.impl.GenericSolutionAttribute;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import opt.jmetal.util.JMetalException;
import opt.jmetal.util.JMetalLogger;
import opt.jmetal.util.archive.impl.NonDominatedSolutionListArchive;
import opt.jmetal.util.experiment.Experiment;
import opt.jmetal.util.experiment.ExperimentComponent;
import opt.jmetal.util.experiment.util.ExperimentAlgorithm;
import opt.jmetal.util.experiment.util.ExperimentProblem;
import opt.jmetal.util.front.Front;
import opt.jmetal.util.front.imp.ArrayFront;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * This class computes the reference Pareto set and front from a set of data
 * files containing the variable (VARx.tsv file) and objective (FUNx.tsv)
 * values. A requirement is that the variable values MUST correspond to
 * {@link DoubleSolution} solutions, i.e., the solved problems must be instances
 * of {@link DoubleProblem}.
 * <p>
 * Once the algorithms of an experiment have been executed through running an
 * instance of class {@link ExecuteAlgorithms}, all the obtained fronts of all
 * the algorithms are gathered per problem; then, the dominated solutions are
 * removed thus yielding to the reference Pareto front.
 * <p>
 * By default, the files are stored in a directory called "referenceFront",
 * which is located in the experiment base directory. The following files are
 * generated per problem: - "problemName.pf": the reference Pareto front. -
 * "problemName.ps": the reference Pareto set (i.e., the variable values of the
 * solutions of the reference Pareto front. - "problemName.algorithmName.pf":
 * the objectives values of the contributed solutions by the algorithm called
 * "algorithmName" to "problemName.pf" - "problemName.algorithmName.ps": the
 * variable values of the contributed solutions by the algorithm called
 * "algorithmName" to "problemName.ps"
 *
 * @author Antonio J. Nebro <antonio@lcc.uma.es>
 */
public class GenerateReferenceParetoSetAndFrontFromDoubleSolutions implements ExperimentComponent {
    protected final Experiment<?, ?> experiment;
    private static Logger logger = LogManager
            .getLogger(GenerateReferenceParetoSetAndFrontFromDoubleSolutions.class.getName());

    public GenerateReferenceParetoSetAndFrontFromDoubleSolutions(Experiment<?, ?> experimentConfiguration) {
        this.experiment = experimentConfiguration;
    }

    /**
     * The run() method creates de output directory and compute the fronts
     */
    @Override
    public void run() throws IOException {
        // 参考前沿目录结构：result/Experiment/PF/
        String outputDirectoryName = experiment.getReferenceFrontDirectory();
        createOutputDirectory(outputDirectoryName);

        List<String> referenceFrontFileNames = new LinkedList<>();

        for (ExperimentProblem<?> problem : experiment.getProblemList()) {
            // 1.获取某一个问题的非支配解集
            List<DoubleSolution> nonDominatedSolutions = getNonDominatedSolutions(problem.getTag());
            referenceFrontFileNames.add(problem.getReferenceFront());

            // 2.写入所有算法构成的参考平面到目录：result/Experiment/PF/
            writeReferenceFrontFile(outputDirectoryName, problem, nonDominatedSolutions);
            writeReferenceSetFile(outputDirectoryName, problem, nonDominatedSolutions);

            // 3.写入每个算法构成的参考平面到目录：result/Experiment/PF/
            writeFilesWithTheSolutionsContributedByEachAlgorithm(outputDirectoryName, problem, nonDominatedSolutions);
        }

    }

    /**
     * 将非支配解按照算法分别写入指定的目录下
     *
     * @param outputDirectoryName
     * @param problem
     * @param nonDominatedSolutions
     * @throws IOException
     */
    protected void writeFilesWithTheSolutionsContributedByEachAlgorithm(String outputDirectoryName,
                                                                        ExperimentProblem<?> problem, List<DoubleSolution> nonDominatedSolutions) throws IOException {
        GenericSolutionAttribute<DoubleSolution, String> solutionAttribute = new GenericSolutionAttribute<DoubleSolution, String>();

        for (ExperimentAlgorithm<?, ?> algorithm : experiment.getAlgorithmList()) {
            List<DoubleSolution> solutionsPerAlgorithm = new ArrayList<>();
            for (DoubleSolution solution : nonDominatedSolutions) {
                if (algorithm.getAlgorithmTag().equals(solutionAttribute.getAttribute(solution))) {
                    solutionsPerAlgorithm.add(solution);
                }
            }

            new SolutionListOutput(solutionsPerAlgorithm).printObjectivesToFile(
                    outputDirectoryName + "/" + problem.getTag() + "." + algorithm.getAlgorithmTag() + ".rf");
            new SolutionListOutput(solutionsPerAlgorithm).printVariablesToFile(
                    outputDirectoryName + "/" + problem.getTag() + "." + algorithm.getAlgorithmTag() + ".rs");
        }
    }

    /**
     * 输出某一个问题的参考前沿面
     *
     * @param outputDirectoryName
     * @param problem
     * @param nonDominatedSolutions
     * @throws IOException
     */
    protected void writeReferenceFrontFile(String outputDirectoryName, ExperimentProblem<?> problem,
                                           List<DoubleSolution> nonDominatedSolutions) throws IOException {
        String referenceFrontFileName = outputDirectoryName + "/" + problem.getReferenceFront();

        new SolutionListOutput(nonDominatedSolutions).printObjectivesToFile(referenceFrontFileName);
    }

    /**
     * 输出某个问题的参考解集
     *
     * @param outputDirectoryName
     * @param problem
     * @param nonDominatedSolutions
     * @throws IOException
     */
    protected void writeReferenceSetFile(String outputDirectoryName, ExperimentProblem<?> problem,
                                         List<DoubleSolution> nonDominatedSolutions) throws IOException {
        String referenceSetFileName = outputDirectoryName + "/" + problem.getTag() + ".ps";
        new SolutionListOutput(nonDominatedSolutions).printVariablesToFile(referenceSetFileName);
    }

    /**
     * 根据某一算法的运行结果，创建非支配解集
     * <p>
     * Create a list of non dominated {@link DoubleSolution} solutions from the
     * FUNx.tsv and VARx.tsv files that must have been previously obtained (probably
     * by invoking the {@link ExecuteAlgorithms#run} method).
     *
     * @param problem
     * @return
     * @throws FileNotFoundException
     */
    protected List<DoubleSolution> getNonDominatedSolutions(String problem) throws FileNotFoundException {
        NonDominatedSolutionListArchive<DoubleSolution> nonDominatedSolutionArchive = new NonDominatedSolutionListArchive<DoubleSolution>();

        for (ExperimentAlgorithm<?, ?> algorithm : experiment.getAlgorithmList()) {
            // 问题目录结构为：result/data/algorithm/problem
            String problemDirectory = experiment.getExperimentBaseDirectory() + "/data/" + algorithm.getAlgorithmTag()
                    + "/" + problem;

            for (int r = 0; r < experiment.getIndependentRuns(); r++) {
                String frontFileName = problemDirectory + "/" + experiment.getOutputParetoFrontFileName() + r + ".tsv";
                String paretoSetFileName = problemDirectory + "/" + experiment.getOutputParetoSetFileName() + r
                        + ".tsv";
                Front frontWithObjectiveValues = new ArrayFront(frontFileName);
                Front frontWithVariableValues = new ArrayFront(paretoSetFileName);
                List<DoubleSolution> solutionList = createSolutionListFrontFiles(algorithm.getAlgorithmTag(),
                        frontWithVariableValues, frontWithObjectiveValues);
                if (solutionList != null && !solutionList.isEmpty()) {// 允许部分算法跑不出结果
                    for (DoubleSolution solution : solutionList) {
                        nonDominatedSolutionArchive.add(solution);
                    }
                }
            }
        }

        return nonDominatedSolutionArchive.getSolutionList();
    }

    /**
     * 根据某一算法的运行结果，创建非支配解集
     *
     * @param problem
     * @param algorithms
     * @param runs
     * @param experimentBaseDirectory
     * @param outputParetoFrontFileName
     * @param outputParetoSetFileName
     * @return
     * @throws FileNotFoundException
     */
    public static List<DoubleSolution> getNonDominatedSolutions(String problem, List<String> algorithms, int runs, String experimentBaseDirectory,
                                                                String outputParetoFrontFileName, String outputParetoSetFileName) throws FileNotFoundException {
        NonDominatedSolutionListArchive<DoubleSolution> nonDominatedSolutionArchive = new NonDominatedSolutionListArchive<DoubleSolution>();

        for (String algorithm : algorithms) {
            // 问题目录结构为：result/data/algorithm/problem
            String problemDirectory = experimentBaseDirectory + "/data/" + algorithm
                    + "/" + problem;

            for (int r = 0; r < runs; r++) {
                String frontFileName = problemDirectory + "/" + outputParetoFrontFileName + r + ".tsv";
                String paretoSetFileName = problemDirectory + "/" + outputParetoSetFileName + r
                        + ".tsv";
                Front frontWithObjectiveValues = new ArrayFront(frontFileName);
                Front frontWithVariableValues = new ArrayFront(paretoSetFileName);
                List<DoubleSolution> solutionList = createSolutionListFrontFiles(algorithm,
                        frontWithVariableValues, frontWithObjectiveValues);
                if (solutionList != null && !solutionList.isEmpty()) {// 允许部分算法跑不出结果
                    for (DoubleSolution solution : solutionList) {
                        nonDominatedSolutionArchive.add(solution);
                    }
                }
            }
        }

        return nonDominatedSolutionArchive.getSolutionList();
    }

    /**
     * Create the output directory where the result files will be stored
     *
     * @param outputDirectoryName
     * @return
     */
    protected static File createOutputDirectory(String outputDirectoryName) {
        File outputDirectory = new File(outputDirectoryName);

        // 创建文件夹
        boolean result = new File(outputDirectoryName).mkdir();
        JMetalLogger.logger.info("Creating " + outputDirectoryName + ". Status = " + result);
        return outputDirectory;
    }

    /**
     * 获取运行得到的解集，并设置解的属性名为算法名
     *
     * @param algorithmName
     * @param frontWithVariableValues
     * @param frontWithObjectiveValues
     * @return
     */
    protected static List<DoubleSolution> createSolutionListFrontFiles(String algorithmName, Front frontWithVariableValues,
                                                                       Front frontWithObjectiveValues) {
        if (frontWithVariableValues.getNumberOfPoints() != frontWithObjectiveValues.getNumberOfPoints()) {
            throw new JMetalException("The number of solutions in the variable and objective fronts are not equal");
        } else if (frontWithObjectiveValues.getNumberOfPoints() == 0) {
            logger.warn(algorithmName + ": The front of solutions is empty");
            return null;
        }

        GenericSolutionAttribute<DoubleSolution, String> solutionAttribute = new GenericSolutionAttribute<DoubleSolution, String>();

        int numberOfVariables = frontWithVariableValues.getPointDimensions();
        int numberOfObjectives = frontWithObjectiveValues.getPointDimensions();
        DummyProblem problem = new DummyProblem(numberOfVariables, numberOfObjectives);

        List<DoubleSolution> solutionList = new ArrayList<>();
        for (int i = 0; i < frontWithVariableValues.getNumberOfPoints(); i++) {
            DoubleSolution solution = new DefaultDoubleSolution(problem);
            for (int vars = 0; vars < numberOfVariables; vars++) {
                solution.setVariableValue(vars, frontWithVariableValues.getPoint(i).getValues()[vars]);
            }
            for (int objs = 0; objs < numberOfObjectives; objs++) {
                solution.setObjective(objs, frontWithObjectiveValues.getPoint(i).getValues()[objs]);
            }

            // 设置解的属性
            solutionAttribute.setAttribute(solution, algorithmName);
            solutionList.add(solution);
        }

        return solutionList;
    }

    /**
     * This private class is intended to create{@link DoubleSolution} objects from
     * the stored values of variables and objectives obtained in files after running
     * an experiment. The values of the lower and upper limits are useless.
     */
    @SuppressWarnings("serial")
    private static class DummyProblem extends AbstractDoubleProblem {
        public DummyProblem(int numberOfVariables, int numberOfObjectives) {
            setNumberOfVariables(numberOfVariables);
            setNumberOfObjectives(numberOfObjectives);

            List<Double> lowerLimit = new ArrayList<>(getNumberOfVariables());
            List<Double> upperLimit = new ArrayList<>(getNumberOfVariables());

            for (int i = 0; i < getNumberOfVariables(); i++) {
                lowerLimit.add(-1.0);
                upperLimit.add(1.0);
            }

            setLowerLimit(lowerLimit);
            setUpperLimit(upperLimit);
        }

        @Override
        public void evaluate(DoubleSolution solution) {
            // This method is an intentionally-blank override.
        }
    }
}
