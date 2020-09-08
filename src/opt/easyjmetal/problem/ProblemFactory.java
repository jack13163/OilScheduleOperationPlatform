//  ProblemFactory.java
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

package opt.easyjmetal.problem;

import opt.easyjmetal.core.Problem;
import opt.easyjmetal.util.Configuration;
import opt.easyjmetal.util.JMException;

import java.lang.reflect.Constructor;

/**
 * This class represents a factory for problems
 */
public class ProblemFactory {
    /**
     * Creates an object representing a problem
     *
     * @param name   Name of the problem
     * @param params Parameters characterizing the problem
     * @return The object representing the problem
     * @throws JMException
     */
    public Problem getProblem(String name, Object[] params) throws JMException {
        // Params are the arguments
        // The number of argument must correspond with the problem constructor params

        String base = "opt.easyjmetal.problem.";
        if (name.substring(0, name.length() - 1).equalsIgnoreCase("LIRCMOP")) {
            base += "LIRCMOP.";
        } else if (name.substring(0, name.length() - 2).equalsIgnoreCase("LIRCMOP")) {
            base += "LIRCMOP.";
        } else if (name.equalsIgnoreCase("EDFPS") || name.equalsIgnoreCase("EDFTSS")) {
            base += "schedule.cop.";
        } else if (name.equalsIgnoreCase("EDFPS") || name.equalsIgnoreCase("SJOIL")) {
            base += "sj.";
        }

        try {
            Class problemClass = Class.forName(base + name);
            Constructor[] constructors = problemClass.getConstructors();
            int i = 0;
            //find the constructor
            while ((i < constructors.length) &&
                    (constructors[i].getParameterTypes().length != params.length)) {
                i++;
            }
            // constructors[i] is the selected one constructor
            Problem problem = (Problem) constructors[i].newInstance(params);
            return problem;
        }// try
        catch (Exception e) {
            Configuration.logger_.severe("ProblemFactory.getProblem: " +
                    "Problem '" + name + "' does not exist. " +
                    "Please, check the problem names in opt.easyjmetal.problem");
            e.printStackTrace();
            throw new JMException("Exception in " + name + ".getProblem()");
        } // catch
    }
}
