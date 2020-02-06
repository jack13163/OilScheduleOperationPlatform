package com.sim.experiment;

import com.sim.oil.cop.OilScheduleConstrainedOptimizationProblem;
import com.sim.oil.op.OilScheduleOptimizationProblem;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.uma.jmetal.algorithm.Algorithm;
import org.uma.jmetal.algorithm.multiobjective.mocell.MOCellBuilder;
import org.uma.jmetal.algorithm.multiobjective.moead.MOEADBuilder;
import org.uma.jmetal.algorithm.multiobjective.nsgaii.NSGAIIBuilder;
import org.uma.jmetal.algorithm.multiobjective.nsgaiii.NSGAIIIBuilder;
import org.uma.jmetal.algorithm.multiobjective.moead.MOEADBuilder.Variant;
import org.uma.jmetal.algorithm.multiobjective.spea2.SPEA2Builder;
import org.uma.jmetal.operator.impl.crossover.SBXCrossover;
import org.uma.jmetal.operator.impl.mutation.PolynomialMutation;
import org.uma.jmetal.operator.impl.selection.BinaryTournamentSelection;
import org.uma.jmetal.problem.multiobjective.*;
import org.uma.jmetal.problem.multiobjective.cdtlz.*;
import org.uma.jmetal.qualityindicator.impl.Epsilon;
import org.uma.jmetal.qualityindicator.impl.InvertedGenerationalDistancePlus;
import org.uma.jmetal.qualityindicator.impl.hypervolume.PISAHypervolume;
import org.uma.jmetal.solution.DoubleSolution;
import org.uma.jmetal.util.archive.impl.CrowdingDistanceArchive;
import org.uma.jmetal.util.experiment.Experiment;
import org.uma.jmetal.util.experiment.ExperimentBuilder;
import org.uma.jmetal.util.experiment.component.ComputeQualityIndicators;
import org.uma.jmetal.util.experiment.component.ExecuteAlgorithms;
import org.uma.jmetal.util.experiment.component.GenerateLatexTablesWithStatistics;
import org.uma.jmetal.util.experiment.component.GenerateReferenceParetoSetAndFrontFromDoubleSolutions;
import org.uma.jmetal.util.experiment.util.ExperimentAlgorithm;
import org.uma.jmetal.util.experiment.util.ExperimentProblem;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

public class TestProblemsExperimentConfig {
    // 日志记录
    private static Logger logger = LogManager.getLogger(TestProblemsExperimentConfig.class.getName());

    /**
     * 获取测试问题列表
     *
     * @param problemList
     * @param numberOfVariables
     * @param numberOfObjectives
     * @return
     * @throws IOException
     */
    public static List<ExperimentProblem<DoubleSolution>> getTestProblemsList(List<String> problemList,
                                                                              int numberOfVariables, int numberOfObjectives) throws IOException {

        List<ExperimentProblem<DoubleSolution>> problems = new LinkedList<>();
        for (int i = 0; i < problemList.size(); i++) {
            String problem = problemList.get(i);

            if (problem.equals("EDF_PS") || problem.equals("EDF_TSS")) {
                problems.add(
                        new ExperimentProblem<DoubleSolution>(new OilScheduleConstrainedOptimizationProblem(problem)));
            } else if (problem.equals("BT")) {
                problems.add(new ExperimentProblem<DoubleSolution>(new OilScheduleOptimizationProblem(problem)));
            } else if (problem.equals("C1_DTLZ1")) {
                // C1_DTLZ1有两个参数，第一个参数为决策变量个数，第二个参数为目标函数的个数，包含一个约束条件
                problems.add(
                        new ExperimentProblem<DoubleSolution>(new C1_DTLZ1(numberOfVariables, numberOfObjectives)));
            } else if (problem.startsWith("C1_DTLZ3")) {
                // C1_DTLZ3在实现上只允许目标个数为3/5/8/10/15
                int numOfObject = Integer.parseInt(problem.substring(problem.indexOf("-") + 1));
                problems.add(new ExperimentProblem<DoubleSolution>(new C1_DTLZ3(numberOfVariables, numOfObject)));
            } else if (problem.equals("C2_DTLZ2")) {
                // C2_DTLZ2有两个参数，第一个参数为决策变量个数，第二个参数为目标函数的个数，包含一个约束条件
                problems.add(
                        new ExperimentProblem<DoubleSolution>(new C2_DTLZ2(numberOfVariables, numberOfObjectives)));
            } else if (problem.equals("C3_DTLZ1")) {
                // C3_DTLZ1有两个参数，第一个参数为决策变量个数，第二个参数为目标函数的个数，约束条件个数可控，这里暂设为1
                problems.add(
                        new ExperimentProblem<DoubleSolution>(new C3_DTLZ1(numberOfVariables, numberOfObjectives, 1)));
            } else if (problem.equals("C3_DTLZ4")) {
                // C3_DTLZ4有三个参数，第一个参数为决策变量个数，第二个参数为目标函数的个数，第三个约束条件个数可控，这里暂设为1
                problems.add(
                        new ExperimentProblem<DoubleSolution>(new C3_DTLZ4(numberOfVariables, numberOfObjectives, 1)));
            } else if (problem.startsWith("ConvexC2_DTLZ2")) {
                // ConvexC2_DTLZ2，在实现上只允许目标个数为3/5/8/10/15
                int numOfObject = Integer.parseInt(problem.substring(problem.indexOf("-") + 1));
                problems.add(new ExperimentProblem<DoubleSolution>(new ConvexC2_DTLZ2(numberOfVariables, numOfObject)));
            } else if (problem.equals("Binh2")) {
                problems.add(new ExperimentProblem<DoubleSolution>(new Binh2()));
            } else if (problem.equals("ConstrEx")) {
                problems.add(new ExperimentProblem<DoubleSolution>(new ConstrEx()));
            } else if (problem.equals("Golinski")) {
                problems.add(new ExperimentProblem<DoubleSolution>(new Golinski()));
            } else if (problem.equals("Srinivas")) {
                problems.add(new ExperimentProblem<DoubleSolution>(new Srinivas()));
            } else if (problem.equals("Tanaka")) {
                problems.add(new ExperimentProblem<DoubleSolution>(new Tanaka()));
            } else if (problem.equals("Water")) {
                problems.add(new ExperimentProblem<DoubleSolution>(new Water()));
            }
        }
        return problems;
    }

    /**
     * 测试问题实验
     *
     * @param problems
     * @param algorithmList
     * @param popSize
     * @param evaluation
     * @param runs
     * @throws IOException
     */
    public static void doTestExperiment(List<ExperimentProblem<DoubleSolution>> problems, List<String> algorithmList,
                                        int popSize, int evaluation, int runs) throws IOException {

        List<ExperimentAlgorithm<DoubleSolution, List<DoubleSolution>>> algorithms = configureAlgorithmListForTest(
                algorithmList, problems, runs, popSize, evaluation);

        if (algorithmList.size() * problems.size() * runs != algorithms.size()) {
            logger.fatal("请确保算法或问题存在.");
            System.exit(1);
        }

        Experiment<DoubleSolution, List<DoubleSolution>> experiment = new ExperimentBuilder<DoubleSolution, List<DoubleSolution>>(
                "Experiment").setAlgorithmList(algorithms).setProblemList(problems).setExperimentBaseDirectory("result")
                .setOutputParetoFrontFileName("FUN").setOutputParetoSetFileName("VAR")
                .setReferenceFrontDirectory("result" + "/Experiment/PF")
                .setIndicatorList(
                        Arrays.asList(new Epsilon<DoubleSolution>(), new PISAHypervolume<DoubleSolution>(),
                                new InvertedGenerationalDistancePlus<DoubleSolution>()))
                .setIndependentRuns(runs).setNumberOfCores(4).setPopulationsize(popSize)
                .setEvaluation(evaluation).build();

        // 运行实验
        new ExecuteAlgorithms<>(experiment).run();
        // 生成pareto参考前沿
        new GenerateReferenceParetoSetAndFrontFromDoubleSolutions(experiment).run();
        // 计算性能指标【The front is empty】
        new ComputeQualityIndicators<>(experiment).run();
        // 生成latex统计表格
        new GenerateLatexTablesWithStatistics(experiment).run();
    }

    /**
     * 为实数编码问题配置算法列表
     *
     * @param algorithmList
     * @param problems
     * @param runs
     * @param popSize
     * @param evaluation
     * @return
     */
    public static List<ExperimentAlgorithm<DoubleSolution, List<DoubleSolution>>> configureAlgorithmListForTest(
            List<String> algorithmList, List<ExperimentProblem<DoubleSolution>> problems, int runs, int popSize,
            int evaluation) {
        List<ExperimentAlgorithm<DoubleSolution, List<DoubleSolution>>> algorithms = new ArrayList<>();

        for (int i = 0; i < problems.size(); i++) {
            ExperimentProblem<DoubleSolution> problem = problems.get(i);

            for (int run = 0; run < runs; run++) {

                if (algorithmList.contains("NSGAII")) {
                    Algorithm<List<DoubleSolution>> algorithm = new NSGAIIBuilder<>(problem.getProblem(),
                            new SBXCrossover(1.0, 20),
                            new PolynomialMutation(1.0 / problem.getProblem().getNumberOfVariables(), 20.0), popSize)
                            .setMaxEvaluations(evaluation).build();
                    algorithms.add(new ExperimentAlgorithm<>(algorithm, problem, run));
                }

                if (algorithmList.contains("NSGAIII")) {
                    Algorithm<List<DoubleSolution>> algorithm = new NSGAIIIBuilder<DoubleSolution>(problem.getProblem())
                            .setPopulationSize(popSize).setMaxIterations(evaluation / popSize)
                            .setSelectionOperator(new BinaryTournamentSelection<DoubleSolution>())
                            .setCrossoverOperator(new SBXCrossover(1.0, 20))
                            .setMutationOperator(
                                    new PolynomialMutation(1.0 / problem.getProblem().getNumberOfVariables(), 20.0))
                            .build();
                    algorithms.add(new ExperimentAlgorithm<>(algorithm, problem, run));
                }

                if (algorithmList.contains("cMOEAD")) {
                    Algorithm<List<DoubleSolution>> algorithm = new MOEADBuilder(problem.getProblem(),
                            Variant.ConstraintMOEAD).setPopulationSize(popSize).setMaxEvaluations(evaluation).build();
                    algorithms.add(new ExperimentAlgorithm<>(algorithm, problem, run));
                }

                if (algorithmList.contains("SPEA2")) {
                    int iters = (int) Math.ceil(1.0 * evaluation / popSize);
                    Algorithm<List<DoubleSolution>> algorithm = new SPEA2Builder<DoubleSolution>(problem.getProblem(),
                            new SBXCrossover(1.0, 10.0),
                            new PolynomialMutation(1.0 / problem.getProblem().getNumberOfVariables(), 20.0))
                            .setPopulationSize(popSize).setMaxIterations(iters).build();
                    algorithms.add(new ExperimentAlgorithm<>(algorithm, problem, run));
                }

                if (algorithmList.contains("MoCell")) {
                    Algorithm<List<DoubleSolution>> algorithm = new MOCellBuilder<>(problem.getProblem(),
                            new SBXCrossover(1.0, 20.0),
                            new PolynomialMutation(1.0 / problem.getProblem().getNumberOfVariables(), 20.0))
                            .setSelectionOperator(new BinaryTournamentSelection<>())
                            .setMaxEvaluations(evaluation).setPopulationSize(popSize)
                            .setArchive(new CrowdingDistanceArchive<DoubleSolution>(100)).build();// 储备集的大小默认为100
                    algorithms.add(new ExperimentAlgorithm<>(algorithm, problem, run));
                }
            }
        }

        return algorithms;
    }
}
