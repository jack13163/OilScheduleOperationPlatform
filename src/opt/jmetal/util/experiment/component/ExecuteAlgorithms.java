package opt.jmetal.util.experiment.component;

import opt.jmetal.solution.Solution;
import org.apache.commons.io.FileUtils;
import opt.jmetal.util.JMetalException;
import opt.jmetal.util.JMetalLogger;
import opt.jmetal.util.experiment.Experiment;
import opt.jmetal.util.experiment.ExperimentComponent;
import opt.jmetal.util.experiment.util.ExperimentAlgorithm;

import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * This class executes the algorithms the have been configured with a instance
 * of class {@link Experiment}.
 * <p>
 * Java 8 parallel streams are used to run the algorithms in parallel.
 * <p>
 * The result of the execution is a pair of files FUNrunId.tsv and VARrunID.tsv
 * per experiment, which are stored in the directory
 * {@link Experiment #getExperimentBaseDirectory()}/algorithmName/problemName.
 *
 * @author Antonio J. Nebro <antonio@lcc.uma.es>
 */
public class ExecuteAlgorithms<S extends Solution<?>, Result extends List<S>> implements ExperimentComponent {
    private Experiment<S, Result> experiment;

    /**
     * Constructor
     */
    public ExecuteAlgorithms(Experiment<S, Result> configuration) {
        this.experiment = configuration;
    }

    @Override
    public void run() {
        JMetalLogger.logger.info("ExecuteAlgorithms: Preparing output directory");

        // 清空上次实验的数据
        prepareOutputDirectory();

        // 设置运行的线程数
        if (experiment.getNumberOfCores() > 0) {
            System.setProperty("java.util.concurrent.ForkJoinPool.common.parallelism",
                    "" + experiment.getNumberOfCores());
            experiment.getAlgorithmList().parallelStream()
                    .forEach(algorithm -> algorithm.runAlgorithm(experiment.getExperimentBaseDirectory()));
        } else {
            for (ExperimentAlgorithm<S, Result> algorithm : experiment.getAlgorithmList()) {
                algorithm.runAlgorithm(experiment.getExperimentBaseDirectory());
            }
        }
    }

    /**
     * 清空数据
     */
    private void prepareOutputDirectory() {
        if (experimentDirectoryDoesNotExist()) {
            createExperimentDirectory();
        } else {
            // 清空Experiment或者SingleRun文件夹中所有的数据
            String dataPath = experiment.getExperimentBaseDirectory();
            File dataDir = new File(dataPath);
            try {
                FileUtils.deleteDirectory(dataDir);
                FileUtils.deleteQuietly(new File(ExperimentAlgorithm.TIMEFILE_PATH));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private boolean experimentDirectoryDoesNotExist() {
        boolean result;
        File experimentDirectory;

        experimentDirectory = new File(experiment.getExperimentBaseDirectory());
        if (experimentDirectory.exists() && experimentDirectory.isDirectory()) {
            result = false;
        } else {
            result = true;
        }

        return result;
    }

    private void createExperimentDirectory() {
        File experimentDirectory;
        experimentDirectory = new File(experiment.getExperimentBaseDirectory());

        if (experimentDirectory.exists()) {
            experimentDirectory.delete();
        }

        boolean result;
        result = new File(experiment.getExperimentBaseDirectory()).mkdirs();
        if (!result) {
            throw new JMetalException(
                    "Error creating experiment directory: " + experiment.getExperimentBaseDirectory());
        }
    }
}
