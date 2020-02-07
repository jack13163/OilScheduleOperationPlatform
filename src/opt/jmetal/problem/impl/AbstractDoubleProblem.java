package opt.jmetal.problem.impl;

import opt.jmetal.solution.DoubleSolution;
import opt.jmetal.solution.impl.DefaultDoubleSolution;
import opt.jmetal.problem.DoubleProblem;

import java.util.List;

@SuppressWarnings("serial")
public abstract class AbstractDoubleProblem extends AbstractGenericProblem<DoubleSolution>
        implements DoubleProblem {

    private List<Double> lowerLimit;
    private List<Double> upperLimit;

    /* Getters */
    @Override
    public Double getUpperBound(int index) {
        return upperLimit.get(index);
    }

    @Override
    public Double getLowerBound(int index) {
        return lowerLimit.get(index);
    }

    /* Setters */
    protected void setLowerLimit(List<Double> lowerLimit) {
        this.lowerLimit = lowerLimit;
    }

    protected void setUpperLimit(List<Double> upperLimit) {
        this.upperLimit = upperLimit;
    }

    @Override
    public DoubleSolution createSolution() {
        return new DefaultDoubleSolution(this);
    }
}
