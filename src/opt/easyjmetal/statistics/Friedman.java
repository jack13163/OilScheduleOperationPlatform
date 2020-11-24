package opt.easyjmetal.statistics;

import java.io.*;
import java.util.Arrays;
import java.util.StringTokenizer;
import java.util.Vector;

public class Friedman {

    /**
     * Friedman检验
     * Rj表示第j个算法的平均排序
     * rij表示第j个算法（1≤j≤k ）在第i个数据集(1≤i≤N)上的排序。
     * Friedman test比较了算法的平均排序，
     * 即对每个算法进行排序：
     * 对最大化问题，排名越大越好
     * 对最小化问题，排名越小越好
     * <p>
     * https://blog.csdn.net/sinat_29874843/article/details/101756615
     *
     * @param indic 指标名称
     */
    public static void executeTest(String indic,
                                   String[] algorithmNameList_,
                                   String[] problemList_,
                                   String dirPath_) {
        Vector algoritmos;
        Vector datasets;
        Vector datos;

        double mean[][];
        Pareja orden[][];
        Pareja rank[][];

        boolean visto[];
        Vector porVisitar;
        double Rj[];

        double friedman;
        double sumatoria = 0;
        double termino1, termino2;

        // 文件输出路径
        String outDir = dirPath_ + "/friedman/";
        String outFile = dirPath_ + "FriedmanTest_" + indic + ".tex";

        StringBuilder Output = new StringBuilder();
        Output.append("\\documentclass{article}\n" +
                "\\usepackage{graphicx}\n" +
                "\\title{" + indic + "}\n" +
                "\\author{}\n" +
                "\\date{\\today}\n" +
                "\\begin{document}\n" +
                "\\oddsidemargin 0in \\topmargin 0in" +
                "\\maketitle\n" +
                "\\section{Tables of Friedman Tests}");

        algoritmos = new Vector();
        datasets = new Vector();
        datos = new Vector();

        for (int alg = 0; alg < algorithmNameList_.length; alg++) {
            algoritmos.add(algorithmNameList_[alg]);
            datos.add(new Vector());

            for (int prob = 0; prob < problemList_.length; prob++) {
                if (alg == 0) {
                    datasets.add(problemList_[prob]);
                }

                // 指标文件所在的路径
                String indicatorFilePath = dirPath_ + "/indicator/" + problemList_[prob] + "/"  + algorithmNameList_[alg] + "." + indic;

                //Leemos el fichero
                String cadena = "";

                try {
                    FileInputStream fis = new FileInputStream(indicatorFilePath);

                    byte[] leido = new byte[4096];
                    int bytesLeidos = 0;

                    while (bytesLeidos != -1) {
                        bytesLeidos = fis.read(leido);

                        if (bytesLeidos != -1) {
                            cadena += new String(leido, 0, bytesLeidos);
                        }
                    }

                    fis.close();
                } catch (IOException e) {
                    e.printStackTrace();
                    System.exit(-1);
                }

                StringTokenizer lineas = new StringTokenizer(cadena, "\n\r");

                double valor = 0.0;
                int n = 0;

                while (lineas.hasMoreTokens()) {
                    String linea = lineas.nextToken();
                    valor = valor + Double.parseDouble(linea);
                    n++;
                }
                if (n != 0) {
                    ((Vector) datos.elementAt(alg)).add(new Double(valor / n));
                } else {
                    ((Vector) datos.elementAt(alg)).add(new Double(valor));
                }
            }
        }


        /*Compute the average performance per algorithm for each data set*/
        mean = new double[datasets.size()][algoritmos.size()];

        for (int j = 0; j < algoritmos.size(); j++) {
            for (int i = 0; i < datasets.size(); i++) {
                mean[i][j] = ((Double) ((Vector) datos.elementAt(j)).elementAt(i)).doubleValue();
            }
        }


        /*We use the pareja structure to compute and order rankings*/
        orden = new Pareja[datasets.size()][algoritmos.size()];
        for (int i = 0; i < datasets.size(); i++) {
            for (int j = 0; j < algoritmos.size(); j++) {
                orden[i][j] = new Pareja(j, mean[i][j]);
            }
            Arrays.sort(orden[i]);
        }

        /*building of the rankings table per algorithms and data sets*/
        rank = new Pareja[datasets.size()][algoritmos.size()];
        int posicion = 0;
        for (int i = 0; i < datasets.size(); i++) {
            for (int j = 0; j < algoritmos.size(); j++) {
                boolean encontrado = false;
                for (int k = 0; k < algoritmos.size() && !encontrado; k++) {
                    if (orden[i][k].indice == j) {
                        encontrado = true;
                        posicion = k + 1;
                    }
                }
                rank[i][j] = new Pareja(posicion, orden[i][posicion - 1].valor);
            }
        }

        /*In the case of having the same performance, the rankings are equal*/
        for (int i = 0; i < datasets.size(); i++) {
            visto = new boolean[algoritmos.size()];
            porVisitar = new Vector();

            Arrays.fill(visto, false);
            for (int j = 0; j < algoritmos.size(); j++) {
                porVisitar.removeAllElements();
                double sum = rank[i][j].indice;
                visto[j] = true;
                int ig = 1;
                for (int k = j + 1; k < algoritmos.size(); k++) {
                    if (rank[i][j].valor == rank[i][k].valor && !visto[k]) {
                        sum += rank[i][k].indice;
                        ig++;
                        porVisitar.add(new Integer(k));
                        visto[k] = true;
                    }
                }
                sum /= (double) ig;
                rank[i][j].indice = sum;
                for (int k = 0; k < porVisitar.size(); k++) {
                    rank[i][((Integer) porVisitar.elementAt(k)).intValue()].indice = sum;
                }
            }
        }

        /*compute the average ranking for each algorithm*/
        Rj = new double[algoritmos.size()];
        for (int i = 0; i < algoritmos.size(); i++) {
            Rj[i] = 0;
            for (int j = 0; j < datasets.size(); j++) {
                Rj[i] += rank[j][i].indice / ((double) datasets.size());
            }
        }

        /*Print the average ranking per algorithm*/
        Output.append("\n");
        Output.append("\\begin{table}[!htp]\n" +
                "\\centering\n" +
                "\\caption{Average Rankings of the algorithms\n}" +// for "+ exp_.problemList_[prob] +" problem\n}" +
                "\\begin{tabular}{c|c}\n" +
                "Algorithm&Ranking\\\\\n\\hline");

        for (int i = 0; i < algoritmos.size(); i++) {
            Output.append("\n" + algoritmos.elementAt(i) + "&" + Rj[i] + "\\\\");
        }

        Output.append("\n" +
                "\\end{tabular}\n" +
                "\\end{table}");

        /*Compute the Friedman statistic*/
        termino1 = (12 * (double) datasets.size()) / ((double) algoritmos.size() * ((double) algoritmos.size() + 1));
        termino2 = (double) algoritmos.size() * ((double) algoritmos.size() + 1) * ((double) algoritmos.size() + 1) / (4.0);
        for (int i = 0; i < algoritmos.size(); i++) {
            sumatoria += Rj[i] * Rj[i];
        }
        friedman = (sumatoria - termino2) * termino1;

        Output.append("\n" + "\n\nFriedman statistic considering reduction performance (distributed according to chi-square with "
                + (algoritmos.size() - 1) + " degrees of freedom: " + String.format("%.4f", friedman) + ").\n\n");

        Output.append("\n");
        Output.append("\\end{document}");
        try {
            File latexOutput;
            latexOutput = new File(outDir);
            if (!latexOutput.exists()) {
                latexOutput.mkdirs();
            }
            FileOutputStream f = new FileOutputStream(outFile);
            DataOutputStream fis = new DataOutputStream(f);

            fis.writeBytes(Output.toString().replace("_", "\\_"));

            fis.close();
            f.close();
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(-1);
        }
    }
}
