package opt.easyjmetal.util.fileinput;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

/**
 * created at 3:49 pm, 2019/1/29 the common util to read reference
 * vectors/reference points/uniform weight vectors from file
 *
 * <p>
 * Modified by Antonio J. Nebro on 8/03/2019
 *
 * @author sunhaoran
 */
public class VectorFileUtils {
    /**
     * @param filePath the file need to read
     * @return referenceVectors. referenceVectors[i][j] means the i-th vector's j-th
     * value
     */
    public static List<Vector> readVectors(String filePath) {
        List<Vector> referenceVectors;
        String path = filePath;

        List<String> vectorStrList = null;
        try {
            vectorStrList = Files.readAllLines(Paths.get(path));
        } catch (IOException e) {
            e.printStackTrace();
        }
        referenceVectors = new ArrayList<>();
        for (int i = 0; i < vectorStrList.size(); i++) {
            String vectorStr = vectorStrList.get(i);
            String[] objectArray = vectorStr.split("[\\s,]+");// 匹配空格或者逗号
            Vector vector = new Vector();
            for (int j = 0; j < objectArray.length; j++) {
                vector.add(Double.parseDouble(objectArray[j]));
            }
            referenceVectors.add(vector);
        }

        return referenceVectors;
    }

    /**
     * @param filePath the file need to read
     * @return referenceVectors. referenceVectors[i][j] means the i-th vector's j-th
     * value
     */
    public static double[][] readDoubleValues(String filePath) {
        double[][] referenceVectors;
        String path = filePath;

        List<String> vectorStrList = null;
        try {
            vectorStrList = Files.readAllLines(Paths.get(path));
        } catch (IOException e) {
            e.printStackTrace();
        }
        referenceVectors = new double[vectorStrList.size()][];
        for (int i = 0; i < vectorStrList.size(); i++) {
            String vectorStr = vectorStrList.get(i);
            String[] objectArray = vectorStr.split("[\\s,]+");// 匹配空格或者逗号
            referenceVectors[i] = new double[objectArray.length];
            for (int j = 0; j < objectArray.length; j++) {
                referenceVectors[i][j] = Double.parseDouble(objectArray[j]);
            }
        }

        return referenceVectors;
    }
}
