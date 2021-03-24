package opt.easyjmetal.util.comparators.one;

import opt.easyjmetal.core.Solution;

import java.util.Comparator;

public class DistanceComparator implements Comparator {
    @Override
    public int compare(Object o1, Object o2) {
        if (o1 == null) {
            return 1;
        } else if (o2 == null) {
            return -1;
        }
        /* His rank is equal, then distance crowding comparator */
        double distance1 = ((Solution) o1).getCrowdingDistance();
        double distance2 = ((Solution) o2).getCrowdingDistance();
        if (distance1 > distance2) {
            return -1;
        }

        if (distance1 < distance2) {
            return 1;
        }

        return 0;
    }
}
