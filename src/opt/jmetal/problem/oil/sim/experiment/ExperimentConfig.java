package opt.jmetal.problem.oil.sim.experiment;

import opt.jmetal.algorithm.Algorithm;
import opt.jmetal.algorithm.multiobjective.ibea.IBEABuilder;
import opt.jmetal.algorithm.multiobjective.mocell.MOCellBuilder;
import opt.jmetal.algorithm.multiobjective.moead.MOEADBuilder;
import opt.jmetal.algorithm.multiobjective.moead.MOEADBuilder.Variant;
import opt.jmetal.algorithm.multiobjective.nsgaii.NSGAIIBuilder;
import opt.jmetal.algorithm.multiobjective.nsgaiii.NSGAIIIBuilder;
import opt.jmetal.algorithm.multiobjective.pesa2.PESA2Builder;
import opt.jmetal.algorithm.multiobjective.spea2.SPEA2Builder;
import opt.jmetal.operator.impl.crossover.DifferentialEvolutionCrossover;
import opt.jmetal.operator.impl.crossover.SBXCrossover;
import opt.jmetal.operator.impl.mutation.PolynomialMutation;
import opt.jmetal.operator.impl.selection.BinaryTournamentSelection;
import opt.jmetal.qualityindicator.impl.*;
import opt.jmetal.qualityindicator.impl.hypervolume.PISAHypervolume;
import opt.jmetal.solution.DoubleSolution;
import opt.jmetal.util.archive.impl.CrowdingDistanceArchive;
import opt.jmetal.util.experiment.Experiment;
import opt.jmetal.util.experiment.ExperimentBuilder;
import opt.jmetal.util.experiment.component.ExecuteAlgorithms;
import opt.jmetal.util.experiment.component.GenerateReferenceParetoSetAndFrontFromDoubleSolutions;
import opt.jmetal.util.experiment.util.ExperimentAlgorithm;
import opt.jmetal.util.experiment.util.ExperimentProblem;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

/**
 * 配置时需要注意，需要先配置problem，后配置algorithm
 *
 * @author Administrator
 */
public class ExperimentConfig {
    // 日志记录
    private static Logger logger = LogManager.getLogger(ExperimentConfig.class.getName());

    /**
     * 单次实验【实数编码】
     *
     * @param problem
     * @param algorithm
     * @param popSize
     * @param evaluation
     * @throws IOException
     */
    public static void singleRunDoubleCode(ExperimentProblem<DoubleSolution> problem, String algorithm, int popSize,
                                           int evaluation, int runs) throws IOException {
        List<String> algorithms = new LinkedList<>();
        algorithms.add(algorithm);

        List<ExperimentAlgorithm<DoubleSolution, List<DoubleSolution>>> algorithmList = configureAlgorithmListDoubleCode(
                algorithms, problem, runs, popSize, evaluation);
        if (algorithmList.isEmpty()) {
            logger.fatal("请确保算法存在.");
            System.exit(1);
        }

        List<ExperimentProblem<DoubleSolution>> problemList = new LinkedList<>();
        problemList.add(problem);

        Experiment<DoubleSolution, List<DoubleSolution>> experiment = new ExperimentBuilder<DoubleSolution, List<DoubleSolution>>(
                "SingleRun").setAlgorithmList(algorithmList).setProblemList(problemList)
                .setExperimentBaseDirectory("result").setOutputParetoFrontFileName("FUN")
                .setOutputParetoSetFileName("VAR")
                .setReferenceFrontDirectory("result/SingleRun/referenceFronts")
                .setIndicatorList(
                        Arrays.asList(new Epsilon<DoubleSolution>(), new PISAHypervolume<DoubleSolution>(),
                                new InvertedGenerationalDistancePlus<DoubleSolution>()))
                .setIndependentRuns(runs).setNumberOfCores(4).setPopulationsize(popSize)
                .setEvaluation(evaluation).build();

        // 运行实验
        new ExecuteAlgorithms<>(experiment).run();

        // 生成pareto参考前沿【如果运行次数较少，可能会导致找不到可行解，最终解集为空，导致生成pareto出错】
        new GenerateReferenceParetoSetAndFrontFromDoubleSolutions(experiment).run();
    }

    /**
     * 多次实验【实数编码】
     *
     * @param problemList
     * @param algorithms
     * @param popSize
     * @param evaluation
     * @param runs
     * @throws IOException
     */
    public static void doExperimentDoubleCode(List<ExperimentProblem<DoubleSolution>> problemList,
                                              List<String> algorithms, int popSize, int evaluation, int runs) throws IOException {

        List<ExperimentAlgorithm<DoubleSolution, List<DoubleSolution>>> algorithmList = configureAlgorithmListDoubleCode(
                algorithms, problemList, runs, popSize, evaluation);

        if (algorithmList.isEmpty()) {
            logger.fatal("请确保算法存在.");
            System.exit(1);
        }

        Experiment<DoubleSolution, List<DoubleSolution>> experiment = new ExperimentBuilder<DoubleSolution, List<DoubleSolution>>(
                "Experiment").setAlgorithmList(algorithmList).setProblemList(problemList)
                .setExperimentBaseDirectory("result").setOutputParetoFrontFileName("FUN")
                .setOutputParetoSetFileName("VAR").setReferenceFrontDirectory("result/Experiment/PF")
                .setIndicatorList(
                        Arrays.asList(new Epsilon<DoubleSolution>(),
                                new GeneralizedSpread<DoubleSolution>(),
                                new PISAHypervolume<DoubleSolution>(),//含有文献说明
                                new GenerationalDistance<DoubleSolution>(),
                                new InvertedGenerationalDistance<DoubleSolution>(),
                                new InvertedGenerationalDistancePlus<DoubleSolution>()))
                .setIndependentRuns(runs).setNumberOfCores(0).setPopulationsize(popSize)
                .setEvaluation(evaluation).build();

        // 运行实验
        new ExecuteAlgorithms<>(experiment).run();
    }

    /**
     * 为问题配置算法列表【实数编码】
     *
     * @param algorithmList
     * @param problem
     * @param runs
     * @param popSize
     * @param evaluation
     * @return
     */
    public static List<ExperimentAlgorithm<DoubleSolution, List<DoubleSolution>>> configureAlgorithmListDoubleCode(
            List<String> algorithmList, ExperimentProblem<DoubleSolution> problem, int runs, int popSize,
            int evaluation) {
        List<ExperimentAlgorithm<DoubleSolution, List<DoubleSolution>>> algorithms = new ArrayList<>();
        for (int run = 0; run < runs; run++) {

            if (algorithmList.contains("IBEA")) {
                Algorithm<List<DoubleSolution>> algorithm = new IBEABuilder(problem.getProblem())
                        .setPopulationSize(popSize)
                        .setArchiveSize(popSize)
                        .setCrossover(new SBXCrossover(0.9, 20))
                        .setMutation(new PolynomialMutation(1.0 / problem.getProblem().getNumberOfVariables(), 20.0))
                        .setMaxEvaluations(evaluation).build();
                algorithms.add(new ExperimentAlgorithm<>(algorithm, problem, run));
            }

            if (algorithmList.contains("PESA2")) {
                Algorithm<List<DoubleSolution>> algorithm = new PESA2Builder<>(problem.getProblem(),
                        new SBXCrossover(0.9, 20),
                        new PolynomialMutation(1.0 / problem.getProblem().getNumberOfVariables(), 20.0))
                        .setPopulationSize(popSize)
                        .setArchiveSize(popSize)
                        .setMaxEvaluations(evaluation).build();
                algorithms.add(new ExperimentAlgorithm<>(algorithm, problem, run));
            }

            if (algorithmList.contains("NSGAII")) {
                Algorithm<List<DoubleSolution>> algorithm = new NSGAIIBuilder<>(problem.getProblem(),
                        new SBXCrossover(0.9, 20),
                        new PolynomialMutation(1.0 / problem.getProblem().getNumberOfVariables(), 20.0), popSize)
                        .setMaxEvaluations(evaluation).build();
                algorithms.add(new ExperimentAlgorithm<>(algorithm, problem, run));
            }

            if (algorithmList.contains("NSGAIII")) {
                Algorithm<List<DoubleSolution>> algorithm = new NSGAIIIBuilder<DoubleSolution>(problem.getProblem())
                        .setPopulationSize(popSize).setMaxIterations(evaluation / popSize)
                        .setSelectionOperator(new BinaryTournamentSelection<DoubleSolution>())
                        .setCrossoverOperator(new SBXCrossover(0.9, 20))
                        .setMutationOperator(new PolynomialMutation(1.0 / problem.getProblem().getNumberOfVariables(), 20.0))
                        .build();
                algorithms.add(new ExperimentAlgorithm<>(algorithm, problem, run));
            }

            if (algorithmList.contains("cMOEAD")) {
                Algorithm<List<DoubleSolution>> algorithm = new MOEADBuilder(problem.getProblem(), Variant.ConstraintMOEAD)
                        .setPopulationSize(popSize)
                        .setCrossover(new DifferentialEvolutionCrossover())
                        .setMutation(new PolynomialMutation(1.0 / problem.getProblem().getNumberOfVariables(), 20.0))
                        .setMaxEvaluations(evaluation).build();
                algorithms.add(new ExperimentAlgorithm<>(algorithm, problem, run));
            }

            if (algorithmList.contains("SPEA2")) {
                int iters = (int) Math.ceil(1.0 * evaluation / popSize);
                Algorithm<List<DoubleSolution>> algorithm = new SPEA2Builder<DoubleSolution>(problem.getProblem(),
                        new SBXCrossover(0.9, 20.0),
                        new PolynomialMutation(1.0 / problem.getProblem().getNumberOfVariables(), 20.0))
                        .setPopulationSize(popSize).setMaxIterations(iters).build();
                algorithms.add(new ExperimentAlgorithm<>(algorithm, problem, run));
            }

            if (algorithmList.contains("MoCell")) {
                Algorithm<List<DoubleSolution>> algorithm = new MOCellBuilder<>(problem.getProblem(),
                        new SBXCrossover(0.9, 20.0),
                        new PolynomialMutation(1.0 / problem.getProblem().getNumberOfVariables(), 20.0))
                        .setSelectionOperator(new BinaryTournamentSelection<>()).setMaxEvaluations(evaluation)
                        .setPopulationSize(popSize).setArchive(new CrowdingDistanceArchive<DoubleSolution>(100))
                        .build();// 储备集的大小默认为100
                algorithms.add(new ExperimentAlgorithm<>(algorithm, problem, run));
            }
        }
        return algorithms;
    }

    /**
     * 为问题配置算法列表【实数编码】
     *
     * @param algorithmList
     * @param problems
     * @param runs
     * @param popSize
     * @param evaluation
     * @return
     */
    public static List<ExperimentAlgorithm<DoubleSolution, List<DoubleSolution>>> configureAlgorithmListDoubleCode(
            List<String> algorithmList, List<ExperimentProblem<DoubleSolution>> problems, int runs, int popSize,
            int evaluation) {
        List<ExperimentAlgorithm<DoubleSolution, List<DoubleSolution>>> algorithms = new ArrayList<>();
        for (ExperimentProblem<DoubleSolution> problem : problems) {
            algorithms.addAll(configureAlgorithmListDoubleCode(algorithmList, problem, runs, popSize, evaluation));
        }
        return algorithms;
    }
}