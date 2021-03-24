package opt.easyjmetal.operator.mutation;

import opt.easyjmetal.core.Solution;
import opt.easyjmetal.encodings.solutiontype.ArrayRealSolutionType;
import opt.easyjmetal.encodings.solutiontype.RealSolutionType;
import opt.easyjmetal.util.Configuration;
import opt.easyjmetal.util.JMException;
import opt.easyjmetal.util.permutation.PseudoRandom;
import opt.easyjmetal.util.wrapper.XReal;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class UniformMutation extends Mutation {
    /**
     * Valid solution types to apply this operator
     */
    private static final List VALID_TYPES = Arrays.asList(RealSolutionType.class,
            ArrayRealSolutionType.class);

    private double perturbation_;
    private double mutationProbability_;

    public UniformMutation(HashMap<String, Object> parameters) {
        super(parameters);
        if (parameters.get("probability") != null)
            mutationProbability_ = Double.parseDouble(parameters.get("probability").toString());
        if (parameters.get("perturbation") != null)
            perturbation_ = Double.parseDouble(parameters.get("perturbation").toString());
    }

    /**
     * Ö´ÐÐ±äÒì²Ù×÷
     *
     * @param probability Mutation probability
     * @param solution    The solution to mutate
     * @throws JMException
     */
    public void doMutation(double probability, Solution solution) throws JMException {
        XReal x = new XReal(solution);

        for (int var = 0; var < solution.getDecisionVariables().length; var++) {
            if (PseudoRandom.randDouble() < probability) {
                double rand = PseudoRandom.randDouble();
                double tmp = (rand - 0.5) * perturbation_;

                tmp += x.getValue(var);

                if (tmp < x.getLowerBound(var))
                    tmp = x.getLowerBound(var);
                else if (tmp > x.getUpperBound(var))
                    tmp = x.getUpperBound(var);

                x.setValue(var, tmp);
            }
        }
    }

    /**
     * Executes the operation
     *
     * @param object An object containing the solution to mutate
     * @throws JMException
     */
    public Object execute(Object object) throws JMException {
        Solution solution = (Solution) object;

        if (!VALID_TYPES.contains(solution.getType().getClass())) {
            Configuration.logger_.severe("UniformMutation.execute: the solution " +
                    "is not of the right type. The type should be 'Real', but " +
                    solution.getType() + " is obtained");

            Class cls = String.class;
            String name = cls.getName();
            throw new JMException("Exception in " + name + ".execute()");
        }

        doMutation(mutationProbability_, solution);

        return solution;
    }
}
