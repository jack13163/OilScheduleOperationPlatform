package opt.jmetal.util.archive.impl;

import opt.jmetal.qualityindicator.impl.Hypervolume;
import opt.jmetal.solution.Solution;
import opt.jmetal.util.comparator.HypervolumeContributionComparator;
import opt.jmetal.util.SolutionListUtils;

import java.util.Collections;
import java.util.Comparator;

/**
 * Created by Antonio J. Nebro on 24/09/14.
 */
@SuppressWarnings("serial")
public class HypervolumeArchive<S extends Solution<?>> extends AbstractBoundedArchive<S> {
    private Comparator<S> comparator;
    Hypervolume<S> hypervolume;

    public HypervolumeArchive(int maxSize, Hypervolume<S> hypervolume) {
        super(maxSize);
        comparator = new HypervolumeContributionComparator<S>();
        this.hypervolume = hypervolume;
    }

    @Override
    public void prune() {
        if (getSolutionList().size() > getMaxSize()) {
            computeDensityEstimator();
            S worst = new SolutionListUtils().findWorstSolution(getSolutionList(), comparator);
            getSolutionList().remove(worst);
        }
    }

    @Override
    public Comparator<S> getComparator() {
        return comparator;
    }

    @Override
    public void computeDensityEstimator() {
        hypervolume.computeHypervolumeContribution(archive.getSolutionList(), archive.getSolutionList());
    }

    @Override
    public void sortByDensityEstimator() {
        Collections.sort(getSolutionList(), new HypervolumeContributionComparator<S>());
    }
}
