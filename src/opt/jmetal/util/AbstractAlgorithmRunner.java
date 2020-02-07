package opt.jmetal.util;

import oil.sim.common.NormalizationHelper;
import opt.jmetal.qualityindicator.impl.*;
import opt.jmetal.qualityindicator.impl.hypervolume.PISAHypervolume;
import opt.jmetal.solution.Solution;
import opt.jmetal.util.fileoutput.SolutionListOutput;
import opt.jmetal.util.fileoutput.impl.DefaultFileOutputContext;
import opt.jmetal.util.front.Front;
import opt.jmetal.util.front.imp.ArrayFront;
import opt.jmetal.util.front.util.FrontNormalizer;
import opt.jmetal.util.front.util.FrontUtils;
import opt.jmetal.util.point.PointSolution;
import opt.jmetal.util.pseudorandom.JMetalRandom;

import java.io.FileNotFoundException;
import java.util.List;

/**
 * Abstract class for Runner classes
 *
 * @author Antonio J. Nebro <antonio@lcc.uma.es>
 */
public abstract class AbstractAlgorithmRunner {
	/**
	 * Write the population into two files and prints some data on screen
	 * 
	 * @param population
	 */
	public static void printFinalSolutionSet(List<? extends Solution<?>> population) {

		new SolutionListOutput(population).setSeparator("\t")
				.setVarFileOutputContext(new DefaultFileOutputContext("VAR.tsv"))
				.setFunFileOutputContext(new DefaultFileOutputContext("FUN.tsv")).print();

		JMetalLogger.logger.info("Random seed: " + JMetalRandom.getInstance().getSeed());
		JMetalLogger.logger.info("Objectives values have been written to file FUN.tsv");
		JMetalLogger.logger.info("Variables values have been written to file VAR.tsv");
	}

	/**
	 * Print all the available quality indicators
	 * 
	 * @param population
	 * @param paretoFrontFile
	 * @throws FileNotFoundException
	 */
	public static <S extends Solution<?>> void printQualityIndicators(List<S> population, String paretoFrontFile)
			throws FileNotFoundException {
		Front referenceFront = new ArrayFront(paretoFrontFile);

		// 设定最大值和最小值
		String[] tmp = paretoFrontFile.split("/");
		String problemName = tmp[tmp.length - 1];
		problemName = problemName.substring(0, problemName.indexOf("."));
		double[][] maxminvalue = NormalizationHelper.getMaxMinObjectValue(problemName);
		FrontNormalizer frontNormalizer = new FrontNormalizer(maxminvalue[0], maxminvalue[1]);

		Front normalizedReferenceFront = frontNormalizer.normalize(referenceFront);
		Front normalizedFront = frontNormalizer.normalize(new ArrayFront(population));
		List<PointSolution> normalizedPopulation = FrontUtils.convertFrontToSolutionList(normalizedFront);

		String outputString = "\n";
		outputString += "Hypervolume (N) : "
				+ new PISAHypervolume<PointSolution>(normalizedReferenceFront).evaluate(normalizedPopulation) + "\n";
		outputString += "Hypervolume     : " + new PISAHypervolume<S>(referenceFront).evaluate(population) + "\n";
		outputString += "Epsilon (N)     : "
				+ new Epsilon<PointSolution>(normalizedReferenceFront).evaluate(normalizedPopulation) + "\n";
		outputString += "Epsilon         : " + new Epsilon<S>(referenceFront).evaluate(population) + "\n";
		outputString += "GD (N)          : "
				+ new GenerationalDistance<PointSolution>(normalizedReferenceFront).evaluate(normalizedPopulation)
				+ "\n";
		outputString += "GD              : " + new GenerationalDistance<S>(referenceFront).evaluate(population) + "\n";
		outputString += "IGD (N)         : " + new InvertedGenerationalDistance<PointSolution>(normalizedReferenceFront)
				.evaluate(normalizedPopulation) + "\n";
		outputString += "IGD             : " + new InvertedGenerationalDistance<S>(referenceFront).evaluate(population)
				+ "\n";
		outputString += "IGD+ (N)        : "
				+ new InvertedGenerationalDistancePlus<PointSolution>(normalizedReferenceFront)
						.evaluate(normalizedPopulation)
				+ "\n";
		outputString += "IGD+            : "
				+ new InvertedGenerationalDistancePlus<S>(referenceFront).evaluate(population) + "\n";
		outputString += "Spread (N)      : "
				+ new Spread<PointSolution>(normalizedReferenceFront).evaluate(normalizedPopulation) + "\n";
		outputString += "Spread          : " + new Spread<S>(referenceFront).evaluate(population) + "\n";
//    outputString += "R2 (N)          : " +
//        new R2<List<DoubleSolution>>(normalizedReferenceFront).runAlgorithm(normalizedPopulation) + "\n";
//    outputString += "R2              : " +
//        new R2<List<? extends Solution<?>>>(referenceFront).runAlgorithm(population) + "\n";
		outputString += "Error ratio     : "
				+ new ErrorRatio<List<? extends Solution<?>>>(referenceFront).evaluate(population) + "\n";

		JMetalLogger.logger.info(outputString);
	}
}
