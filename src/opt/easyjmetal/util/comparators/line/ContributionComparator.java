package opt.easyjmetal.util.comparators.line;

import opt.easyjmetal.core.Solution;

import java.util.Comparator;

public class ContributionComparator implements Comparator {
    private static final Comparator dominance_ = new EqualSolutionsComparator();

    /**
     * Compares two solutions.
     * A <code>Solution</code> a is less than b for this <code>Comparator</code>.
     * if the crowding distance of a if greater than the crowding distance of b.
     *
     * @param o1 Object representing a <code>Solution</code>.
     * @param o2 Object representing a <code>Solution</code>.
     * @return -1, or 0, or 1 if o1 is less than, equals, or greater than o2,
     * respectively.
     */
    @Override
    public int compare(Object o1, Object o2) {
        // 相等的值贡献为0
        if(dominance_.compare(o1, o2) == 0){
            return -1;
        }

        double contribution1 = ((Solution) o1).getContribution();
        double contribution2 = ((Solution) o2).getContribution();

        if (contribution1 > contribution2) {
            return -1;
        } else if (contribution1 < contribution2) {
            return 1;
        } else {
            return new ConstraintDominanceComparator().compare(o1, o2);
        }
    }
}
