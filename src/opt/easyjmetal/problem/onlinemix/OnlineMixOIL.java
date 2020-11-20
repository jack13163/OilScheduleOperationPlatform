//  Binh2.java
//
//  Author:
//       Antonio J. Nebro <antonio@lcc.uma.es>
//
//  Copyright (c) 2012 Antonio J. Nebro
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

package opt.easyjmetal.problem.onlinemix;

import opt.easyjmetal.core.Problem;
import opt.easyjmetal.core.Solution;
import opt.easyjmetal.core.Variable;
import opt.easyjmetal.encodings.solutiontype.BinaryRealSolutionType;
import opt.easyjmetal.encodings.solutiontype.RealSolutionType;
import opt.easyjmetal.util.JMException;
import opt.easyjmetal.util.wrapper.XReal;

import java.util.List;

/**
 * 原油调度问题，通过反射和工厂模式创建问题对象
 **/
public class OnlineMixOIL extends Problem {

    public OnlineMixOIL(String solutionType) {
        numberOfVariables_ = 125;
        numberOfObjectives_ = 5;
        problemName_ = "OnlineMixOIL";

        lowerLimit_ = new double[numberOfVariables_];
        upperLimit_ = new double[numberOfVariables_];
        for (int i = 0; i < numberOfVariables_; i++) {
            lowerLimit_[i] = 0.0;
            upperLimit_[i] = 1.0;
        }

        if (solutionType.compareTo("BinaryReal") == 0) {
            solutionType_ = new BinaryRealSolutionType(this);
        } else if (solutionType.compareTo("Real") == 0) {
            solutionType_ = new RealSolutionType(this);
        } else {
            System.out.println("Error: solution type " + solutionType + " invalid");
            System.exit(-1);
        }
    }

    /**
     * 评价适应度
     *
     * @param solution 个体【染色体/解】
     * @throws JMException
     */
    @Override
    public void evaluate(Solution solution) throws JMException {
        // 解码前的准备操作
        XReal vars = new XReal(solution);
        double[] x = new double[numberOfVariables_];
        for (int i = 0; i < numberOfVariables_; i++) {
            x[i] = vars.getValue(i);
        }
        double[][] pop = new double[1][x.length];
        for (int i = 0; i < x.length; i++) {
            pop[0][i] = x[i];
        }

        // 解码
        List<List<Double>> eff = Oilschdule.fat(pop, false);

        // 更新解，因为解码过程中会修改染色体
        Variable[] decisionVariables = solution.getDecisionVariables();
        for (int i = 0; i < numberOfVariables_; i++) {
            decisionVariables[i].setValue(eff.get(0).get(i));
        }

        // 设置目标值
        solution.setObjective(0, eff.get(0).get(numberOfVariables_ + 0));
        solution.setObjective(1, eff.get(0).get(numberOfVariables_ + 1));
        solution.setObjective(2, eff.get(0).get(numberOfVariables_ + 2));
        solution.setObjective(3, eff.get(0).get(numberOfVariables_ + 3));
        solution.setObjective(4, eff.get(0).get(numberOfVariables_ + 4));
    }
}
