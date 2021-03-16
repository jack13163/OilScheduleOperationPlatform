package opt.easyjmetal.algorithm.common;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.StringTokenizer;

public class UtilityFunctions {


    /**
     * 读取指定路径下的权重文件，生成参考点
     * @param weightDirectory_  权重文件的路径
     * @param lambda_
     */
    public static void initUniformWeight(String weightDirectory_, double[][] lambda_) {
        int populationSize_ = lambda_.length;
        int numberOfObjectives = lambda_[0].length;
        if ((numberOfObjectives == 2) && (populationSize_ <= 300)) {
            for (int n = 0; n < populationSize_; n++) {
                double a = 1.0 * n / (populationSize_ - 1);
                lambda_[n][0] = a;
                lambda_[n][1] = 1 - a;
            }
        } else {
            String dataFileName;
            dataFileName = "W" + numberOfObjectives + "D_" + populationSize_ + ".dat";

            try {
                // 读取权重文件
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
    }
}
