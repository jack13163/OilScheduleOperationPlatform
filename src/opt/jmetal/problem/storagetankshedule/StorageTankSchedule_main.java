package opt.jmetal.problem.storagetankshedule;

import opt.jmetal.problem.oil.sim.experiment.ExperimentConfig;
import opt.jmetal.qualityindicator.impl.hypervolume.util.WfgHypervolumeFront;
import opt.jmetal.qualityindicator.impl.hypervolume.util.WfgHypervolumeVersion;
import opt.jmetal.solution.DoubleSolution;
import opt.jmetal.util.JMetalException;
import opt.jmetal.util.JMetalLogger;
import opt.jmetal.util.experiment.util.ExperimentProblem;
import opt.jmetal.util.front.Front;
import opt.jmetal.util.front.imp.ArrayFront;
import opt.jmetal.util.point.Point;
import opt.jmetal.util.point.impl.ArrayPoint;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.util.*;

public class StorageTankSchedule_main {
    public static void main(String[] args) throws Exception {
        int popsize = 100;
        int evaluation = 50000;
        int runs = 5;
        ExperimentProblem<DoubleSolution> problem = new ExperimentProblem<DoubleSolution>(new Problem());
        List<String> algorithms = Arrays.asList(
                "IBEA",
                "NSGAII",
                "NSGAIII",
                "SPEA2",
                "MoCell"
        );

        batchRun(problem, algorithms, popsize, evaluation, runs);
        anysis(problem, algorithms, popsize, evaluation, runs);
    }

    /**
     * 批量运行
     *
     * @param problem
     * @param algorithmNames
     * @param popsize
     * @param evaluation
     * @param runs
     */
    private static void batchRun(ExperimentProblem<DoubleSolution> problem, List<String> algorithmNames, int popsize, int evaluation, int runs) {
        try {
            List<ExperimentProblem<DoubleSolution>> problems = new LinkedList<>();
            problems.add(problem);
            ExperimentConfig.doExperimentDoubleCode(problems, algorithmNames, popsize, evaluation, runs);
        } catch (Exception ex) {
            ex.printStackTrace();
            System.exit(-1);
        }
    }

    /**
     * 分析结果
     *
     * @param problem
     * @param algorithmNames
     * @param popsize
     * @param evaluation
     * @param runs
     */
    private static void anysis(ExperimentProblem<DoubleSolution> problem, List<String> algorithmNames, int popsize, int evaluation, int runs) {
        List<String> problemNames = Arrays.asList("StorageTankSchedule");

        // 生成结果的路径
        String experimentBaseDirectory = "result/Experiment/";
        String outputDirectoryName = "PF/";
        String outputParetoFrontFileName = "FUN";
        String outputParetoSetFileName = "VAR";

        try {
            generatePF(outputDirectoryName, experimentBaseDirectory, outputParetoFrontFileName, outputParetoSetFileName, problemNames, algorithmNames, runs);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /**
     * 生成参考PF
     *
     * @param outputDirectoryName
     * @param experimentBaseDirectory
     * @param outputParetoFrontFileName
     * @param outputParetoSetFileName
     * @param problems
     * @param algorithms
     * @param runs
     */
    private static void generatePF(String outputDirectoryName,
                                   String experimentBaseDirectory,
                                   String outputParetoFrontFileName,
                                   String outputParetoSetFileName,
                                   List<String> problems,
                                   List<String> algorithms,
                                   int runs) throws Exception {

        // 参考点
        Point refPoint = new ArrayPoint(new double[]{1.0, 1.0, 1.0, 1.0});

        // 创建PF目录
        String pfDir = experimentBaseDirectory + outputDirectoryName;
        File outputDirectory = new File(pfDir);
        if (!outputDirectory.exists()) {
            new File(pfDir).mkdir();
        }

        // 获取各个算法的非支配解
        List<Point> allPoint = new ArrayList<>();
        Map<String, List<List<Point>>> dataMap = new HashMap<>();
        for (String problem : problems) {
            for (String algorithm : algorithms) {
                String key = problem + " on " + algorithm;
                List<List<Point>> mult_run_points = new ArrayList<>();
                for (int r = 0; r < runs; r++) {
                    // 问题目录结构为：result/data/algorithm/problem
                    String frontFileName = experimentBaseDirectory + "/data/" + algorithm + "/" + problem + "/" + outputParetoFrontFileName + r + ".tsv";
                    Front frontWithObjectiveValues = new ArrayFront(frontFileName);
                    List<Point> pop = convertPoints(frontWithObjectiveValues);

                    mult_run_points.add(pop);
                    allPoint.addAll(pop);
                }
                dataMap.put(key, mult_run_points);
            }
        }
        printPoints(new FirstRank().getFirstfront(allPoint), pfDir + "StorageTankSchedule.pf");

        // 以规范化后的整个运行结果所产生的的解为参考平面
        double[] min = minPoints(allPoint);
        double[] max = maxPoints(allPoint);

        for (String key : dataMap.keySet()) {
            for (int i = 0; i < dataMap.get(key).size(); i++) {
                List<Point> points = dataMap.get(key).get(i);
                // 标准化
                List<Point> normalizedPoints = getNormalizedPoints(points, max, min);
                double indicatorValue = computeHypervolume(normalizedPoints, refPoint);
                JMetalLogger.logger.info(key + " run_" + i + " HV: " + indicatorValue);
            }
        }
    }

    /**
     * 标准化点
     *
     * @param front
     * @param maximumValues
     * @param minimumValues
     * @return
     */
    private static List<Point> getNormalizedPoints(List<Point> front, double[] maximumValues, double[] minimumValues) {
        if (front.size() == 0) {
            throw new JMetalException("The front is empty");
        } else if (front.get(0).getDimension() != maximumValues.length) {
            throw new JMetalException("The length of the point dimensions (" + front.get(0).getDimension() + ") "
                    + "is different from the length of the maximum array (" + maximumValues.length + ")");
        }

        int numberOfPointDimensions = front.get(0).getDimension();
        List<Point> normalizedPoints = new ArrayList<>();

        for (int i = 0; i < front.size(); i++) {
            double[] values = new double[numberOfPointDimensions];
            for (int j = 0; j < numberOfPointDimensions; j++) {
                if ((maximumValues[j] - minimumValues[j]) < 0) {
                    throw new JMetalException("Maximum values of index are small than and minimum values at " + j + ".");
                }
                values[j] = (front.get(i).getValue(j) - minimumValues[j]) / (maximumValues[j] - minimumValues[j]);
            }
            Point point = new ArrayPoint(values);
            normalizedPoints.add(point);
        }
        return normalizedPoints;
    }

    /**
     * 输出参考平面的所有非支配解
     *
     * @param ndPoints
     * @param fileName
     */
    private static void printPoints(List<Point> ndPoints, String fileName) throws Exception {
        FileOutputStream outputStream = new FileOutputStream(fileName);
        OutputStreamWriter outputStreamWriter = new OutputStreamWriter(outputStream);
        BufferedWriter bufferedWriter = new BufferedWriter(outputStreamWriter);

        if (ndPoints.size() > 0) {
            int numberOfObjectives = ndPoints.get(0).getDimension();
            for (int i = 0; i < ndPoints.size(); i++) {
                for (int j = 0; j < numberOfObjectives; j++) {
                    if (j < numberOfObjectives - 1) {
                        bufferedWriter.write(ndPoints.get(i).getValue(j) + ",");
                    } else {
                        bufferedWriter.write(ndPoints.get(i).getValue(j) + "");
                    }
                }
                bufferedWriter.newLine();
            }
        }

        bufferedWriter.close();
    }

    private static List<Point> convertPoints(Front front) {
        List<Point> pop = new ArrayList<>();
        for (int i = 0; i < front.getNumberOfPoints(); i++) {
            pop.add(front.getPoint(i));
        }
        return pop;
    }

    /**
     * 获取最大点
     *
     * @param points
     * @return
     */
    private static double[] maxPoints(List<Point> points) {
        double[] max = new double[points.get(0).getDimension()];
        for (int i = 0; i < max.length; i++) {
            max[i] = Double.MIN_VALUE;
        }
        for (Point point : points) {
            for (int i = 0; i < point.getDimension(); i++) {
                if (max[i] < point.getValue(i)) {
                    max[i] = point.getValue(i);
                }
            }
        }

        return max;
    }

    /**
     * 获取最小点
     *
     * @param points
     * @return
     */
    private static double[] minPoints(List<Point> points) {
        double[] min = new double[points.get(0).getDimension()];
        for (int i = 0; i < min.length; i++) {
            min[i] = Double.MAX_VALUE;
        }
        for (Point point : points) {
            for (int i = 0; i < point.getDimension(); i++) {
                if (min[i] > point.getValue(i)) {
                    min[i] = point.getValue(i);
                }
            }
        }

        return min;
    }

    /**
     * 计算超体积
     *
     * @param solutionList
     * @param referencePoint
     * @return
     */
    private static double computeHypervolume(List<Point> solutionList, Point referencePoint) {
        double hv = 0.0;
        if (solutionList.size() == 0) {
            hv = 0.0;
        } else {
            int numberOfObjectives = solutionList.get(0).getDimension();

            if (numberOfObjectives == 2) {
                Collections.sort(solutionList, new PointComparator(1, PointComparator.Ordering.ASCENDING));
                hv = get2DHV(solutionList, referencePoint);
            } else {
                WfgHypervolumeVersion wfgHv = new WfgHypervolumeVersion(numberOfObjectives, solutionList.size());
                WfgHypervolumeFront front = new WfgHypervolumeFront(solutionList.size(), solutionList.get(0).getDimension());
                for (int i = 0; i < solutionList.size(); i++) {
                    front.setPoint(i, solutionList.get(i));
                }
                hv = wfgHv.getHV(front);
            }
        }

        return hv;
    }


    /**
     * Computes the HV of a solution list.
     * REQUIRES: The problem is bi-objective
     * REQUIRES: The setArchive is ordered in descending order by the second objective
     *
     * @return
     */
    private static double get2DHV(List<Point> solutionSet, Point referencePoint) {
        double hv = 0.0;
        if (solutionSet.size() > 0) {
            hv = Math.abs((solutionSet.get(0).getValue(0) - referencePoint.getValue(0)) *
                    (solutionSet.get(0).getValue(1) - referencePoint.getValue(1)));

            for (int i = 1; i < solutionSet.size(); i++) {
                double tmp;
                tmp = Math.abs((solutionSet.get(i).getValue(0)
                        - referencePoint.getValue(0)) * (solutionSet.get(i).getValue(1)
                        - solutionSet.get(i - 1).getValue(1)));
                hv += tmp;
            }
        }
        return hv;
    }

}
