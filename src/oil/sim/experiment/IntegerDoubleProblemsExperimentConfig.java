package oil.sim.experiment;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import opt.jmetal.algorithm.Algorithm;
import opt.jmetal.algorithm.multiobjective.mocell.MOCellBuilder;
import opt.jmetal.algorithm.multiobjective.nsgaii.NSGAIIBuilder;
import opt.jmetal.algorithm.multiobjective.spea2.SPEA2Builder;
import opt.jmetal.operator.impl.crossover.IntegerDoubleSBXCrossover;
import opt.jmetal.operator.impl.mutation.IntegerDoublePolynomialMutation;
import opt.jmetal.problem.Problem;
import opt.jmetal.qualityindicator.impl.Epsilon;
import opt.jmetal.qualityindicator.impl.InvertedGenerationalDistancePlus;
import opt.jmetal.qualityindicator.impl.hypervolume.PISAHypervolume;
import opt.jmetal.solution.IntegerDoubleSolution;
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

public class IntegerDoubleProblemsExperimentConfig {
    // 日志记录
    private static Logger logger = LogManager.getLogger(IntegerDoubleProblemsExperimentConfig.class.getName());

    /**
     * 单次运行
     *
     * @param problem
     * @param algorithm
     * @param popSize
     * @param evaluation
     * @param runs
     * @throws IOException
     */
    public static void singleRunIntegerDoubleCode(ExperimentProblem<IntegerDoubleSolution> problem, String algorithm,
                                                  int popSize, int evaluation, int runs) throws IOException {

        List<String> algorithms = new LinkedList<>();
        algorithms.add(algorithm);

        List<ExperimentAlgorithm<IntegerDoubleSolution, List<IntegerDoubleSolution>>> algorithmList = configureAlgorithmListForIntegerDoubleCode(
                algorithms, problem, runs, popSize, evaluation);
        if (algorithmList.isEmpty()) {
            logger.fatal("请确保算法存在.");
            System.exit(1);
        }

        List<ExperimentProblem<IntegerDoubleSolution>> problemList = new LinkedList<>();
        problemList.add(problem);

        Experiment<IntegerDoubleSolution, List<IntegerDoubleSolution>> experiment = new ExperimentBuilder<IntegerDoubleSolution, List<IntegerDoubleSolution>>(
                "SingleRun").setAlgorithmList(algorithmList).setProblemList(problemList)
                .setExperimentBaseDirectory("result").setOutputParetoFrontFileName("FUN")
                .setOutputParetoSetFileName("VAR")
                .setReferenceFrontDirectory("result" + "/SingleRun/referenceFronts")
                .setIndicatorList(Arrays.asList(new Epsilon<IntegerDoubleSolution>(),
                        new PISAHypervolume<IntegerDoubleSolution>(),
                        new InvertedGenerationalDistancePlus<IntegerDoubleSolution>()))
                .setIndependentRuns(runs).setNumberOfCores(4).build();

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
    public static void doExperimentIntegerDoubleCode(ExperimentProblem<IntegerDoubleSolution> problem,
                                                     List<String> algorithms, int popSize, int evaluation, int runs) throws IOException {
        List<ExperimentProblem<IntegerDoubleSolution>> problemList = new LinkedList<>();
        problemList.add(problem);

        List<ExperimentAlgorithm<IntegerDoubleSolution, List<IntegerDoubleSolution>>> algorithmList = configureAlgorithmListForIntegerDoubleCode(
                algorithms, problem, runs, popSize, evaluation);
        if (algorithmList.isEmpty()) {
            logger.fatal("请确保算法存在.");
            System.exit(1);
        }

        Experiment<IntegerDoubleSolution, List<IntegerDoubleSolution>> experiment = new ExperimentBuilder<IntegerDoubleSolution, List<IntegerDoubleSolution>>(
                "Experiment").setAlgorithmList(algorithmList).setProblemList(problemList)
                .setExperimentBaseDirectory("result").setOutputParetoFrontFileName("FUN")
                .setOutputParetoSetFileName("VAR").setReferenceFrontDirectory("result" + "/Experiment/PF")
                .setIndicatorList(Arrays.asList(new Epsilon<IntegerDoubleSolution>(),
                        new PISAHypervolume<IntegerDoubleSolution>(),
                        new InvertedGenerationalDistancePlus<IntegerDoubleSolution>()))
                .setIndependentRuns(runs).setNumberOfCores(4).build();

        // 运行实验
        new ExecuteAlgorithms<>(experiment).run();

        // 生成pareto参考前沿【如果运行次数较少，可能会导致找不到可行解，最终解集为空，导致生成pareto出错】
        new GenerateReferenceParetoSetAndFrontFromDoubleSolutions(experiment).run();
        // 计算性能指标
        new ComputeQualityIndicators<>(experiment).run();
        // 生成latex统计表格
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
    public static List<ExperimentAlgorithm<IntegerDoubleSolution, List<IntegerDoubleSolution>>> configureAlgorithmListForIntegerDoubleCode(
            List<String> algorithmList, ExperimentProblem<IntegerDoubleSolution> problem, int runs, int popSize,
            int evaluation) {
        List<ExperimentAlgorithm<IntegerDoubleSolution, List<IntegerDoubleSolution>>> algorithms = new ArrayList<>();
        for (int run = 0; run < runs; run++) {

            if (algorithmList.contains("NSGAII")) {
                Algorithm<List<IntegerDoubleSolution>> algorithm = new NSGAIIBuilder<>(problem.getProblem(),
                        new IntegerDoubleSBXCrossover(1.0, 20),
                        new IntegerDoublePolynomialMutation(1.0 / problem.getProblem().getNumberOfVariables(), 20.0),
                        popSize).setMaxEvaluations(evaluation).build();
                algorithms.add(new ExperimentAlgorithm<>(algorithm, problem, run));
            }

            if (algorithmList.contains("SPEA2")) {
                int iters = (int) Math.ceil(1.0 * evaluation / popSize);
                Algorithm<List<IntegerDoubleSolution>> algorithm = new SPEA2Builder<IntegerDoubleSolution>(
                        problem.getProblem(), new IntegerDoubleSBXCrossover(1.0, 10.0),
                        new IntegerDoublePolynomialMutation(1.0 / problem.getProblem().getNumberOfVariables(), 20.0))
                        .setPopulationSize(popSize).setMaxIterations(iters).build();
                algorithms.add(new ExperimentAlgorithm<>(algorithm, problem, run));
            }

            if (algorithmList.contains("MoCell")) {
                Algorithm<List<IntegerDoubleSolution>> algorithm = new MOCellBuilder<IntegerDoubleSolution>(
                        (Problem<IntegerDoubleSolution>) problem.getProblem(), new IntegerDoubleSBXCrossover(1.0, 20.0),
                        new IntegerDoublePolynomialMutation(1.0 / problem.getProblem().getNumberOfVariables(), 20.0))
                        .setMaxEvaluations(evaluation).setPopulationSize(popSize).build();// 储备集的大小默认为种群大小
                algorithms.add(new ExperimentAlgorithm<>(algorithm, problem, run));
            }
        }
        return algorithms;
    }
}
