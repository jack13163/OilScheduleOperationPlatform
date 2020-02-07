package opt.jmetal.util.extremevalues.impl;

import opt.jmetal.solution.Solution;
import opt.jmetal.util.extremevalues.ExtremeValuesFinder;
import opt.jmetal.util.front.imp.ArrayFront;

import java.util.List;

/**
 * Class for finding the extreme values of a list of objects
 *
 * @author Antonio J. Nebro
 */
public class SolutionListExtremeValues implements ExtremeValuesFinder<List<Solution<?>>, List<Double>> {

    @Override
    public List<Double> findLowestValues(List<Solution<?>> solutionList) {
        return new FrontExtremeValues().findLowestValues(new ArrayFront(solutionList));
    }

    @Override
    public List<Double> findHighestValues(List<Solution<?>> solutionList) {
        return new FrontExtremeValues().findHighestValues(new ArrayFront(solutionList));
    }
}
