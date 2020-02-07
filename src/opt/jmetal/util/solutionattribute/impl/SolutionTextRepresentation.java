package opt.jmetal.util.solutionattribute.impl;

import opt.jmetal.solution.Solution;

@SuppressWarnings("serial")
public class SolutionTextRepresentation extends GenericSolutionAttribute<Solution<?>, String> {

    private static SolutionTextRepresentation singleInstance = null;

    private SolutionTextRepresentation() {
    }

    public static SolutionTextRepresentation getAttribute() {
        if (singleInstance == null)
            singleInstance = new SolutionTextRepresentation();
        return singleInstance;
    }
}
