package opt.jmetal.util.archive.impl;

import opt.jmetal.solution.Solution;
import opt.jmetal.util.archive.Archive;
import opt.jmetal.util.archive.BoundedArchive;

import java.util.List;

/**
 * @param <S>
 * @author Antonio J. Nebro <antonio@lcc.uma.es>
 */
@SuppressWarnings("serial")
public abstract class AbstractBoundedArchive<S extends Solution<?>> implements BoundedArchive<S> {
    protected NonDominatedSolutionListArchive<S> archive;
    protected int maxSize;

    public AbstractBoundedArchive(int maxSize) {
        this.maxSize = maxSize;
        this.archive = new NonDominatedSolutionListArchive<S>();
    }

    @Override
    public boolean add(S solution) {
        boolean success = archive.add(solution);
        if (success) {
            prune();
        }

        return success;
    }

    @Override
    public S get(int index) {
        return getSolutionList().get(index);
    }

    @Override
    public List<S> getSolutionList() {
        return archive.getSolutionList();
    }

    @Override
    public int size() {
        return archive.size();
    }

    @Override
    public int getMaxSize() {
        return maxSize;
    }

    public abstract void prune();

    public Archive<S> join(Archive<S> archive) {
        for (S solution : archive.getSolutionList()) {
            this.add(solution);
        }

        return archive;
    }
}
