package opt.easyjmetal.core;

import opt.easyjmetal.util.Configuration;

import java.io.*;
import java.util.*;

public class SolutionSet implements Serializable {

    protected final List<Solution> solutionsList_;
    private int capacity_ = Integer.MAX_VALUE;

    public SolutionSet() {
        solutionsList_ = new ArrayList<>();
    }

    public SolutionSet(int maximumSize) {
        solutionsList_ = new ArrayList<>();
        capacity_ = maximumSize;
    }

    public boolean add(Solution solution) {
        if (solutionsList_.size() == capacity_) {
            Configuration.logger_.severe("The population is full");
            Configuration.logger_.severe("Capacity is : " + capacity_);
            Configuration.logger_.severe("\t Size is: " + this.size());
            return false;
        }

        solutionsList_.add(solution);
        return true;
    }

    public boolean add(int index, Solution solution) {
        solutionsList_.add(index, solution);
        return true;
    }

    public double MaxOverallConViolation() {
        if ((solutionsList_ == null) || (this.solutionsList_.isEmpty())) {
            return -1;
        }
        Comparator temp = new Comparator() {
            @Override
            public int compare(Object o1, Object o2) {
                Solution a = (Solution) o1;
                Solution b = (Solution) o2;
                return (int) a.getOverallConstraintViolation() - (int) b.getOverallConstraintViolation();
            }
        };
        Solution worstKnown = solutionsList_.get(0), candidateSolution;
        int flag;
        for (int i = 1; i < solutionsList_.size(); i++) {
            candidateSolution = solutionsList_.get(i);
            flag = temp.compare(worstKnown, candidateSolution);
            if (flag == -1) {
                worstKnown = candidateSolution;
            }
        }

        return worstKnown.getOverallConstraintViolation();
    }

    public Solution get(int i) {
        if (i >= solutionsList_.size()) {
            throw new IndexOutOfBoundsException("Index out of Bound " + i);
        }
        return solutionsList_.get(i);
    }

    public int getMaxSize() {
        return capacity_;
    } // getMaxSize

    public void sort(Comparator comparator) {
        if (comparator == null) {
            Configuration.logger_.severe("No criterium for comparing exist");
            return;
        }
        Collections.sort(solutionsList_, comparator);
    }

    /**
     * Returns the index of the best Solution using a <code>Comparator</code>.
     * If there are more than one occurrences, only the index of the first one is returned
     *
     * @param comparator <code>Comparator</code> used to compare solutions.
     * @return The index of the best Solution attending to the comparator or
     * <code>-1<code> if the SolutionSet is empty
     */
    public int indexBest(Comparator comparator) {
        if ((solutionsList_ == null) || (this.solutionsList_.isEmpty())) {
            return -1;
        }

        int index = 0;
        Solution bestKnown = solutionsList_.get(0), candidateSolution;
        int flag;
        for (int i = 1; i < solutionsList_.size(); i++) {
            candidateSolution = solutionsList_.get(i);
            flag = comparator.compare(bestKnown, candidateSolution);
            if (flag == +1) {
                index = i;
                bestKnown = candidateSolution;
            }
        }

        return index;
    }

    /**
     * Returns the best Solution using a <code>Comparator</code>.
     * If there are more than one occurrences, only the first one is returned
     *
     * @param comparator <code>Comparator</code> used to compare solutions.
     * @return The best Solution attending to the comparator or <code>null<code>
     * if the SolutionSet is empty
     */
    public Solution best(Comparator comparator) {
        int indexBest = indexBest(comparator);
        if (indexBest < 0) {
            return null;
        } else {
            return solutionsList_.get(indexBest);
        }
    }

    /**
     * Returns the index of the worst Solution using a <code>Comparator</code>.
     * If there are more than one occurrences, only the index of the first one is returned
     *
     * @param comparator <code>Comparator</code> used to compare solutions.
     * @return The index of the worst Solution attending to the comparator or
     * <code>-1<code> if the SolutionSet is empty
     */
    public int indexWorst(Comparator comparator) {
        if ((solutionsList_ == null) || (this.solutionsList_.isEmpty())) {
            return -1;
        }
        int index = 0;
        Solution worstKnown = solutionsList_.get(0), candidateSolution;
        int flag;
        for (int i = 1; i < solutionsList_.size(); i++) {
            candidateSolution = solutionsList_.get(i);
            flag = comparator.compare(worstKnown, candidateSolution);
            if (flag == -1) {
                index = i;
                worstKnown = candidateSolution;
            }
        }
        return index;
    }

    /**
     * Returns the worst Solution using a <code>Comparator</code>.
     * If there are more than one occurrences, only the first one is returned
     *
     * @param comparator <code>Comparator</code> used to compare solutions.
     * @return The worst Solution attending to the comparator or <code>null<code>
     * if the SolutionSet is empty
     */
    public Solution worst(Comparator comparator) {
        int index = indexWorst(comparator);
        if (index < 0) {
            return null;
        } else {
            return solutionsList_.get(index);
        }
    }

    /**
     * Returns the number of solutions in the SolutionSet.
     *
     * @return The size of the SolutionSet.
     */
    public int size() {
        return solutionsList_.size();
    }

    /**
     * Writes the objective function values of the <code>Solution</code>
     * objects into the set in a file.
     *
     * @param path The output file name
     */
    public void printObjectivesToFile(String path) {
        try {
            /* Open the file */
            FileOutputStream fos = new FileOutputStream(path);
            OutputStreamWriter osw = new OutputStreamWriter(fos);
            BufferedWriter bw = new BufferedWriter(osw);

            for (Solution aSolutionsList_ : solutionsList_) {
                bw.write(aSolutionsList_.toString());
                bw.newLine();
            }

            bw.close();
        } catch (IOException e) {
            Configuration.logger_.severe("Error acceding to the file");
            e.printStackTrace();
        }
    }

    /**
     * Writes the decision encodings.variable values of the <code>Solution</code>
     * solutions objects into the set in a file.
     *
     * @param path The output file name
     */
    public void printVariablesToFile(String path) {
        try {
            FileOutputStream fos = new FileOutputStream(path);
            OutputStreamWriter osw = new OutputStreamWriter(fos);
            BufferedWriter bw = new BufferedWriter(osw);

            if (size() > 0) {
                int numberOfVariables = solutionsList_.get(0).getDecisionVariables().length;
                for (Solution aSolutionsList_ : solutionsList_) {
                    for (int j = 0; j < numberOfVariables; j++) {
                        bw.write(aSolutionsList_.getDecisionVariables()[j].toString() + " ");
                    }
                    bw.newLine();
                }
            }
            bw.close();
        } catch (IOException e) {
            Configuration.logger_.severe("Error acceding to the file");
            e.printStackTrace();
        }
    }

    /**
     * Write the function values of feasible solutions into a file
     *
     * @param path File name
     */
    public void printFeasibleFUN(String path) {
        try {
            FileOutputStream fos = new FileOutputStream(path);
            OutputStreamWriter osw = new OutputStreamWriter(fos);
            BufferedWriter bw = new BufferedWriter(osw);

            for (Solution aSolutionsList_ : solutionsList_) {
                if (aSolutionsList_.getOverallConstraintViolation() == 0.0) {
                    bw.write(aSolutionsList_.toString());
                    bw.newLine();
                }
            }
            bw.close();
        } catch (IOException e) {
            Configuration.logger_.severe("Error acceding to the file");
            e.printStackTrace();
        }
    }

    /**
     * Write the encodings.variable values of feasible solutions into a file
     *
     * @param path File name
     */
    public void printFeasibleVAR(String path) {
        try {
            FileOutputStream fos = new FileOutputStream(path);
            OutputStreamWriter osw = new OutputStreamWriter(fos);
            BufferedWriter bw = new BufferedWriter(osw);

            if (size() > 0) {
                int numberOfVariables = solutionsList_.get(0).getDecisionVariables().length;
                for (Solution aSolutionsList_ : solutionsList_) {
                    if (aSolutionsList_.getOverallConstraintViolation() == 0.0) {
                        for (int j = 0; j < numberOfVariables; j++) {
                            bw.write(aSolutionsList_.getDecisionVariables()[j].toString() + " ");
                        }
                        bw.newLine();
                    }
                }
            }
            bw.close();
        } catch (IOException e) {
            Configuration.logger_.severe("Error acceding to the file");
            e.printStackTrace();
        }
    }

    /**
     * Empties the SolutionSet
     */
    public void clear() {
        solutionsList_.clear();
    }

    /**
     * Deletes the <code>Solution</code> at position i in the set.
     *
     * @param i The position of the solution to remove.
     */
    public void remove(int i) {
        if (i > solutionsList_.size() - 1) {
            Configuration.logger_.severe("Size is: " + this.size());
        }
        solutionsList_.remove(i);
    }


    /**
     * Returns an <code>Iterator</code> to access to the solution set list.
     *
     * @return the <code>Iterator</code>.
     */
    public Iterator<Solution> iterator() {
        return solutionsList_.iterator();
    }

    /**
     * 合并两个种群
     * @param solutionSet 要合并的另一个种群
     * @return
     */
    public SolutionSet union(SolutionSet solutionSet) {
        //Check the correct size. In development
        int newSize = this.size() + solutionSet.size();
        if (newSize < capacity_) {
            newSize = capacity_;
        }

        //Create a new population
        SolutionSet union = new SolutionSet(newSize);
        for (int i = 0; i < this.size(); i++) {
            union.add(this.get(i));
        }

        for (int i = this.size(); i < (this.size() + solutionSet.size()); i++) {
            union.add(solutionSet.get(i - this.size()));
        }

        return union;
    }

    /**
     * Replaces a solution by a new one
     *
     * @param position The position of the solution to replace
     * @param solution The new solution
     */
    public void replace(int position, Solution solution) {
        if (position > this.solutionsList_.size()) {
            solutionsList_.add(solution);
        }
        solutionsList_.remove(position);
        solutionsList_.add(position, solution);
    }

    /**
     * Copies the objectives of the solution set to a matrix
     *
     * @return A matrix containing the objectives
     */
    public double[][] writeObjectivesToMatrix() {
        if (this.size() == 0) {
            return null;
        }
        double[][] objectives;
        objectives = new double[size()][get(0).getNumberOfObjectives()];
        for (int i = 0; i < size(); i++) {
            for (int j = 0; j < get(0).getNumberOfObjectives(); j++) {
                objectives[i][j] = get(i).getObjective(j);
            }
        }
        return objectives;
    }

    public void printObjectives() {
        for (int i = 0; i < solutionsList_.size(); i++) {
            System.out.println("" + solutionsList_.get(i));
        }
    }

    public void setCapacity(int capacity) {
        capacity_ = capacity;
    }

    public int getCapacity() {
        return capacity_;
    }

    /**
     * 获取可行解集
     * @return
     */
    public SolutionSet getFeasible() {
        SolutionSet result = new SolutionSet(solutionsList_.size());
        for (Solution aSolutionsList_ : solutionsList_) {
            if (aSolutionsList_.getOverallConstraintViolation() >= 0) {
                result.add(aSolutionsList_);
            }
        }
        return result;
    }

    public List<Integer> getFeasibelIndex() {
        List<Integer> result = new ArrayList<Integer>(solutionsList_.size());
        for (int i = 0; i < solutionsList_.size(); i++) {
            if (solutionsList_.get(i).getOverallConstraintViolation() == 0) {
                result.add(i);
            }
        }
        return result;
    }

    /**
     * 获取不可行解集
     * @return
     */
    public SolutionSet getInfeasible() {
        SolutionSet result = new SolutionSet(solutionsList_.size());
        for (Solution aSolutionsList_ : solutionsList_) {
            if (aSolutionsList_.getOverallConstraintViolation() < 0) {
                result.add(aSolutionsList_);
            }
        }
        return result;
    }

    public List<Integer> getInfeasibelIndex() {
        List<Integer> result = new ArrayList<Integer>(solutionsList_.size());
        for (int i = 0; i < solutionsList_.size(); i++) {
            if (solutionsList_.get(i).getOverallConstraintViolation() < 0) {
                result.add(i);
            }
        }
        return result;
    }

    public double GetFeasible_Ratio() {
        double result = 0.0;
        for (Solution aSolutionsList_ : solutionsList_) {
            if (aSolutionsList_.getOverallConstraintViolation() == 0.0) {
                result += 1;
            }
        }
        return result / solutionsList_.size();
    }

    public void removeDuplicate() {
        HashSet h = new HashSet(solutionsList_);
        solutionsList_.clear();
        solutionsList_.addAll(h);
    }
}
