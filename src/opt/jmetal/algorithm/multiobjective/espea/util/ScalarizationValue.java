package opt.jmetal.algorithm.multiobjective.espea.util;

import opt.jmetal.solution.Solution;
import opt.jmetal.util.solutionattribute.impl.GenericSolutionAttribute;

/**
 * Scalarization attribute. A scalarization value is an aggregation of the
 * objective values.
 *
 * @param <S> The solution type
 * @author Marlon Braun <marlon.braun@partner.kit.edu>
 */
@SuppressWarnings("serial")
public class ScalarizationValue<S extends Solution<?>> extends GenericSolutionAttribute<S, Double> {

}
