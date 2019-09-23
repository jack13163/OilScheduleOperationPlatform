package com.sim.experiment;

import java.io.IOException;
import java.util.List;

import org.uma.jmetal.qualityindicator.impl.GenericIndicator;
import org.uma.jmetal.solution.Solution;
import org.uma.jmetal.util.JMetalLogger;
import org.uma.jmetal.util.SolutionListUtils;
import org.uma.jmetal.util.experiment.Experiment;
import org.uma.jmetal.util.experiment.component.ComputeQualityIndicators;
import org.uma.jmetal.util.experiment.util.ExperimentAlgorithm;
import org.uma.jmetal.util.front.Front;
import org.uma.jmetal.util.front.imp.ArrayFront;
import org.uma.jmetal.util.front.util.FrontNormalizer;
import org.uma.jmetal.util.front.util.FrontUtils;
import org.uma.jmetal.util.point.PointSolution;

import com.sim.common.NormalizationHelper;

/**
 * 根据实验需要而重写的计算指标值的方法
 * 
 * @author Administrator
 *
 * @param <S>
 * @param <Result>
 */
public class ExperimentComputeQualityIndicators<S extends Solution<?>, Result extends List<S>>
		extends ComputeQualityIndicators<S, Result> {

	public ExperimentComputeQualityIndicators(Experiment<S, Result> experiment) {
		super(experiment);
	}

	@Override
	@SuppressWarnings("unchecked")
	public void run() throws IOException {
		resetIndicatorFiles();

		// 1. 读取参考前沿面
		JMetalLogger.logger.info("RF: result/Experiment/PF/oilschedule.pf");
		Front referenceFront = new ArrayFront("result/Experiment/PF/oilschedule.pf");

		// 2. 根据所有运行结果中的最大值和最小值标准化
		double[][] maxminvalue = NormalizationHelper.getMaxMinObjectValue();
		FrontNormalizer frontNormalizer = new FrontNormalizer(maxminvalue[0], maxminvalue[1]);

		Front normalizedReferenceFront = frontNormalizer.normalize(referenceFront);

		for (GenericIndicator<S> indicator : experiment.getIndicatorList()) {
			// 3. 以规范化后的整个运行结果所产生的的解为参考平面
			indicator.setReferenceParetoFront(normalizedReferenceFront);

			for (ExperimentAlgorithm<?, Result> algorithm : experiment.getAlgorithmList()) {

				JMetalLogger.logger.info("Computing " + algorithm.getAlgorithmTag() + " on " + algorithm.getProblemTag()
						+ " run " + algorithm.getRunId() + " indicator: " + indicator.getName());
				Result result = (Result) SolutionListUtils
						.getNondominatedSolutions(algorithm.getAlgorithm().getResult());

				// 4. 运行结果前沿
				Front front = new ArrayFront(result);

				if (front.getNumberOfPoints() == 0) {
					JMetalLogger.logger.severe("运行结果为空，无法计算其指标");
					continue;
				} else {
					// 5. 标准化运行结果前沿
					Front normalizedFront = frontNormalizer.normalize(front);
					List<PointSolution> normalizedPopulation = FrontUtils.convertFrontToSolutionList(normalizedFront);

					// 6. 计算指标值
					Double indicatorValue = (Double) indicator.evaluate((List<S>) normalizedPopulation);

					// 7. 写入指标值到当前目录下的指标文件中
					String qualityIndicatorFile = experiment.getExperimentBaseDirectory() + "/data/"
							+ algorithm.getAlgorithmTag() + "/" + algorithm.getProblemTag() + "/" + indicator.getName();
					writeQualityIndicatorValueToFile(indicatorValue, qualityIndicatorFile);
				}

				JMetalLogger.logger.info("Computing " + algorithm.getAlgorithmTag() + " on " + algorithm.getProblemTag()
						+ " run " + algorithm.getRunId() + " indicator: " + indicator.getName() + " per generation.");
				int iterations = (int) Math.ceil(experiment.getEvaluation() / experiment.getPopulationsize());
				String solutionFunListFilePath = experiment.getExperimentBaseDirectory() + "/data/"
						+ algorithm.getAlgorithmTag() + "/" + algorithm.getProblemTag() + "/FUN" + algorithm.getRunId()
						+ ".list";
				List<S> solutionFunList = (List<S>) FrontUtils
						.convertFrontToSolutionList(new ArrayFront(solutionFunListFilePath));

				for (int i = 0; i < iterations; i++) {

					List<S> solutionList = solutionFunList.subList(i * experiment.getPopulationsize(),
							(i + 1) * experiment.getPopulationsize());
					Front normalizedFront = frontNormalizer.normalize(new ArrayFront(solutionList));
					List<PointSolution> normalizedPopulation = FrontUtils.convertFrontToSolutionList(normalizedFront);

					// 6. 计算指标值
					Double indicatorValue = (Double) indicator.evaluate((List<S>) normalizedPopulation);

					// 7. 写入指标值到当前目录下的指标文件中
					String qualityIndicatorFile = experiment.getExperimentBaseDirectory() + "/data/"
							+ algorithm.getAlgorithmTag() + "/" + algorithm.getProblemTag() + "/" + indicator.getName()
							+ ".r" + algorithm.getRunId();
					writeQualityIndicatorValueToFile(indicatorValue, qualityIndicatorFile);
				}
			}
		}
		findBestIndicatorFronts(experiment);
		writeSummaryFile(experiment);
	}
}
