// An Evolutionary Many-Objective Optimization Algorithm Using Reference-point
// Based Non-dominated Sorting Approach,
// Part I: Solving Problems with Box Constraints.
package opt.easyjmetal.algorithm.cmoeas.nsgaiii_cdp;

import opt.easyjmetal.algorithm.cmoeas.util.Utils;
import opt.easyjmetal.core.*;
import opt.easyjmetal.util.Distance;
import opt.easyjmetal.util.JMException;
import opt.easyjmetal.util.Ranking;
import opt.easyjmetal.util.sqlite.SqlUtils;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;
import java.util.Vector;

public class NSGAIII_CDP extends Algorithm {
    /**
     * Constructor
     *
     * @param problem Problem to solve
     */
    public NSGAIII_CDP(Problem problem) {
        super(problem);
    } // NSGAIII

    private SolutionSet population_;
    private SolutionSet external_archive_;
    private int populationSize_;
    private int maxEvaluations_;
    private String dataDirectory_;

    Distance distance;

    /**
     * 参考点
     */
    //Vector<Vector<Double>> lambda_ ;
    private double[][] lambda_;
    protected List<ReferencePoint> referencePoints = new Vector<>();

    /**
     * Runs the NSGA-II algorithm.
     *
     * @return a <code>SolutionSet</code> that is a set of non dominated solutions
     * as a result of the algorithm execution
     * @throws JMException
     */
    public SolutionSet execute() throws JMException, ClassNotFoundException {

        distance = new Distance();// 计算距离
        int runningTime = (Integer) getInputParameter("runningTime") + 1;

        //Read the parameters
        populationSize_ = (Integer) getInputParameter("populationSize");
        maxEvaluations_ = (Integer) getInputParameter("maxEvaluations");
        String methodName_ = getInputParameter("AlgorithmName").toString();
        dataDirectory_ = getInputParameter("dataDirectory").toString();
        lambda_ = new double[populationSize_][problem_.getNumberOfObjectives()];

        //Initialize the variables
        population_ = new SolutionSet(populationSize_);
        int evaluations_ = 0;

        //Read the operators
        Operator mutationOperator_ = operators_.get("mutation");
        Operator crossoverOperator_ = operators_.get("crossover");
        Operator selectionOperator_ = operators_.get("selection");


        // STEP 1. Initialization
        // STEP 1.1. Compute euclidean distances between weight vectors and find T
        initUniformWeight();

        // Create the initial solutionSet
        Solution newSolution;
        for (int i = 0; i < populationSize_; i++) {
            newSolution = new Solution(problem_);
            problem_.evaluate(newSolution);
            problem_.evaluateConstraints(newSolution);
            evaluations_++;
            population_.add(newSolution);
        } //for

        SolutionSet allPop = population_;

        // Initialize the external archive
        external_archive_ = new SolutionSet(populationSize_);
        Utils.initializeExternalArchive(population_, populationSize_, external_archive_);

        //creat database
        String problemName = problem_.getName() + "_" + Integer.toString(runningTime);
        SqlUtils.CreateTable(problemName, methodName_);


        int gen = 0;
        // Generations
        while (evaluations_ < maxEvaluations_) {

            // Create the offSpring solutionSet
            SolutionSet offspringPopulation_ = new SolutionSet(populationSize_);
            for (int i = 0; i < (populationSize_ / 2); i++) {
                //obtain parents
                Solution[] offSpring = new Solution[2];
                // Apply Crossover for Real codification
                if (crossoverOperator_.getClass().getSimpleName().equalsIgnoreCase("SBXCrossover")) {
                    Solution[] parents = new Solution[2];
                    parents[0] = (Solution) selectionOperator_.execute(population_);
                    parents[1] = (Solution) selectionOperator_.execute(population_);
                    offSpring = ((Solution[]) crossoverOperator_.execute(parents));
                }
                // Apply DE crossover
                else if (crossoverOperator_.getClass().getSimpleName().equalsIgnoreCase("DifferentialEvolutionCrossover")) {
                    Solution[] parents = new Solution[3];
                    parents[0] = (Solution) selectionOperator_.execute(population_);
                    parents[1] = (Solution) selectionOperator_.execute(population_);
                    parents[2] = parents[0];
                    offSpring[0] = (Solution) crossoverOperator_.execute(new Object[]{parents[0], parents});
                    offSpring[1] = (Solution) crossoverOperator_.execute(new Object[]{parents[1], parents});
                } else {
                    System.out.println("unknown crossover");

                }
                mutationOperator_.execute(offSpring[0]);
                mutationOperator_.execute(offSpring[1]);
                problem_.evaluate(offSpring[0]);
                problem_.evaluateConstraints(offSpring[0]);
                problem_.evaluate(offSpring[1]);
                problem_.evaluateConstraints(offSpring[1]);
                offspringPopulation_.add(offSpring[0]);
                offspringPopulation_.add(offSpring[1]);
                evaluations_ += 2;
            } // for

            // 环境选择
            population_ = replacement(population_, offspringPopulation_);

            Utils.updateExternalArchive(population_, populationSize_, external_archive_);

            if (gen % 50 == 0) {
                allPop = allPop.union(population_);
            }
            gen++;
        } // while

        SqlUtils.InsertSolutionSet(problemName, external_archive_);

        return external_archive_;
    } // execute


    // Generate the reference points and random population
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
                String filepath = dataDirectory_ + "/" + dataFileName;
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
                System.out.println("initUniformWeight: failed when reading for file: " + dataDirectory_ + "/" + dataFileName);
                e.printStackTrace();
            }
        }
    } // initUniformWeight

    // 环境选择
    protected SolutionSet replacement(SolutionSet population, SolutionSet offspringPopulation) throws JMException {

        // Create the solutionSet union of solutionSet and offSpring
        SolutionSet jointPopulation = population_.union(offspringPopulation);

        // Ranking the union
        Ranking ranking = new Ranking(jointPopulation);

        // List<Solution> pop = crowdingDistanceSelection(ranking);
        SolutionSet pop = new SolutionSet(populationSize_);
        List<SolutionSet> fronts = new ArrayList<>();
        int rankingIndex = 0;
        int candidateSolutions = 0;
        while (candidateSolutions < populationSize_) {

            SolutionSet solutions = ranking.getSubfront(rankingIndex);
            fronts.add(solutions);

            candidateSolutions += ranking.getSubfront(rankingIndex).size();
            if ((pop.size() + ranking.getSubfront(rankingIndex).size()) <= populationSize_) {

                for (int i = 0; i < solutions.size(); i++) {
                    pop.add(solutions.get(i));
                }
            }
            rankingIndex++;
        }

        // Environmental selection
        // A copy of the reference list should be used as parameter of the environmental selection
        EnvironmentalSelection selection =
                new EnvironmentalSelection(fronts, populationSize_, getReferencePointsCopy(), problem_.getNumberOfObjectives());

        pop = selection.execute(pop);

        return pop;
    }


    private List<ReferencePoint> getReferencePointsCopy() {
        List<ReferencePoint> copy = new ArrayList<>();
        for (int i = 0; i < lambda_.length; i++) {
            copy.add(new ReferencePoint(lambda_[i]));
        }
        return copy;
    }
} // NSGA-III

