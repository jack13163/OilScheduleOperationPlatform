package opt.jmetal.problem.impl;

import opt.jmetal.solution.PermutationSolution;
import opt.jmetal.solution.impl.DefaultIntegerPermutationSolution;
import opt.jmetal.problem.PermutationProblem;

@SuppressWarnings("serial")
public abstract class AbstractIntegerPermutationProblem
        extends AbstractGenericProblem<PermutationSolution<Integer>> implements
        PermutationProblem<PermutationSolution<Integer>> {

    /* Getters */

    /* Setters */

    @Override
    public PermutationSolution<Integer> createSolution() {
        return new DefaultIntegerPermutationSolution(this);
    }
}
