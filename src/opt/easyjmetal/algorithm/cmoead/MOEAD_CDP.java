//  MOEAD_CDP.java
//
//  Author:
//       wenji li  <wenji_li@126.com>

package opt.easyjmetal.algorithm.cmoead;

import opt.easyjmetal.core.*;
import opt.easyjmetal.util.JMException;
import opt.easyjmetal.util.PseudoRandom;
import opt.easyjmetal.util.jmathplot.ScatterPlot;
import opt.easyjmetal.util.sqlite.SqlUtils;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.StringTokenizer;
import java.util.Vector;


// This class implements a constrained version of the MOEAD algorithm based on
// the CDP method.
public class MOEAD_CDP extends Algorithm {

    private int populationSize_;
    /**
     * Stores the population
     */
    private SolutionSet population_;
    /**
     * Z vector (ideal point)
     */
    private double[] z_;
    /**
     * Lambda vectors
     */
    private double[][] lambda_;
    /**
     * T: neighbour size
     */
    private int T_;
    /**
     * Neighborhood
     */
    private int[][] neighborhood_;

    /**
     * nr: maximal number of solutions replaced by each child solution
     */
    private int nr_;
    private String functionType_;
    private int evaluations_;
    private String dataDirectory_;
    private ScatterPlot plot_;
    private SolutionSet external_archive_;

    /**
     * Constructor
     *
     * @param problem Problem to solve
     */
    public MOEAD_CDP(Problem problem) {
        super(problem);
        functionType_ = "_TCHE2";
    } // MOEAD_CDP

    public SolutionSet execute() throws JMException, ClassNotFoundException {
        int runningTime;
        evaluations_ = 0;
        int maxEvaluations_ = (Integer) getInputParameter("maxEvaluations");
        populationSize_ = (Integer) getInputParameter("populationSize");
        dataDirectory_ = getInputParameter("dataDirectory").toString();
        String methodName_ = getInputParameter("AlgorithmName").toString();
        boolean isDisplay_ = (Boolean) getInputParameter("isDisplay");
        int plotFlag_ = (Integer) getInputParameter("plotFlag");
        runningTime = (Integer) getInputParameter("runningTime") + 1; // start from 1
        population_ = new SolutionSet(populationSize_);
        T_ = (Integer) getInputParameter("T");
        nr_ = (Integer) getInputParameter("nr");
        double delta_ = (Double) getInputParameter("delta");
        neighborhood_ = new int[populationSize_][T_];
        String paratoFilePath_ = this.getInputParameter("paretoPath").toString();
        z_ = new double[problem_.getNumberOfObjectives()];
        lambda_ = new double[populationSize_][problem_.getNumberOfObjectives()];
        Operator crossover_ = operators_.get("crossover"); // default: DE crossover
        Operator mutation_ = operators_.get("mutation");  // default: polynomial mutation

        //creat database
        String problemName = problem_.getName() + "_" + Integer.toString(runningTime);
        SqlUtils.CreateTable(problemName, methodName_);

        // STEP 1. Initialization
        // STEP 1.1. Compute euclidean distances between weight vectors and find T
        initUniformWeight();

        initNeighborhood();

        // STEP 1.2. Initialize population

        initPopulation();

        // Initialize the external archive
        external_archive_ = new SolutionSet(populationSize_);
        Utils.initializeExternalArchive(population_, populationSize_, external_archive_);

        SolutionSet allPop = population_;

        // STEP 1.3. Initialize z_
        initIdealPoint();

        //display constraint info
        if (isDisplay_ && paratoFilePath_ != null) {
            if (plotFlag_ == 0) {
                plot_ = new ScatterPlot(this.getClass().getName(), problem_.getName(), population_);
            }
            if (plotFlag_ == 1) {
                plot_ = new ScatterPlot(this.getClass().getName(), problem_.getName(), external_archive_);
            }
            plot_.displayPf(paratoFilePath_);
        }

        int gen = 0;

        // STEP 2. Update
        do {
            int[] permutation = new int[populationSize_];
            Utils.randomPermutation(permutation, populationSize_);

            for (int i = 0; i < populationSize_; i++) {
                int n = permutation[i]; // or int n = i;
                //int n = i ; // or int n = i;
                int type;
                double rnd = PseudoRandom.randDouble();

                // STEP 2.1. Mating selection based on probability
                if (rnd < delta_) // if (rnd < realb)
                {
                    type = 1;   // neighborhood
                } else {
                    type = 2;   // whole population
                }
                Vector<Integer> p = new Vector<Integer>();
                matingSelection(p, n, 2, type);

                // STEP 2.2. Reproduction
                Solution child = null;

                // Apply Crossover for Real codification
                if (crossover_.getClass().getSimpleName().equalsIgnoreCase("SBXCrossover")) {
                    Solution[] parents = new Solution[2];
                    parents[0] = population_.get(p.get(0));
                    parents[1] = population_.get(n);
                    child = ((Solution[]) crossover_.execute(parents))[0];
                }
                // Apply DE crossover
                else if (crossover_.getClass().getSimpleName().equalsIgnoreCase("DifferentialEvolutionCrossover")) {
                    Solution[] parents = new Solution[3];
                    parents[0] = population_.get(p.get(0));
                    parents[1] = population_.get(p.get(1));
                    parents[2] = population_.get(n);
                    child = (Solution) crossover_.execute(new Object[]{population_.get(n), parents});
                } else {
                    System.out.println("unknown crossover");
                }
                // Apply mutation
                mutation_.execute(child);


                // Evaluation
                problem_.evaluate(child);
                problem_.evaluateConstraints(child);
                evaluations_++;

                // STEP 2.3. Repair. Not necessary

                // STEP 2.4. Update z_
                updateReference(child);

                // STEP 2.5. Update of solutions
                updateProblem(child, n, type);
            } // for

            // Update the external archive
            Utils.updateExternalArchive(population_, populationSize_, external_archive_);
            allPop = allPop.union(population_);
            // display populations
            if (isDisplay_) {
                plotPopulation(plotFlag_);
            }
            gen = gen + 1;

        } while (evaluations_ < maxEvaluations_);

        allPop = allPop.union(external_archive_);
        SqlUtils.InsertSolutionSet(problemName, allPop);
        return external_archive_;
    }

    /**
     * initUniformWeight
     */
    private void initUniformWeight() {
        if ((problem_.getNumberOfObjectives() == 2) && (populationSize_ <= 300)) {
            for (int n = 0; n < populationSize_; n++) {
                double a = 1.0 * n / (populationSize_ - 1);
                lambda_[n][0] = a;
                lambda_[n][1] = 1 - a;
            } // for
        } // if
        else {
            String dataFileName;
            dataFileName = "W" + problem_.getNumberOfObjectives() + "D_" +
                    populationSize_ + ".dat";

            try {
                // Open the file
                FileInputStream fis = new FileInputStream(dataDirectory_ + "/" + dataFileName);
                InputStreamReader isr = new InputStreamReader(fis);
                BufferedReader br = new BufferedReader(isr);
                int i = 0;
                int j;
                String aux = br.readLine();
                while (aux != null) {
                    StringTokenizer st = new StringTokenizer(aux);
                    j = 0;
                    while (st.hasMoreTokens()) {
                        double value = new Double(st.nextToken());
                        lambda_[i][j] = value;
                        //System.out.println("lambda["+i+","+j+"] = " + value) ;
                        j++;
                    }
                    aux = br.readLine();
                    i++;
                }
                br.close();
            } catch (Exception e) {
                System.out.println("initUniformWeight: failed when reading for file: " + dataDirectory_ + "/" + dataFileName);
                e.printStackTrace();
            }
        } // else

        //System.exit(0) ;
    } // initUniformWeight

    /**
     *
     */
    private void initNeighborhood() {
        double[] x = new double[populationSize_];
        int[] idx = new int[populationSize_];

        for (int i = 0; i < populationSize_; i++) {
            // calculate the distances based on weight vectors
            for (int j = 0; j < populationSize_; j++) {
                x[j] = Utils.distVector(lambda_[i], lambda_[j]);
                idx[j] = j;
            } // for

            // find 'niche' nearest neighboring subproblems
            Utils.minFastSort(x, idx, populationSize_, T_);
            System.arraycopy(idx, 0, neighborhood_[i], 0, T_);
        } // for
    } // initNeighborhood

    /**
     *
     */
    private void initPopulation() throws JMException, ClassNotFoundException {
        for (int i = 0; i < populationSize_; i++) {
            Solution newSolution = new Solution(problem_);
            problem_.evaluate(newSolution);
            problem_.evaluateConstraints(newSolution);
            evaluations_++;
            population_.add(newSolution);
        } // for
    } // initPopulation

    private void initIdealPoint() throws JMException, ClassNotFoundException {
        for (int i = 0; i < problem_.getNumberOfObjectives(); i++) {
            z_[i] = 1.0e+30;
        } // for

        for (int i = 0; i < populationSize_; i++) {
            updateReference(population_.get(i));
        } // for
    } // initIdealPoint

    private void matingSelection(Vector<Integer> list, int cid, int size, int type) {
        // list : the set of the indexes of selected mating parents
        // cid  : the id of current subproblem
        // size : the number of selected mating parents
        // type : 1 - neighborhood; otherwise - whole population
        int ss;
        int r;
        int p;

        ss = neighborhood_[cid].length;
        while (list.size() < size) {
            if (type == 1) {
                r = PseudoRandom.randInt(0, ss - 1);
                p = neighborhood_[cid][r];
                //p = population[cid].table[r];
            } else {
                p = PseudoRandom.randInt(0, populationSize_ - 1);
            }
            boolean flag = true;
            for (Integer aList : list) {
                if (aList == p) // p is in the list
                {
                    flag = false;
                    break;
                }
            }

            //if (flag) list.push_back(p);
            if (flag) {
                list.addElement(p);
            }
        }
    } // matingSelection

    private void updateReference(Solution individual) {
        for (int n = 0; n < problem_.getNumberOfObjectives(); n++) {
            if (individual.getObjective(n) < z_[n]) {
                z_[n] = individual.getObjective(n);
            }
        }
    } // updateReference

    private void updateProblem(Solution indiv, int id, int type) {
        // indiv: child solution
        // id:   the id of current subproblem
        // type: update solutions in - neighborhood (1) or whole population (otherwise)
        int size;
        int time;

        time = 0;

        if (type == 1) {
            size = neighborhood_[id].length;
        } else {
            size = population_.size();
        }
        int[] perm = new int[size];

        Utils.randomPermutation(perm, size);

        for (int i = 0; i < size; i++) {
            int k;
            if (type == 1) {
                k = neighborhood_[id][perm[i]];
            } else {
                k = perm[i];      // calculate the values of objective function regarding the current subproblem
            }
            double f1, f2, con1, con2;

            f1 = fitnessFunction(population_.get(k), lambda_[k]);
            f2 = fitnessFunction(indiv, lambda_[k]);

            con1 = population_.get(k).getOverallConstraintViolation();
            con2 = indiv.getOverallConstraintViolation();

            // use CDP method
            if (con1 == con2) {
                if (f2 < f1) {
                    population_.replace(k, new Solution(indiv));
                    time++;
                }
            } else if (con2 > con1) {
                population_.replace(k, new Solution(indiv));
                time++;
            }
            if (time >= nr_) {
                return;
            }

        }
    } // updateProblem

    private double fitnessFunction(Solution individual, double[] lambda) {
        double fitness;
        fitness = 0.0;

        if (functionType_.equals("_TCHE1")) {
            double maxFun = -1.0e+30;

            for (int n = 0; n < problem_.getNumberOfObjectives(); n++) {
                double diff = Math.abs(individual.getObjective(n) - z_[n]);

                double feval;
                if (lambda[n] == 0) {
                    feval = 0.0001 * diff;
                } else {
                    feval = diff * lambda[n];
                }
                if (feval > maxFun) {
                    maxFun = feval;
                }
            } // for

            fitness = maxFun;
        } else if (functionType_.equals("_TCHE2")) {
            double maxFun = -1.0e+30;

            for (int i = 0; i < problem_.getNumberOfObjectives(); i++) {
                double diff = Math.abs(individual.getObjective(i) - z_[i]);

                double feval;
                if (lambda[i] == 0) {
                    feval = diff / 0.000001;
                } else {
                    feval = diff / lambda[i];
                }
                if (feval > maxFun) {
                    maxFun = feval;
                }
            } // for
            fitness = maxFun;
        } else if (functionType_.equals("_PBI")) {
            double theta; // penalty parameter
            theta = 5.0;

            // normalize the weight vector (line segment)
            double nd = Utils.norm_vector(lambda, problem_.getNumberOfObjectives());
            for (int i = 0; i < problem_.getNumberOfObjectives(); i++)
                lambda[i] = lambda[i] / nd;

            double[] realA = new double[problem_.getNumberOfObjectives()];
            double[] realB = new double[problem_.getNumberOfObjectives()];

            // difference between current point and reference point
            for (int n = 0; n < problem_.getNumberOfObjectives(); n++)
                realA[n] = (individual.getObjective(n) - z_[n]);

            // distance along the line segment
            double d1 = Math.abs(Utils.innerproduct(realA, lambda));

            // distance to the line segment
            for (int n = 0; n < problem_.getNumberOfObjectives(); n++)
                realB[n] = (individual.getObjective(n) - (z_[n] + d1 * lambda[n]));
            double d2 = Utils.norm_vector(realB, problem_.getNumberOfObjectives());

            fitness = d1 + theta * d2;
        } else {
            System.out.println("MOEAD.fitnessFunction: unknown type "
                    + functionType_);
            System.exit(-1);
        }
        return fitness;
    } // fitnessEvaluation

    //Display the population in the objective space
    private void plotPopulation(int flag) {
        if (flag == 0) {
            // plot the population
            if (population_ != null && population_.size() > 0) {
                plot_.displayPop(population_);
            }
        }
        if (flag == 1) {
            // plot the population
            if (external_archive_ != null && external_archive_.size() > 0) {
                plot_.displayPop(external_archive_);
            }
        }
    }
}
