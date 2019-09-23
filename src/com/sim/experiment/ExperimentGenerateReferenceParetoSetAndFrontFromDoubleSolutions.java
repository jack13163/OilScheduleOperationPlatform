package com.sim.experiment;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;

import org.uma.jmetal.solution.DoubleSolution;
import org.uma.jmetal.util.archive.impl.NonDominatedSolutionListArchive;
import org.uma.jmetal.util.experiment.Experiment;
import org.uma.jmetal.util.experiment.component.GenerateReferenceParetoSetAndFrontFromDoubleSolutions;
import org.uma.jmetal.util.experiment.util.ExperimentAlgorithm;
import org.uma.jmetal.util.experiment.util.ExperimentProblem;
import org.uma.jmetal.util.fileoutput.SolutionListOutput;
import org.uma.jmetal.util.front.Front;
import org.uma.jmetal.util.front.imp.ArrayFront;

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
			nonDominatedSolutions = getNonDominatedSolutions(problem);

			// 2.2 写入每个算法构成的参考平面到目录
			writeFilesWithTheSolutionsContributedByEachAlgorithm(outputDirectoryName, problem, nonDominatedSolutions);
		}
	}

	protected void writeReferenceFrontFile(String outputDirectoryName, List<DoubleSolution> nonDominatedSolutions)
			throws IOException {
		new SolutionListOutput(nonDominatedSolutions).printObjectivesToFile(outputDirectoryName + "/oilschedule.pf");
	}

	protected void writeReferenceSetFile(String outputDirectoryName, List<DoubleSolution> nonDominatedSolutions)
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
