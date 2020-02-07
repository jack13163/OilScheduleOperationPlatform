package oil.sim.experiment;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import opt.jmetal.algorithm.Algorithm;
import opt.jmetal.algorithm.multiobjective.mocell.MOCellBuilder;
import opt.jmetal.algorithm.multiobjective.nsgaii.NSGAIIBuilder;
import opt.jmetal.algorithm.multiobjective.nsgaiii.NSGAIIIBuilder;
import opt.jmetal.algorithm.multiobjective.spea2.SPEA2Builder;
import opt.jmetal.operator.impl.crossover.IntegerSBXCrossover;
import opt.jmetal.operator.impl.mutation.IntegerPolynomialMutation;
import opt.jmetal.operator.impl.selection.BinaryTournamentSelection;
import opt.jmetal.problem.IntegerProblem;
import opt.jmetal.qualityindicator.impl.Epsilon;
import opt.jmetal.qualityindicator.impl.InvertedGenerationalDistancePlus;
import opt.jmetal.qualityindicator.impl.hypervolume.PISAHypervolume;
import opt.jmetal.solution.IntegerSolution;
import opt.jmetal.util.archive.impl.CrowdingDistanceArchive;
import opt.jmetal.util.experiment.Experiment;
import opt.jmetal.util.experiment.ExperimentBuilder;
import opt.jmetal.util.experiment.component.ComputeQualityIndicators;
import opt.jmetal.util.experiment.component.ExecuteAlgorithms;
import opt.jmetal.util.experiment.component.GenerateLatexTablesWithStatistics;
import opt.jmetal.util.experiment.component.GenerateReferenceParetoSetAndFrontFromDoubleSolutions;
import opt.jmetal.util.experiment.util.ExperimentAlgorithm;
import opt.jmetal.util.experiment.util.ExperimentProblem;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

public class IntegerProblemsExperimentConfig {
    // 日志记录
    private static Logger logger = LogManager.getLogger(IntegerProblemsExperimentConfig.class.getName());

    /**
     * 单次运行
     *
     * @param problem
     * @param algorithm
     * @param popSize
     * @param evaluation
     * @throws IOException
     * @Deprecated 方法已过期
     */
    @Deprecated
    public static void singleRunIntegerCode(ExperimentProblem<IntegerSolution> problem, String algorithm, int popSize,
                                            int evaluation, int runs) throws IOException {
        List<String> algorithms = new LinkedList<>();
        algorithms.add(algorithm);

        List<ExperimentAlgorithm<IntegerSolution, List<IntegerSolution>>> algorithmList = configureAlgorithmListIntegerCode(
                algorithms, problem, runs, popSize, evaluation);
        if (algorithmList.isEmpty()) {
            logger.fatal("请确保算法存在.");
            System.exit(1);
        }

        List<ExperimentProblem<IntegerSolution>> problemList = new LinkedList<>();
        problemList.add(problem);

        Experiment<IntegerSolution, List<IntegerSolution>> experiment = new ExperimentBuilder<IntegerSolution, List<IntegerSolution>>(
                "SingleRun").setAlgorithmList(algorithmList).setProblemList(problemList)
                .setExperimentBaseDirectory("result").setOutputParetoFrontFileName("FUN")
                .setOutputParetoSetFileName("VAR")
                .setReferenceFrontDirectory("result" + "/SingleRun/referenceFronts")
                .setIndicatorList(
                        Arrays.asList(new Epsilon<IntegerSolution>(), new PISAHypervolume<IntegerSolution>(),
                                new InvertedGenerationalDistancePlus<IntegerSolution>()))
                .setIndependentRuns(runs).setNumberOfCores(4).setPopulationsize(popSize)
                .setEvaluation(evaluation).build();

        // 运行实验
        new ExecuteAlgorithms<>(experiment).run();

        // 生成pareto参考前沿【如果运行次数较少，可能会导致找不到可行解，最终解集为空，导致生成pareto出错】
        new GenerateReferenceParetoSetAndFrontFromDoubleSolutions(experiment).run();
    }


    /**
     * 实验
     *
     * @param problem
     * @param algorithms
     * @param popSize
     * @param evaluation
     * @param runs
     * @throws IOException
     */
    public static void doExperimentIntegerCode(ExperimentProblem<IntegerSolution> problem, List<String> algorithms,
                                               int popSize, int evaluation, int runs) throws IOException {
        List<ExperimentProblem<IntegerSolution>> problemList = new LinkedList<>();
        problemList.add(problem);

        List<ExperimentAlgorithm<IntegerSolution, List<IntegerSolution>>> algorithmList = configureAlgorithmListIntegerCode(
                algorithms, problem, runs, popSize, evaluation);
        if (algorithmList.isEmpty()) {
            logger.fatal("请确保算法存在.");
            System.exit(1);
        }

        Experiment<IntegerSolution, List<IntegerSolution>> experiment = new ExperimentBuilder<IntegerSolution, List<IntegerSolution>>(
                "Experiment").setAlgorithmList(algorithmList).setProblemList(problemList)
                .setExperimentBaseDirectory("result").setOutputParetoFrontFileName("FUN")
                .setOutputParetoSetFileName("VAR").setReferenceFrontDirectory("result" + "/Experiment/PF")
                .setIndicatorList(
                        Arrays.asList(new Epsilon<IntegerSolution>(), new PISAHypervolume<IntegerSolution>(),
                                new InvertedGenerationalDistancePlus<IntegerSolution>()))
                .setIndependentRuns(runs).setNumberOfCores(4).setPopulationsize(popSize)
                .setEvaluation(evaluation).build();

        // 运行实验
        new ExecuteAlgorithms<>(experiment).run();

        // 1.生成pareto参考前沿【如果运行次数较少，可能会导致找不到可行解，最终解集为空，导致生成pareto出错】
        new GenerateReferenceParetoSetAndFrontFromDoubleSolutions(experiment).run();
        // 2.计算性能指标
        new ComputeQualityIndicators<>(experiment).run();
        // 3.生成latex统计表格
        new GenerateLatexTablesWithStatistics(experiment).run();
    }

    /**
     * 为问题配置算法列表
     *
     * @param algorithmList
     * @param problem
     * @param runs
     * @param popSize
     * @param evaluation
     * @return
     */
    public static List<ExperimentAlgorithm<IntegerSolution, List<IntegerSolution>>> configureAlgorithmListIntegerCode(
            List<String> algorithmList, ExperimentProblem<IntegerSolution> problem, int runs, int popSize,
            int evaluation) {
        List<ExperimentAlgorithm<IntegerSolution, List<IntegerSolution>>> algorithms = new ArrayList<>();
        for (int run = 0; run < runs; run++) {

            if (algorithmList.contains("NSGAII")) {
                Algorithm<List<IntegerSolution>> algorithm = new NSGAIIBuilder<>(problem.getProblem(),
                        new IntegerSBXCrossover(1.0, 20),
                        new IntegerPolynomialMutation(1.0 / problem.getProblem().getNumberOfVariables(), 20.0), popSize)
                        .setMaxEvaluations(evaluation).build();
                algorithms.add(new ExperimentAlgorithm<>(algorithm, problem, run));
            }

            if (algorithmList.contains("NSGAIII")) {
                Algorithm<List<IntegerSolution>> algorithm = new NSGAIIIBuilder<IntegerSolution>(problem.getProblem())
                        .setPopulationSize(popSize).setMaxIterations(evaluation / popSize)
                        .setSelectionOperator(new BinaryTournamentSelection<IntegerSolution>())
                        .setCrossoverOperator(new IntegerSBXCrossover(1.0, 20))
                        .setMutationOperator(
                                new IntegerPolynomialMutation(1.0 / problem.getProblem().getNumberOfVariables(), 20.0))
                        .build();
                algorithms.add(new ExperimentAlgorithm<>(algorithm, problem, run));
            }

            if (algorithmList.contains("SPEA2")) {
                int iters = (int) Math.ceil(1.0 * evaluation / popSize);
                Algorithm<List<IntegerSolution>> algorithm = new SPEA2Builder<IntegerSolution>(problem.getProblem(),
                        new IntegerSBXCrossover(1.0, 10.0),
                        new IntegerPolynomialMutation(1.0 / problem.getProblem().getNumberOfVariables(), 20.0))
                        .setPopulationSize(popSize).setMaxIterations(iters).build();
                algorithms.add(new ExperimentAlgorithm<>(algorithm, problem, run));
            }

            if (algorithmList.contains("MoCell")) {
                Algorithm<List<IntegerSolution>> algorithm = new MOCellBuilder<>((IntegerProblem) problem.getProblem(),
                        new IntegerSBXCrossover(1.0, 20.0),
                        new IntegerPolynomialMutation(1.0 / problem.getProblem().getNumberOfVariables(), 20.0))
                        .setSelectionOperator(new BinaryTournamentSelection<>()).setMaxEvaluations(evaluation)
                        .setPopulationSize(popSize)
                        .setArchive(new CrowdingDistanceArchive<IntegerSolution>(100)).build();// 储备集的大小默认为100
                algorithms.add(new ExperimentAlgorithm<>(algorithm, problem, run));
            }
        }
        return algorithms;
    }
}
