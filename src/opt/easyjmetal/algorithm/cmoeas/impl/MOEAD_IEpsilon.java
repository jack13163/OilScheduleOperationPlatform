//  Z. Fan, W. Li, X. Cai, H. Huang, Y. Fang, Y. You, J. Mo, C. Wei,
//  and E. D. Goodman, ¡°An improved epsilon constraint-handling method
//  in MOEA/D for cmops with large infeasible regions,¡± arXiv preprint
//  arXiv:1707.08767, 2017.

package opt.easyjmetal.algorithm.cmoeas.impl;

import opt.easyjmetal.util.MoeadUtils;
import opt.easyjmetal.core.*;
import opt.easyjmetal.util.JMException;
import opt.easyjmetal.util.PseudoRandom;
import opt.easyjmetal.util.jmathplot.ScatterPlot;
import opt.easyjmetal.util.sqlite.SqlUtils;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.StringTokenizer;
import java.util.Vector;

// This class implements a constrained version of the MOEAD algorithm based on
// the IEpsilon method.
public class MOEAD_IEpsilon extends Algorithm {

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
    //Vector<Vector<Double>> lambda_ ;
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
    private String weightDirectory_;
    private ScatterPlot plot_;
    private double epsilon_k_;
    private SolutionSet external_archive_;
    private double phi_max_ = -1e30;

    public MOEAD_IEpsilon(Problem problem) {
        super(problem);
        functionType_ = "_TCHE2";
    }

    @Override
    public SolutionSet execute() throws JMException, ClassNotFoundException {
        int runningTime;
        evaluations_ = 0;
        int maxEvaluations_ = (Integer) getInputParameter("maxEvaluations");
        populationSize_ = (Integer) getInputParameter("populationSize");
        dataDirectory_ = getInputParameter("dataDirectory").toString();
        weightDirectory_ = getInputParameter("weightDirectory").toString();
        boolean isDisplay_ = (Boolean) getInputParameter("isDisplay");
        int plotFlag_ = (Integer) getInputParameter("plotFlag");
        runningTime = (Integer) getInputParameter("runningTime") + 1; // start from 1
        population_ = new SolutionSet(populationSize_);
        T_ = (Integer) getInputParameter("T");
        nr_ = (Integer) this.getInputParameter("nr");
        double delta_ = (Double) getInputParameter("delta");
        neighborhood_ = new int[populationSize_][T_];
        String paratoFilePath_ = this.getInputParameter("paretoPath").toString();
        z_ = new double[problem_.getNumberOfObjectives()];
        lambda_ = new double[populationSize_][problem_.getNumberOfObjectives()];
        Operator crossover_ = operators_.get("crossover"); // default: DE crossover
        Operator mutation_ = operators_.get("mutation");  // default: polynomial mutation


        //creat database
        String dbName = dataDirectory_;
        String tableName = "MOEAD_Epsilon_" + runningTime;
        SqlUtils.CreateTable(tableName, dbName);

        // STEP 1. Initialization
        // STEP 1.1. Compute euclidean distances between weight vectors and find T
        initUniformWeight();

        initNeighborhood();

        // STEP 1.2. Initialize population

        initPopulation();

        // initialize external
        // Initialize the external archive
        external_archive_ = new SolutionSet(populationSize_);
        MoeadUtils.initializeExternalArchive(population_, populationSize_, external_archive_);


        // Initialize the epsilon_zero_
        double[] constraints = new double[populationSize_];
        for (int i = 0; i < populationSize_; i++) {
            constraints[i] = population_.get(i).getOverallConstraintViolation();
        }
        Arrays.sort(constraints); // each constraints is less or equal than zero;
        double epsilon_zero_ = Math.abs(constraints[(int) Math.ceil(0.05 * populationSize_)]);


        if (phi_max_ < Math.abs(constraints[0])) {
            phi_max_ = Math.abs(constraints[0]);
        }
        int tc_ = (int) (0.8 * maxEvaluations_ / populationSize_);
        double r_k_ = population_.GetFeasible_Ratio();
        double tao_ = 0.05;
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
        epsilon_k_ = epsilon_zero_;

        // STEP 2. Update
        do {
            // update the epsilon level
            if (gen >= tc_) {
                epsilon_k_ = 0;
            } else {
                if (r_k_ < 0.95) {
                    epsilon_k_ = (1 - tao_) * epsilon_k_;
                } else {
                    epsilon_k_ = phi_max_ * (1 + tao_);
                }
            }

            int[] permutation = new int[populationSize_];
            MoeadUtils.randomPermutation(permutation, populationSize_);

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

                //update phi_max_

                if (phi_max_ < Math.abs(child.getOverallConstraintViolation())) {
                    phi_max_ = Math.abs(child.getOverallConstraintViolation());
                }

                // STEP 2.3. Repair. Not necessary

                // STEP 2.4. Update z_
                updateReference(child);

                // STEP 2.5. Update of solutions
                updateProblem(child, n, type);
                //updateProblem_new(child, n, type);
            } // for

            r_k_ = population_.GetFeasible_Ratio();

            // update external archive
            MoeadUtils.updateExternalArchive(population_, populationSize_, external_archive_);

            // display populations
            if (isDisplay_) {
                plotPopulation(plotFlag_);
            }
            gen = gen + 1;

            allPop = allPop.union(population_);

        } while (evaluations_ < maxEvaluations_);

        // Update the external archive
        MoeadUtils.updateExternalArchive(population_, populationSize_, external_archive_);
        SqlUtils.InsertSolutionSet(dbName, tableName, external_archive_);
        return external_archive_;
    }

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
            dataFileName = "W" + problem_.getNumberOfObjectives() + "D_" + populationSize_ + ".dat";

            try {
                // Open the file
                String filepath = weightDirectory_ + dataFileName;
                FileInputStream fis = new FileInputStream(filepath);
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
                System.out.println("initUniformWeight: failed when reading for file: " + weightDirectory_ + dataFileName);
                e.printStackTrace();
            }
        }
    } // initUniformWeight

    private void initNeighborhood() {
        double[] x = new double[populationSize_];
        int[] idx = new int[populationSize_];

        for (int i = 0; i < populationSize_; i++) {
            // calculate the distances based on weight vectors
            for (int j = 0; j < populationSize_; j++) {
                x[j] = MoeadUtils.distVector(lambda_[i], lambda_[j]);
                idx[j] = j;
            } // for

            // find 'niche' nearest neighboring subproblems
            MoeadUtils.minFastSort(x, idx, populationSize_, T_);
            System.arraycopy(idx, 0, neighborhood_[i], 0, T_);
        } // for
    } // initNeighborhood

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

        MoeadUtils.randomPermutation(perm, size);

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

            con1 = Math.abs(population_.get(k).getOverallConstraintViolation());
            con2 = Math.abs(indiv.getOverallConstraintViolation());

            // use epsilon constraint method

            if (con1 <= epsilon_k_ && con2 <= epsilon_k_) {
                if (f2 < f1) {
                    population_.replace(k, new Solution(indiv));
                    time++;
                }
            } else if (con2 == con1) {
                if (f2 < f1) {
                    population_.replace(k, new Solution(indiv));
                    time++;
                }
            } else if (con2 < con1) {
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
            double nd = MoeadUtils.norm_vector(lambda, problem_.getNumberOfObjectives());
            for (int i = 0; i < problem_.getNumberOfObjectives(); i++)
                lambda[i] = lambda[i] / nd;

            double[] realA = new double[problem_.getNumberOfObjectives()];
            double[] realB = new double[problem_.getNumberOfObjectives()];

            // difference between current point and reference point
            for (int n = 0; n < problem_.getNumberOfObjectives(); n++)
                realA[n] = (individual.getObjective(n) - z_[n]);

            // distance along the line segment
            double d1 = Math.abs(MoeadUtils.innerproduct(realA, lambda));

            // distance to the line segment
            for (int n = 0; n < problem_.getNumberOfObjectives(); n++)
                realB[n] = (individual.getObjective(n) - (z_[n] + d1 * lambda[n]));
            double d2 = MoeadUtils.norm_vector(realB, problem_.getNumberOfObjectives());

            fitness = d1 + theta * d2;
        } else {
            System.out.println("MOEAD.fitnessFunction: unknown type "
                    + functionType_);
            System.exit(-1);
        }
        return fitness;
    } // fitnessEvaluation

}
