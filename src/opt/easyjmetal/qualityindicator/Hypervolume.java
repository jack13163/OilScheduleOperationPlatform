package opt.easyjmetal.qualityindicator;

import opt.easyjmetal.qualityindicator.util.MetricsUtil;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * This class implements the hypervolume indicator. The code is the a Java version
 * of the original metric implementation by Eckart Zitzler.
 * It can be used also as a command line program just by typing
 * $java jmetal.qualityIndicator.Hypervolume <solutionFrontFile> <trueFrontFile> <numberOfOjbectives>
 * Reference: E. Zitzler and L. Thiele
 * Multiobjective Evolutionary Algorithms: A Comparative Case Study
 * and the Strength Pareto Approach,
 * IEEE Transactions on Evolutionary Computation, vol. 3, no. 4,
 * pp. 257-271, 1999.
 */
public class Hypervolume {

    public MetricsUtil utils_;

    /**
     * Constructor
     * Creates a new instance of MultiDelta
     */
    public Hypervolume() {
        utils_ = new MetricsUtil();
    } // Hypervolume

    /*
     returns true if 'point1' dominates 'points2' with respect to the
     to the first 'noObjectives' objectives
     */
    boolean dominates(double point1[], double point2[], int noObjectives) {
        int i;
        int betterInAnyObjective;

        betterInAnyObjective = 0;
        for (i = 0; i < noObjectives && point1[i] >= point2[i]; i++) {
            if (point1[i] > point2[i]) {
                betterInAnyObjective = 1;
            }
        }

        return ((i >= noObjectives) && (betterInAnyObjective > 0));
    } //Dominates

    void swap(double[][] front, int i, int j) {
        double[] temp;

        temp = front[i];
        front[i] = front[j];
        front[j] = temp;
    } // Swap


    /* all nondominated points regarding the first 'noObjectives' dimensions
    are collected; the points referenced by 'front[0..noPoints-1]' are
    considered; 'front' is resorted, such that 'front[0..n-1]' contains
    the nondominated points; n is returned */
    int filterNondominatedSet(double[][] front, int noPoints, int noObjectives) {
        int i, j;
        int n;

        n = noPoints;
        i = 0;
        while (i < n) {
            j = i + 1;
            while (j < n) {
                if (dominates(front[i], front[j], noObjectives)) {
                    /* remove point 'j' */
                    n--;
                    swap(front, j, n);
                } else if (dominates(front[j], front[i], noObjectives)) {
	/* remove point 'i'; ensure that the point copied to index 'i'
	   is considered in the next outer loop (thus, decrement i) */
                    n--;
                    swap(front, i, n);
                    i--;
                    break;
                } else {
                    j++;
                }
            }
            i++;
        }
        return n;
    } // FilterNondominatedSet


    /* calculate next value regarding dimension 'objective'; consider
       points referenced in 'front[0..noPoints-1]' */
    double surfaceUnchangedTo(double[][] front, int noPoints, int objective) {
        int i;
        double minValue, value;

        if (noPoints < 1) {
            System.err.println("run-time error");
        }

        minValue = front[0][objective];
        for (i = 1; i < noPoints; i++) {
            value = front[i][objective];
            if (value < minValue) {
                minValue = value;
            }
        }
        return minValue;
    } // SurfaceUnchangedTo

    /* remove all points which have a value <= 'threshold' regarding the
       dimension 'objective'; the points referenced by
       'front[0..noPoints-1]' are considered; 'front' is resorted, such that
       'front[0..n-1]' contains the remaining points; 'n' is returned */
    int reduceNondominatedSet(double[][] front, int noPoints, int objective,
                              double threshold) {
        int n;
        int i;

        n = noPoints;
        for (i = 0; i < n; i++) {
            if (front[i][objective] <= threshold) {
                n--;
                swap(front, i, n);
            }
        }

        return n;
    } // ReduceNondominatedSet

    public double calculateHypervolume(double[][] front, int noPoints, int noObjectives) {
        int n;
        double volume, distance;

        volume = 0;
        distance = 0;
        n = noPoints;
        while (n > 0) {
            int noNondominatedPoints;
            double tempVolume, tempDistance;

            noNondominatedPoints = filterNondominatedSet(front, n, noObjectives - 1);
            //noNondominatedPoints = front.length;
            if (noObjectives < 3) {
                if (noNondominatedPoints < 1) {
                    System.err.println("run-time error");
                }

                tempVolume = front[0][0];
            } else {
                tempVolume = calculateHypervolume(front,
                        noNondominatedPoints,
                        noObjectives - 1);
            }

            tempDistance = surfaceUnchangedTo(front, n, noObjectives - 1);
            volume += tempVolume * (tempDistance - distance);
            distance = tempDistance;
            n = reduceNondominatedSet(front, n, noObjectives - 1, distance);
        }
        return volume;
    } // CalculateHypervolume


    /* merge two fronts */
    double[][] mergeFronts(double[][] front1, int sizeFront1,
                           double[][] front2, int sizeFront2, int noObjectives) {
        int i, j;
        int noPoints;
        double[][] frontPtr;

        /* allocate memory */
        noPoints = sizeFront1 + sizeFront2;
        frontPtr = new double[noPoints][noObjectives];
        /* copy points */
        noPoints = 0;
        for (i = 0; i < sizeFront1; i++) {
            for (j = 0; j < noObjectives; j++)
                frontPtr[noPoints][j] = front1[i][j];
            noPoints++;
        }
        for (i = 0; i < sizeFront2; i++) {
            for (j = 0; j < noObjectives; j++)
                frontPtr[noPoints][j] = front2[i][j];
            noPoints++;
        }

        return frontPtr;
    } // MergeFronts

    /**
     * 计算超体积
     *
     * @param paretoFront        The pareto front
     * @param paretoTrueFront    The true pareto front
     * @param numberOfObjectives Number of objectives of the pareto front
     */
    public double hypervolume(double[][] paretoFront,
                              double[][] paretoTrueFront,
                              int numberOfObjectives) {

        double[] maximumValues;
        double[] minimumValues;
        double[][] normalizedFront;
        double[][] invertedFront;

        // 读取properties配置文件中的最大值和最小值
        double[] customMaximumValues = new double[numberOfObjectives];
        double[] customMinimumValues = new double[numberOfObjectives];
        Properties properties = new Properties();
        try {
            InputStream inputStream = new FileInputStream(new File("data/min_max_objective.properties"));
            properties.load(inputStream);
            // 读取以下几个目标的最大值和最小值：energyCost, pipeMixingCost, tankMixingCost, numberOfChange, numberOfTankUsed
            customMinimumValues[0] = Double.parseDouble(properties.get("energyCost").toString().split(",")[0]);
            customMinimumValues[1] = Double.parseDouble(properties.get("pipeMixingCost").toString().split(",")[0]);
            customMinimumValues[2] = Double.parseDouble(properties.get("tankMixingCost").toString().split(",")[0]);
            customMinimumValues[3] = Double.parseDouble(properties.get("numberOfChange").toString().split(",")[0]);
            customMinimumValues[4] = Double.parseDouble(properties.get("numberOfTankUsed").toString().split(",")[0]);

            customMaximumValues[0] = Double.parseDouble(properties.get("energyCost").toString().split(",")[1]);
            customMaximumValues[1] = Double.parseDouble(properties.get("pipeMixingCost").toString().split(",")[1]);
            customMaximumValues[2] = Double.parseDouble(properties.get("tankMixingCost").toString().split(",")[1]);
            customMaximumValues[3] = Double.parseDouble(properties.get("numberOfChange").toString().split(",")[1]);
            customMaximumValues[4] = Double.parseDouble(properties.get("numberOfTankUsed").toString().split(",")[1]);
        } catch (IOException e) {
            e.printStackTrace();
        }

        // 计算pareto前沿上的最值
        maximumValues = utils_.getMaximumValues(paretoTrueFront, numberOfObjectives);
        minimumValues = utils_.getMinimumValues(paretoTrueFront, numberOfObjectives);

        // 1.将用户配置的最值和运行得到的最值进行合并，得到最终的最值
        for (int i = 0; i < numberOfObjectives; i++) {
            maximumValues[i] = Math.max(customMaximumValues[i], maximumValues[i]);
            minimumValues[i] = Math.min(customMinimumValues[i], minimumValues[i]);
        }

        // 2.标准化
        normalizedFront = utils_.getNormalizedFront(paretoFront, maximumValues, minimumValues);

        // 3.转换为最小化问题
        invertedFront = utils_.invertedFront(normalizedFront);

        // 4.计算hv
        return this.calculateHypervolume(invertedFront, invertedFront.length, numberOfObjectives);
    }

    /**
     * This class can be invoqued from the command line. Three params are required:
     * 1) the name of the file containing the front,
     * 2) the name of the file containig the true Pareto front
     * 3) the number of objectives
     */
    public static void main(String args[]) {
        if (args.length < 2) {
            System.err.println("Error using Hypervolume. Usage: \n java jmetal.qualityIndicator.Hypervolume " +
                    "<SolutionFrontFile> " +
                    "<TrueFrontFile> " + "<getNumberOfObjectives>");
            System.exit(1);
        }

        //Create a new instance of the metric
        Hypervolume qualityIndicator = new Hypervolume();
        //Read the front from the files
        double[][] solutionFront = qualityIndicator.utils_.readFront(args[0]);
        double[][] trueFront = qualityIndicator.utils_.readFront(args[1]);

        //Obtain delta value
        double value = qualityIndicator.hypervolume(solutionFront, trueFront, new Integer(args[2]));

        System.out.println(value);
    }
}
