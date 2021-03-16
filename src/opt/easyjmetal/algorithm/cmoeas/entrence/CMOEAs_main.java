package opt.easyjmetal.algorithm.cmoeas.entrence;

import opt.easyjmetal.algorithm.AlgorithmFactory;
import opt.easyjmetal.core.Algorithm;
import opt.easyjmetal.core.Operator;
import opt.easyjmetal.core.Problem;
import opt.easyjmetal.operator.crossover.CrossoverFactory;
import opt.easyjmetal.operator.mutation.MutationFactory;
import opt.easyjmetal.operator.selection.SelectionFactory;
import opt.easyjmetal.problem.ProblemFactory;

import java.io.File;
import java.io.FileWriter;
import java.util.HashMap;


public class CMOEAs_main {

    public static void main(String[] args) throws Exception {
        // 0 represents for DE, 1 represents for SBX
        int crossoverMethod = 1;
        // "NSGAII_CDP",
        // "ISDEPLUS_CDP",
        // "NSGAIII_CDP",
        // "MOEAD_CDP",
        // "MOEAD_IEpsilon",
        // "MOEAD_Epsilon",
        // "MOEAD_SR",
        // "C_MOEAD",
        // "PPS_MOEAD"
        batchRun(new String[]{
                "NSGAII_CDP",
                "CMMO",
                "C_TAEA",
                "ISDEPLUS_CDP",
                "NSGAIII_CDP",
                "MOEAD_CDP",
                "MOEAD_IEpsilon",
                "MOEAD_Epsilon",
                "MOEAD_SR",
                "C_MOEAD",
                "PPS_MOEAD"
        }, crossoverMethod);
    }

    private static void batchRun(String[] methods, int crossMethod) throws Exception {
        String[] algorithmSet = methods;
        int algorithmNo = algorithmSet.length;

        // �������ʱ��
        String basePath = "result/easyjmetal/twopipeline/";
        File dir = new File(basePath);
        if (!dir.exists()) {
            dir.mkdirs();
        }

        // ÿ���½��ļ�true = append file
        FileWriter fileWritter = new FileWriter(basePath + "runtimes.txt", false);
        StringBuilder stringBuilder = new StringBuilder();

        for (int i = 0; i < algorithmNo; i++) {
            System.out.println("The tested algorithm: " + algorithmSet[i]);
            System.out.println("The process: " + String.format("%.2f", (100.0 * i / algorithmNo)) + "%");
            stringBuilder.append(singleRun(algorithmSet[i], crossMethod, basePath));
        }

        fileWritter.write(stringBuilder.toString());
        fileWritter.flush();
        fileWritter.close();
    }

    private static String singleRun(String algorithmName, int crossMethod, String basePath) throws Exception {
        StringBuilder stringBuilder = new StringBuilder();

        Operator crossover;
        Operator mutation;
        Operator selection;
        HashMap parameters;

        int popSize = 100;
        int maxFES = 50000;
        int updateNumber = 2;
        int neighborSize = (int) (0.1 * popSize);
        double deDelta = 0.9;
        double DeCrossRate = 1.0;
        double DeFactor = 0.5;
        double tao = 0.1;
        double alpha = 0.9;
        float infeasibleRatio = 0.1f;
        String AlgorithmName = algorithmName;
        double threshold = 1e-3;
        // Ȩ���ļ�·��
        String weightPath = "resources/MOEAD_Weights/";
        // ��������
        int runtime = 10;
        // �Ƿ���ʾ��ϸ����
        Boolean isDisplay = false;
        // 0: population; 1: external archive
        int plotFlag = 0;
        // MOEAD_SR parameters
        double srFactor = 0.05;

        // �㷨��Ҫ��������������һ���Ǳ��뷽ʽ����һ����xml�����ļ����ڵ�·��
        Object[] params = {"Real", "data/configs/config1.xml"};
        String[] problemStrings = {"EDFPS", "EDFTSS"};

        for (int i = 0; i < problemStrings.length; i++) {
            // ����
            Problem problem = ProblemFactory.getProblem(problemStrings[i], params);

            // �㷨
            Object[] algorithmParams = {problem};
            Algorithm algorithm = AlgorithmFactory.getAlgorithm(AlgorithmName, algorithmParams);

            // pareto�ļ�·��
            String paretoPath = basePath + problem.getName() + ".pf";

            // Algorithm parameters
            algorithm.setInputParameter("AlgorithmName", AlgorithmName);
            algorithm.setInputParameter("populationSize", popSize);
            algorithm.setInputParameter("maxEvaluations", maxFES);
            // ʵ�����ݴ�ŵ�·��
            algorithm.setInputParameter("dataDirectory", basePath + problem.getName());
            // Ȩ���ļ���ŵ�·��
            algorithm.setInputParameter("weightDirectory", weightPath);
            algorithm.setInputParameter("T", neighborSize);
            algorithm.setInputParameter("delta", deDelta);
            algorithm.setInputParameter("nr", updateNumber);
            algorithm.setInputParameter("isDisplay", isDisplay);
            algorithm.setInputParameter("plotFlag", plotFlag);
            algorithm.setInputParameter("paretoPath", paretoPath);
            algorithm.setInputParameter("srFactor", srFactor);
            algorithm.setInputParameter("tao", tao);
            algorithm.setInputParameter("alpha", alpha);
            algorithm.setInputParameter("threshold_change", threshold);
            algorithm.setInputParameter("infeasibleRatio", infeasibleRatio);

            // ��������
            if (crossMethod == 0) {
                parameters = new HashMap();
                parameters.put("CR", DeCrossRate);
                parameters.put("F", DeFactor);
                crossover = CrossoverFactory.getCrossoverOperator("DifferentialEvolutionCrossover", parameters);
                algorithm.addOperator("crossover", crossover);
            } else if (crossMethod == 1) {
                parameters = new HashMap();
                parameters.put("probability", 1.0);
                parameters.put("distributionIndex", 20.0);
                crossover = CrossoverFactory.getCrossoverOperator("SBXCrossover", parameters);
                algorithm.addOperator("crossover", crossover);
            }

            // ��������
            parameters = new HashMap();
            parameters.put("probability", 1.0 / problem.getNumberOfVariables());
            parameters.put("distributionIndex", 20.0);
            mutation = MutationFactory.getMutationOperator("PolynomialMutation", parameters);
            algorithm.addOperator("mutation", mutation);

            // ѡ������
            parameters = null;
            selection = SelectionFactory.getSelectionOperator("BinaryTournament2", parameters);
            algorithm.addOperator("selection", selection);

            // �����������ɴ�
            for (int j = 0; j < runtime; j++) {
                System.out.println("==================================================================");
                algorithm.setInputParameter("runningTime", j);
                // �����㷨������¼����ʱ��
                System.out.println("The " + j + " run of " + algorithmName);
                long initTime = System.currentTimeMillis();
                algorithm.execute();
                long estimatedTime = System.currentTimeMillis() - initTime;
                System.out.println("Total execution time: " + estimatedTime + "ms");
                System.out.println("Problem:  " + problemStrings[i] + "  running time:  " + j);
                System.out.println("==================================================================");
                stringBuilder.append(algorithmName + "," + problemStrings[i] + "," + estimatedTime + "\n");
            }
        }
        return stringBuilder.toString();
    }
}
