package opt.easyjmetal.util;

import opt.easyjmetal.core.Solution;
import opt.easyjmetal.core.SolutionSet;
import opt.easyjmetal.qualityindicator.Epsilon;
import opt.easyjmetal.qualityindicator.Hypervolume;
import opt.easyjmetal.qualityindicator.InvertedGenerationalDistance;
import opt.easyjmetal.qualityindicator.Spread;
import opt.easyjmetal.util.sqlite.SqlUtils;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ParetoFrontUtil {

    /**
     * 生成问题和算法的Pareto前沿面
     *
     * @param algorithmNameList_  算法列表
     * @param problemList_        问题列表
     * @param independentRuns_    独立运行次数
     * @param basePath_           用于保存结果的根路径
     */
    public static void generateParetoFront(String[] algorithmNameList_,
                                           String[] problemList_,
                                           int independentRuns_,
                                           String basePath_) throws JMException {
        try {
            // 判断路径是否存在，若不存在，则创建
            File targetDir = new File(basePath_);
            if (!targetDir.exists()) {
                FileUtils.forceMkdir(targetDir);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        for (String problemName : problemList_) {
            List<Solution> solutionList = new ArrayList<>();

            // 读取某一问题的所有结果
            for (String algorithmName : algorithmNameList_) {
                // 输出每种算法的非支配解集
                List<Solution> solutionList2 = new ArrayList<>();

                for (int numRun = 0; numRun < independentRuns_; numRun++) {
                    String dbName = basePath_ + problemName;
                    String tableName = algorithmName + "_" + (numRun + 1);
                    SolutionSet tmp = SqlUtils.SelectData(dbName, tableName);
                    for (int i = 0; i < tmp.size(); i++) {
                        solutionList.add(tmp.get(i));
                        solutionList2.add(tmp.get(i));
                    }
                }

                // 输出该问题的各个算法的非支配解集
                outputNondomincantSolutionSet(solutionList2, basePath_, algorithmName + "_" + problemName + ".pf");
            }

            // 输出当前问题的非支配解集
            outputNondomincantSolutionSet(solutionList, basePath_, problemName + ".pf");
        }
    }

    /**
     * 生成所有的配置下的pareto前沿
     *
     * @param configList          配置列表
     * @param algorithmNameList_  算法列表
     * @param problemList_        问题列表
     * @param independentRuns_    独立运行次数
     * @param paretoFrontDirPath_ 生成的pareto前沿目录的根路径
     */
    public static void generateParetoFrontForAllConfigs(String[] configList,
                                                        String[] algorithmNameList_,
                                                        String[] problemList_,
                                                        int independentRuns_,
                                                        String paretoFrontDirPath_) throws JMException, IOException {
        // 判断路径是否存在，若不存在，则创建
        File targetDir = new File(paretoFrontDirPath_);
        if (targetDir.exists()) {
            FileUtils.forceMkdir(targetDir);
        }

        List<Solution> allSolutions = new ArrayList<>();
        for (String config : configList) {
            List<Solution> solutionList = new ArrayList<>();
            for (String problemName : problemList_) {
                for (String algorithmName : algorithmNameList_) {
                    // 输出每种算法的非支配解集
                    List<Solution> solutionList2 = new ArrayList<>();

                    for (int numRun = 0; numRun < independentRuns_; numRun++) {
                        String tableName = problemName + "_" + (numRun + 1);
                        // 查询数据
                        String dbPath = paretoFrontDirPath_ + "/" + config + "/" + algorithmName;
                        SolutionSet tmp = SqlUtils.SelectData(dbPath, tableName);
                        for (int i = 0; i < tmp.size(); i++) {
                            solutionList.add(tmp.get(i));
                            solutionList2.add(tmp.get(i));
                        }
                    }

                    // 输出非支配解集
                    outputNondomincantSolutionSet(solutionList2, paretoFrontDirPath_,
                            config + "_" + algorithmName + "_" + problemName + ".pf");
                }
            }

            // 输出非支配解集
            allSolutions.addAll(solutionList);
            outputNondomincantSolutionSet(solutionList, paretoFrontDirPath_, config + ".pf");
        }
        outputNondomincantSolutionSet(allSolutions, paretoFrontDirPath_, "oil.pf");
    }

    /**
     * Generate the Pareto Front for crude oil scheduling problem
     *
     * @param algorithmNameList_  算法列表
     * @param problemList_        问题列表
     * @param independentRuns_    独立运行次数
     * @param paretoFrontDirPath_ pareto前沿存储目录
     */
    public static String generateOilScheduleParetoFront(String[] algorithmNameList_,
                                                        String[] problemList_,
                                                        int independentRuns_,
                                                        String paretoFrontDirPath_) throws JMException, IOException {
        // 判断路径是否存在，若不存在，则创建
        File targetDir = new File(paretoFrontDirPath_);
        if (targetDir.exists()) {
            FileUtils.forceMkdir(targetDir);
        }

        List<Solution> solutionList = new ArrayList<>();

        // 从sqlite数据库中读取所有结果
        for (String problemName : problemList_) {
            for (String algorithmName : algorithmNameList_) {
                for (int numRun = 0; numRun < independentRuns_; numRun++) {
                    String tableName = problemName + "_" + (numRun + 1);
                    SolutionSet solutionSet = SqlUtils.SelectData(algorithmName, tableName);

                    for (int i = 0; i < solutionSet.size(); i++) {
                        solutionList.add(solutionSet.get(i));
                    }
                }
            }
        }

        // 输出非支配解集
        return outputNondomincantSolutionSet(solutionList, paretoFrontDirPath_, "oil.pf");
    }

    public interface ToDo {
        void dosomething(Solution solution, String rule);
    }

    /**
     * 从数据库中查找出指定的个体，即目标值符合的个体
     *
     * @param algorithmNameList_ 算法列表
     * @param problemList_       问题列表
     * @param independentRuns_   独立运行次数
     * @param toselect           要选择的个体
     * @param todo               要对个体执行的操作
     * @param basePath_          数据库所在的文件夹
     * @return 符合条件的解的集合，多个符合条件的只返回一个
     * @throws JMException
     */
    public static SolutionSet getSolutionFromDB(String[] algorithmNameList_,
                                                String[] problemList_,
                                                int independentRuns_,
                                                double[][] toselect,
                                                ToDo todo,
                                                String basePath_) throws JMException {
        SolutionSet solutionSet = new SolutionSet(toselect.length);

        // 判断当前个体是否为待查找的个体
        for (int j = 0; j < toselect.length; j++) {
            boolean flag = false;

            for (String problemName : problemList_) {
                if (flag) {
                    break;
                }
                for (String algorithmName : algorithmNameList_) {
                    if (flag) {
                        break;
                    }
                    for (int numRun = 0; numRun < independentRuns_; numRun++) {
                        // 没有找到就继续找，否则，退出
                        if (flag) {
                            break;
                        }
                        String dbName = basePath_ + problemName;
                        String tableName = algorithmName + "_" + (numRun + 1);
                        SolutionSet tmp = SqlUtils.SelectData(dbName, tableName);
                        for (int i = 0; i < tmp.size() && !flag; i++) {
                            flag = true;
                            Solution solution = tmp.get(i);
                            for (int k = 0; k < toselect[j].length && flag; k++) {
                                if (solution.getObjective(k) != toselect[j][k]) {
                                    flag = false;
                                }
                            }

                            // 这里需要保证当出现多个解的目标值相同时，就只找到第一个
                            if (flag) {
                                System.out.println(String.format("find solution in db:%s table:%s no:%d", algorithmName, tableName, i + 1));
                                solutionSet.add(solution);
                                if (todo != null) {
                                    todo.dosomething(solution, problemName);
                                }
                            }
                        }
                    }
                }
            }
        }
        return solutionSet;
    }

    /**
     * 输出非支配解集
     *
     * @param solutionList 非支配解集
     * @param dirPath      保存目录
     * @param filename     保存的文件名
     */
    public static String outputNondomincantSolutionSet(List<Solution> solutionList, String dirPath, String filename) {

        // 进行非支配排序，获取非支配解集
        SolutionSet nondominatedSolutionSet = getNondominantSolutionSet(solutionList);
        // 输出非支配解集
        File dir = new File(dirPath);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        String filepath = dirPath + "/" + filename;
        nondominatedSolutionSet.printObjectivesToFile(filepath);
        return filepath;
    }

    /**
     * 获取非支配解集
     *
     * @param solutionList 算法运行结果集
     * @return
     */
    public static SolutionSet getNondominantSolutionSet(List<Solution> solutionList) {

        SolutionSet solutionSet = new SolutionSet(solutionList.size());
        for (int i = 0; i < solutionList.size(); i++) {
            solutionSet.add(solutionList.get(i));
        }
        // 进行非支配排序，并返回第一层结果
        Ranking ranking = new Ranking(solutionSet);
        SolutionSet nondominatedSolutionSet = ranking.getSubfront(0);
        return nondominatedSolutionSet;
    }

    /**
     * 计算性能指标
     *
     * @param algorithmName       算法
     * @param problemName         问题
     * @param indicatorName       指标名字
     * @param runId               运行编号，用于从数据库中查找算法运行结果
     * @param basePath            数据结果保存根路径
     * @param trueParetoFrontPath 真实的pareto前沿路径
     */
    public static double calculateQualityIndicator(String algorithmName,
                                                   String problemName,
                                                   String indicatorName,
                                                   int runId,
                                                   String basePath,
                                                   String trueParetoFrontPath) throws JMException {

        // 加载真实的Pareto前沿
        double[][] trueFront = new Hypervolume().utils_.readFront(trueParetoFrontPath);
        // 加载算法运行得到的非支配解集
        String dbName = basePath + problemName;
        String tableName = algorithmName + "_" + runId;
        double[][] solutionFront = SqlUtils.SelectData(dbName, tableName).writeObjectivesToMatrix();
        // 计算并返回指标值
        return calculateIndicatorValue(indicatorName, trueFront, solutionFront);
    }

    /**
     * 计算性能指标
     *
     * @param indicatorName         指标的名称
     * @param resultParetoFrontPath 算法运行结果
     * @param trueParetoFrontPath   真实的pareto前沿路径
     */
    public static double calculateQualityIndicator(String indicatorName,
                                                   String resultParetoFrontPath,
                                                   String trueParetoFrontPath) {

        // 加载真实的Pareto前沿
        double[][] trueFront = new Hypervolume().utils_.readFront(trueParetoFrontPath);
        // 加载算法运行得到的非支配解集
        double[][] resultParetoFront = new Hypervolume().utils_.readFront(resultParetoFrontPath);
        // 计算并返回指标值
        return calculateIndicatorValue(indicatorName, trueFront, resultParetoFront);
    }

    /**
     * 计算指标值
     *
     * @param indicatorName 指标名字
     * @param trueFront     真实的pareto前沿
     * @param solutionFront 算法运行结果
     * @return
     */
    private static double calculateIndicatorValue(String indicatorName, double[][] trueFront, double[][] solutionFront) {
        double value = 0;
        if (indicatorName.equals("HV")) {
            Hypervolume indicators = new Hypervolume();
            value = indicators.hypervolume(solutionFront, trueFront, trueFront[0].length);
        }
        if (indicatorName.equals("SPREAD")) {
            Spread indicators = new Spread();
            value = indicators.spread(solutionFront, trueFront, trueFront[0].length);
        }
        if (indicatorName.equals("IGD")) {
            InvertedGenerationalDistance indicators = new InvertedGenerationalDistance();
            value = indicators.invertedGenerationalDistance(solutionFront, trueFront, trueFront[0].length);
        }
        if (indicatorName.equals("EPSILON")) {
            Epsilon indicators = new Epsilon();
            value = indicators.epsilon(solutionFront, trueFront, trueFront[0].length);
        }
        return value;
    }

    /**
     * 生成性能指标
     *
     * @param algorithmNameList_ 算法列表
     * @param problemList_       问题列表
     * @param indicatorList_     指标列表
     * @param independentRuns_   独立运行次数
     * @param dirPath_           真实的pareto前沿存放的路径，指标文件将会生成在当前文件夹下的indicator目录下
     */
    public static void generateQualityIndicators(String[] algorithmNameList_,
                                                 String[] problemList_,
                                                 String[] indicatorList_,
                                                 int independentRuns_,
                                                 String dirPath_) throws JMException {
        if (indicatorList_.length > 0) {
            for (String algorithmName : algorithmNameList_) {
                for (String problemName : problemList_) {
                    for (String indicator : indicatorList_) {
                        String trueParetoFilePath = dirPath_ + problemName + ".pf";
                        writeIndicatorToFile(independentRuns_, dirPath_, trueParetoFilePath, algorithmName, problemName, indicator);
                    }
                }
            }
        }
    }

    /**
     * 输出指标值到文件
     *
     * @param independentRuns_
     * @param dirPath_
     * @param trueParetoFilePath
     * @param algorithmName
     * @param problemName
     * @param indicator
     * @throws JMException
     */
    private static void writeIndicatorToFile(int independentRuns_,
                                             String dirPath_,
                                             String trueParetoFilePath,
                                             String algorithmName,
                                             String problemName,
                                             String indicator) {
        try {
            // 输出到文件
            String indicatorDir = dirPath_ + "indicator/" + problemName + "/";
            File file = new File(indicatorDir);
            if (!file.exists()) {
                file.mkdirs();// 不存在，则创建目录
            }
            String indicatorFileName = indicatorDir + algorithmName + "." + indicator;
            FileWriter writer = new FileWriter(indicatorFileName);

            // 计算每次实验的指标值
            for (int numRun = 1; numRun <= independentRuns_; numRun++) {
                double value = calculateQualityIndicator(algorithmName, problemName, indicator, numRun, dirPath_, trueParetoFilePath);
                writer.write(String.format("%.5f\n", value));
            }

            writer.flush();
            writer.close();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /**
     * 生成所有配置下的指标值
     *
     * @param configList         配置列表
     * @param algorithmNameList_ 算法列表
     * @param problemList_       问题列表
     * @param indicatorList_     指标列表
     * @param independentRuns_   独立运行次数
     * @param dirPath_           真实的pareto前沿存放的路径，指标文件将会生成在当前文件夹下的indicator目录下
     */
    public static void generateQualityIndicatorsForAllConfigs(String[] configList,
                                                              String[] algorithmNameList_,
                                                              String[] problemList_,
                                                              String[] indicatorList_,
                                                              int independentRuns_,
                                                              String dirPath_) throws JMException {
        if (indicatorList_.length > 0) {
            for (String config : configList) {
                for (String algorithmName : algorithmNameList_) {
                    for (String problemName : problemList_) {
                        for (String indicator : indicatorList_) {
                            String trueParetoFilePath = dirPath_ + config + ".pf";
                            writeIndicatorToFile(independentRuns_, dirPath_, trueParetoFilePath, algorithmName, problemName, indicator);
                        }
                    }
                }
            }
        }
    }
}
