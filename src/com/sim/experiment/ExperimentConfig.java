package com.sim.experiment;

import com.sim.oil.cop.OilScheduleConstrainedOptimizationProblem;
import com.sim.oil.op.OilScheduleOptimizationProblem;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.uma.jmetal.algorithm.Algorithm;
import org.uma.jmetal.algorithm.multiobjective.mocell.MOCellBuilder;
import org.uma.jmetal.algorithm.multiobjective.moead.MOEADBuilder;
import org.uma.jmetal.algorithm.multiobjective.moead.MOEADBuilder.Variant;
import org.uma.jmetal.algorithm.multiobjective.nsgaii.NSGAIIBuilder;
import org.uma.jmetal.algorithm.multiobjective.nsgaiii.NSGAIIIBuilder;
import org.uma.jmetal.algorithm.multiobjective.spea2.SPEA2Builder;
import org.uma.jmetal.operator.impl.crossover.IntegerDoubleSBXCrossover;
import org.uma.jmetal.operator.impl.crossover.IntegerSBXCrossover;
import org.uma.jmetal.operator.impl.crossover.SBXCrossover;
import org.uma.jmetal.operator.impl.mutation.IntegerDoublePolynomialMutation;
import org.uma.jmetal.operator.impl.mutation.IntegerPolynomialMutation;
import org.uma.jmetal.operator.impl.mutation.PolynomialMutation;
import org.uma.jmetal.operator.impl.selection.BinaryTournamentSelection;
import org.uma.jmetal.problem.IntegerProblem;
import org.uma.jmetal.problem.Problem;
import org.uma.jmetal.problem.multiobjective.*;
import org.uma.jmetal.problem.multiobjective.cdtlz.*;
import org.uma.jmetal.qualityindicator.impl.*;
import org.uma.jmetal.qualityindicator.impl.hypervolume.PISAHypervolume;
import org.uma.jmetal.solution.DoubleSolution;
import org.uma.jmetal.solution.IntegerDoubleSolution;
import org.uma.jmetal.solution.IntegerSolution;
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

/**
 * 配置时需要注意，需要先配置problem，后配置algorithm
 * 
 * @author Administrator
 */
public class ExperimentConfig {
	// 日志记录
	private static Logger logger = LogManager.getLogger(ExperimentConfig.class.getName());

	/**
	 * 单次实验【整数编码】
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
	 * 多次实验【整数编码】
	 * 
	 * @param experimentName
	 * @param problem
	 * @param algorithms
	 * @param popSize
	 * @param evaluation
	 * @param experimentBaseDirectory
	 * @param runs
	 * @throws IOException
	 * @Deprecated 方法已过期
	 */
	@Deprecated
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
	 * 为问题配置算法列表【整数编码】
	 *
	 * @param problemList
	 * @return
	 * @Deprecated 方法已过期
	 */
	@Deprecated
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

	/**
	 * 单次实验【混合编码】
	 * 
	 * @param problem
	 * @param algorithm
	 * @param popSize
	 * @param evaluation
	 * @throws IOException
	 * @Deprecated 方法已过期
	 */
	@Deprecated
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
	 * 多次实验【混合编码】
	 *
	 * @param experimentName
	 * @param problem
	 * @param algorithms
	 * @param popSize
	 * @param evaluation
	 * @param experimentBaseDirectory
	 * @param runs
	 * @throws IOException
	 * @Deprecated 方法已过期
	 */
	@Deprecated
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
	 * 为问题配置算法列表【混合编码】
	 * 
	 * @param problemList
	 * @return
	 * @Deprecated 方法已过期
	 */
	@Deprecated
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

		// 1.生成pareto参考前沿
		new ExperimentGenerateReferenceParetoSetAndFrontFromDoubleSolutions(experiment).run();
		// 2.计算性能指标
		new ExperimentComputeQualityIndicators<>(experiment).run();
		// 3.生成latex统计表格
		new GenerateLatexTablesWithStatistics(experiment).run();
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
	 * @param problem
	 * @param algorithms
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