package com.chart.util;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

public class DataSetHelper {

	private static final String EXPERIMENT_DIR = "result/Experiment";

	public static XYDataset createAlgorithmsCompareDataset(String problem, List<String> algorithms, String indicator,
			int runId) {

		XYSeriesCollection xySeriesCollection = new XYSeriesCollection();

		for (String algorithm : algorithms) {
			String qualityIndicatorFile = EXPERIMENT_DIR + "/data/" + algorithm + "/" + problem + "/" + indicator + ".r"
					+ runId;

			XYSeries xyseries = new XYSeries(algorithm);
			try {
				List<String> lines = FileUtils.readLines(new File(qualityIndicatorFile), "UTF-8");
				for (int i = 0; i < lines.size(); i++) {
					xyseries.add(i + 1, Double.parseDouble(lines.get(i)));
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
			xySeriesCollection.addSeries(xyseries);
		}

		return xySeriesCollection;
	}

	public static XYDataset createProblemsCompareDataset(List<String> problems, String algorithm, String indicator,
			int runId) {

		XYSeriesCollection xySeriesCollection = new XYSeriesCollection();

		for (String problem : problems) {
			String qualityIndicatorFile = EXPERIMENT_DIR + "/data/" + algorithm + "/" + problem + "/" + indicator + ".r"
					+ runId;

			XYSeries xyseries = new XYSeries(problem);
			try {
				List<String> lines = FileUtils.readLines(new File(qualityIndicatorFile), "UTF-8");
				for (int i = 0; i < lines.size(); i++) {
					xyseries.add(i + 1, Double.parseDouble(lines.get(i)));
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
			xySeriesCollection.addSeries(xyseries);
		}

		return xySeriesCollection;
	}
}
