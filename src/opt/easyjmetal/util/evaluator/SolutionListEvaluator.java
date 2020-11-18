package opt.easyjmetal.util.evaluator;

import opt.easyjmetal.core.Problem;
import opt.easyjmetal.core.Solution;
import opt.easyjmetal.util.JMException;

import java.io.Serializable;
import java.util.List;

public interface SolutionListEvaluator extends Serializable {
    List<Solution> evaluate(List<Solution> solutionList, Problem problem)  throws JMException;

    void shutdown();
}
