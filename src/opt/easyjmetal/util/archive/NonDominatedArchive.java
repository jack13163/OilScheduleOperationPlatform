package opt.easyjmetal.util.archive;

import opt.easyjmetal.core.Problem;
import opt.easyjmetal.core.Solution;
import opt.easyjmetal.util.JMException;
import opt.easyjmetal.util.comparators.DominanceComparator;
import opt.easyjmetal.util.comparators.EqualSolutionsComparator;
import opt.easyjmetal.util.pseudorandom.JMetalRandom;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

/**
 * 仅包含非支配解的储备集
 */
public class NonDominatedArchive extends Archive {
    private List<Solution> solutionList;
    private Comparator<Solution> dominanceComparator;
    private Comparator<Solution> equalSolutions = new EqualSolutionsComparator();

    public NonDominatedArchive() {
        dominanceComparator = new DominanceComparator();
        solutionList = new ArrayList<>();
    }

    @Override
    public boolean add(Solution solution) {
        boolean solutionInserted = false;
        if (solutionList.size() == 0) {
            solutionList.add(solution);
            solutionInserted = true;
        } else {
            Iterator<Solution> iterator = solutionList.iterator();
            boolean isDominated = false;

            boolean isContained = false;
            while (((!isDominated) && (!isContained)) && (iterator.hasNext())) {
                Solution listIndividual = iterator.next();
                int flag = dominanceComparator.compare(solution, listIndividual);
                if (flag == -1) {
                    iterator.remove();
                } else if (flag == 1) {
                    isDominated = true; // dominated by one in the list
                } else if (flag == 0) {
                    int equalflag = equalSolutions.compare(solution, listIndividual);
                    if (equalflag == 0) // solutions are equals
                        isContained = true;
                }
            }

            if (!isDominated && !isContained) {
                solutionList.add(solution);
                solutionInserted = true;
            }

            return solutionInserted;
        }

        return solutionInserted;
    }

    public Archive addAll(List<Solution> list) {
        for (Solution solution : list) {
            this.add(solution);
        }
        return this;
    }

    @Override
    public int size() {
        return solutionList.size();
    }

    @Override
    public Solution get(int index) {
        return solutionList.get(index);
    }

    public static void main(String args[]) throws ClassNotFoundException, JMException {
        JMetalRandom.getInstance().setSeed(1L);
        Archive archive = new NonDominatedArchive();
        Problem problem = new MockedDoubleProblem1(100);
        long initTime = System.currentTimeMillis();
        for (int i = 0; i < 1000000; i++) {
            Solution solution = new Solution(problem);
            problem.evaluate(solution);
            archive.add(solution);
        }
        System.out.println("Time: " + (System.currentTimeMillis() - initTime));
    }

    private static class MockedDoubleProblem1 extends Problem {
        public MockedDoubleProblem1(int numberOfVariables) {
            setNumberOfVariables(numberOfVariables);
            numberOfVariables_ = numberOfVariables;
            numberOfConstraints_ = 0;
            numberOfObjectives_ = 2;

            lowerLimit_ = new double[numberOfVariables_];
            upperLimit_ = new double[numberOfVariables_];
            for (int i = 0; i < numberOfVariables_; i++) {
                lowerLimit_[i] = 0.0;
                upperLimit_[i] = 1.0;
            }
        }

        public void evaluate(Solution solution) throws JMException {
            double[] f = new double[getNumberOfObjectives()];

            f[0] = solution.getDecisionVariables()[0].getValue() + 0.0;
            double g = this.evalG(solution);
            double h = this.evalH(f[0], g);
            f[1] = h * g;

            solution.setObjective(0, f[0]);
            solution.setObjective(1, f[1]);
        }

        /**
         * Returns the value of the ZDT1 function G.
         *
         * @param solution Solution
         */
        private double evalG(Solution solution) throws JMException {
            double g = 0.0;
            for (int i = 1; i < numberOfVariables_; i++) {
                g += solution.getDecisionVariables()[i].getValue();
            }
            double constant = 9.0 / (numberOfVariables_ - 1.0);
            g = constant * g;
            g = g + 1.0;
            return g;
        }

        /**
         * Returns the value of the ZDT1 function H.
         *
         * @param f First argument of the function H.
         * @param g Second argument of the function H.
         */
        public double evalH(double f, double g) {
            double h;
            h = 1.0 - Math.sqrt(f / g);
            return h;
        }
    }
}
