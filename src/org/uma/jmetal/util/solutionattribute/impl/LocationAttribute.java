package org.uma.jmetal.util.solutionattribute.impl;

import org.uma.jmetal.solution.Solution;

import java.util.List;

/**
 * Assign to each solution in a solution list an attribute containing the position of
 * the solutions in the list.
 *
 * @param <S>
 * @author Antonio J. Nebro
 */

@SuppressWarnings("serial")
public class LocationAttribute<S extends Solution<?>>
        extends GenericSolutionAttribute<S, Integer> {

    public LocationAttribute(List<S> solutionList) {
        int location = 0;
        for (S solution : solutionList)
            solution.setAttribute(getAttributeIdentifier(), location++);
    }
}
