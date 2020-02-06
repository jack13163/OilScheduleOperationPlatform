package org.uma.jmetal.util.evaluator.impl;

import java.util.List;

import org.uma.jmetal.problem.Problem;
import org.uma.jmetal.util.JMetalException;
import org.uma.jmetal.util.evaluator.SolutionListEvaluator;

/**
 * @author Antonio J. Nebro
 */
@SuppressWarnings("serial")
public class SequentialSolutionListEvaluator<S> implements SolutionListEvaluator<S> {

    /**
     * 评价种群
     */
    @Override
    public List<S> evaluate(List<S> solutionList, Problem<S> problem) throws JMetalException {
        solutionList.stream().forEach(s -> problem.evaluate(s));

        return solutionList;
    }

    @Override
    public void shutdown() {
        // This method is an intentionally-blank override.
    }
}
