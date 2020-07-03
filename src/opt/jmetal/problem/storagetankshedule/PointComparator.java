package opt.jmetal.problem.storagetankshedule;

import opt.jmetal.util.JMetalException;
import opt.jmetal.util.point.Point;

import java.util.Comparator;

public class PointComparator implements Comparator<Point> {

    public enum Ordering {ASCENDING, DESCENDING}

    private Ordering order;
    private int objectiveId;

    public PointComparator(int objectiveId, Ordering order) {
        this.objectiveId = objectiveId;
        this.order = order;
    }

    @Override
    public int compare(Point solution1, Point solution2) {
        int result;
        if (solution1 == null) {
            if (solution2 == null) {
                result = 0;
            } else {
                result = 1;
            }
        } else if (solution2 == null) {
            result = -1;
        } else if (solution1.getDimension() <= objectiveId) {
            throw new JMetalException("The solution1 has " + solution1.getDimension() + " objectives "
                    + "and the objective to sort is " + objectiveId);
        } else if (solution2.getDimension() <= objectiveId) {
            throw new JMetalException("The solution2 has " + solution2.getDimension() + " objectives "
                    + "and the objective to sort is " + objectiveId);
        } else {
            Double objective1 = solution1.getValue(this.objectiveId);
            Double objective2 = solution2.getValue(this.objectiveId);
            if (order == Ordering.ASCENDING) {
                result = Double.compare(objective1, objective2);
            } else {
                result = Double.compare(objective2, objective1);
            }
        }
        return result;
    }
}
