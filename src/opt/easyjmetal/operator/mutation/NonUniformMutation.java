//  NonUniformMutation.java
//
//  Author:
//       Antonio J. Nebro <antonio@lcc.uma.es>
//       Juan J. Durillo <durillo@lcc.uma.es>
//
//  Copyright (c) 2011 Antonio J. Nebro, Juan J. Durillo
//
//  This program is free software: you can redistribute it and/or modify
//  it under the terms of the GNU Lesser General Public License as published by
//  the Free Software Foundation, either version 3 of the License, or
//  (at your option) any later version.
//
//  This program is distributed in the hope that it will be useful,
//  but WITHOUT ANY WARRANTY; without even the implied warranty of
//  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
//  GNU Lesser General Public License for more details.
// 
//  You should have received a copy of the GNU Lesser General Public License
//  along with this program.  If not, see <http://www.gnu.org/licenses/>.

package opt.easyjmetal.operator.mutation;

import opt.easyjmetal.core.Solution;
import opt.easyjmetal.encodings.solutiontype.ArrayRealSolutionType;
import opt.easyjmetal.encodings.solutiontype.RealSolutionType;
import opt.easyjmetal.util.Configuration;
import opt.easyjmetal.util.JMException;
import opt.easyjmetal.util.PseudoRandom;
import opt.easyjmetal.util.wrapper.XReal;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

/**
 * 非均匀变异
 */
public class NonUniformMutation extends Mutation {
    // 合法的解的类型
    private static final List VALID_TYPES = Arrays.asList(RealSolutionType.class,
            ArrayRealSolutionType.class);

    private double perturbation_;
    private int maxIterations_;
    private int currentIteration_;
    private double mutationProbability_;

    public NonUniformMutation(HashMap<String, Object> parameters) {
        super(parameters);
        if (parameters.get("probability") != null)
            mutationProbability_ = Double.parseDouble(parameters.get("probability").toString());
        if (parameters.get("perturbation") != null)
            perturbation_ = Double.parseDouble(parameters.get("perturbation").toString());
        if (parameters.get("maxIterations") != null)
            maxIterations_ = Integer.parseInt(parameters.get("maxIterations").toString());
    }

    /**
     * 执行变异操作
     *
     * @param probability
     * @param solution
     * @throws JMException
     */
    public void doMutation(double probability, Solution solution) throws JMException {
        XReal x = new XReal(solution);
        for (int var = 0; var < solution.getDecisionVariables().length; var++) {
            if (PseudoRandom.randDouble() < probability) {
                double rand = PseudoRandom.randDouble();
                double tmp;

                if (rand <= 0.5) {
                    tmp = delta(x.getUpperBound(var) - x.getValue(var),
                            perturbation_);
                    tmp += x.getValue(var);
                } else {
                    tmp = delta(x.getLowerBound(var) - x.getValue(var),
                            perturbation_);
                    tmp += x.getValue(var);
                }

                if (tmp < x.getLowerBound(var))
                    tmp = x.getLowerBound(var);
                else if (tmp > x.getUpperBound(var))
                    tmp = x.getUpperBound(var);

                x.setValue(var, tmp);
            }
        }
    }

    /**
     * 计算delta值
     *
     * @param y
     * @param bMutationParameter
     * @return
     */
    private double delta(double y, double bMutationParameter) {
        double rand = PseudoRandom.randDouble();
        int it, maxIt;
        it = currentIteration_;
        maxIt = maxIterations_;

        return (y * (1.0 - Math.pow(rand, Math.pow((1.0 - it / (double) maxIt), bMutationParameter))));
    }

    public Object execute(Object object) throws JMException {
        Solution solution = (Solution) object;

        if (!VALID_TYPES.contains(solution.getType().getClass())) {
            Configuration.logger_.severe("NonUniformMutation.execute: the solution " +
                    solution.getType() + "is not of the right type");
            Class cls = String.class;
            String name = cls.getName();
            throw new JMException("Exception in " + name + ".execute()");
        }

        if (getParameter("currentIteration") != null) {
            currentIteration_ = (Integer) getParameter("currentIteration");
        }

        doMutation(mutationProbability_, solution);
        return solution;
    }
}
