//  DistanceNode.java
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
package opt.easyjmetal.util;


/**
 * This is an auxiliar class for calculating the SPEA2 environmental selection.
 * Each instance of DistanceNode contains two parameter called
 * <code>reference_</code> and <code>distance_</code>.
 * <code>reference_</code> indicates one <code>Solution</code> in a
 * <code>SolutionSet</code> and <code>distance_</code> represents the distance_
 * to this solution.
 */
public class DistanceNode {

    private int reference_;
    private double distance_;

    /**
     * Constructor.
     *
     * @param distance  The distance to a <code>Solution</code>.
     * @param reference The position of the <code>Solution</code>.
     */
    public DistanceNode(double distance, int reference) {
        distance_ = distance;
        reference_ = reference;
    }

    public void setDistance(double distance) {
        distance_ = distance;
    }

    public void setReferece(int reference) {
        reference_ = reference;
    }

    public double getDistance() {
        return distance_;
    }

    public int getReference() {
        return reference_;
    }
}
