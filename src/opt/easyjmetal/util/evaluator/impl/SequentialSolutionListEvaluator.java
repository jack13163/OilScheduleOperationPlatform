package opt.easyjmetal.util.evaluator.impl;

import opt.easyjmetal.util.evaluator.SolutionListEvaluator;
import opt.easyjmetal.core.Problem;
import opt.easyjmetal.core.Solution;
import opt.easyjmetal.util.JMException;
import opt.jmetal.util.JMetalException;

import java.util.List;

public class SequentialSolutionListEvaluator implements SolutionListEvaluator {

    /**
     * 评价种群
     */
    @Override
    public List<Solution> evaluate(List<Solution> solutionList, Problem problem) throws JMetalException, JMException {
        for (int i = 0; i < solutionList.size(); i++) {
            problem.evaluate(solutionList.get(i));
        }
        return solutionList;
    }

    @Override
    public void shutdown() {
        // This method is an intentionally-blank override.
    }
}
