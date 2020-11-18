package opt.easyjmetal.util.evaluator.impl;

import opt.easyjmetal.util.evaluator.SolutionListEvaluator;
import opt.easyjmetal.core.Problem;
import opt.easyjmetal.core.Solution;
import opt.easyjmetal.util.JMException;

import java.util.List;

public class MultithreadedSolutionListEvaluator implements SolutionListEvaluator {

    private int numberOfThreads;

    public MultithreadedSolutionListEvaluator(int numberOfThreads, Problem problem) {
        if (numberOfThreads == 0) {
            this.numberOfThreads = Runtime.getRuntime().availableProcessors();
        } else {
            this.numberOfThreads = numberOfThreads;
            System.setProperty("java.util.concurrent.ForkJoinPool.common.parallelism",
                    "" + this.numberOfThreads);
        }
        System.out.println("Number of cores: " + numberOfThreads);
    }

    @Override
    public List<Solution> evaluate(List<Solution> solutionList, Problem problem) throws JMException {
        solutionList.parallelStream().forEach(s -> {
            try {
                problem.evaluate(s);
            } catch (JMException e) {
                e.printStackTrace();
            }
        });
        return solutionList;
    }

    public int getNumberOfThreads() {
        return numberOfThreads;
    }

    @Override
    public void shutdown() {
        //This method is an intentionally-blank override.
    }
}
