package opt.easyjmetal.util.fileinput;

import opt.easyjmetal.core.SolutionSet;

import java.io.*;
import java.util.StringTokenizer;
import java.util.Vector;

public class FileUtils {

    static public void appendObjectToFile(String fileName, Object object) {
        FileOutputStream fos;
        try {
            fos = new FileOutputStream(fileName, true);
            OutputStreamWriter osw = new OutputStreamWriter(fos);
            BufferedWriter bw = new BufferedWriter(osw);

            bw.write(object.toString());
            bw.newLine();
            bw.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    static public void createEmtpyFile(String fileName) {
        FileOutputStream fos;
        try {
            fos = new FileOutputStream(fileName, false);
            OutputStreamWriter osw = new OutputStreamWriter(fos);
            BufferedWriter bw = new BufferedWriter(osw);

            bw.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static boolean deleteFile(String delpath) throws IOException {
        try {
            File file = new File(delpath);
            if (!file.isDirectory()) {
                file.delete();
            } else if (file.isDirectory()) {
                String[] filelist = file.list();
                for (int i = 0; i < filelist.length; i++) {
                    File delfile = new File(delpath + "/" + filelist[i]);
                    if (!delfile.isDirectory()) {
                        delfile.delete();
                    } else if (delfile.isDirectory()) {
                        deleteFile(delpath + "/" + filelist[i]);
                    }
                }
                file.delete();
            }

        } catch (FileNotFoundException e) {
            System.out.println("deletefile() Exception:" + e.getMessage());
        }
        return true;
    }

    public static double[][] readDataFromFile(String fileName) {

        Vector<Double[]> vectors = new Vector<Double[]>(10000);
        int numberofColumn = 0;
        try {
            // Open the file
            FileInputStream fis = new FileInputStream(fileName);
            InputStreamReader isr = new InputStreamReader(fis);
            BufferedReader br = new BufferedReader(isr);
            String aux = br.readLine();
            while (aux != null) {
                StringTokenizer st = new StringTokenizer(aux);
                numberofColumn = st.countTokens();
                Double[] rowInfo = new Double[numberofColumn];
                int i = 0;
                while (st.hasMoreTokens()) {
                    double value = (new Double(st.nextToken())).doubleValue();
                    rowInfo[i] = value;
                    i++;
                }
                vectors.add(rowInfo);
                aux = br.readLine();
            }
            br.close();
        } catch (Exception e) {
            System.out.println("readConstraintInfo: failed when reading for file: " + fileName);
            e.printStackTrace();
        }

        double[][] datas = new double[numberofColumn][vectors.size()];
        for (int i = 0; i < vectors.size(); i++) {
            for (int j = 0; j < numberofColumn; j++) {
                datas[j][i] = vectors.get(i)[j];
            }

        }
        return datas;
    }

    public static double[][] readFromFile(String fileName) {

        Vector<Double[]> vectors = new Vector<Double[]>(10000);
        int numberofColumn = 0;
        try {
            // Open the file
            FileInputStream fis = new FileInputStream(fileName);
            InputStreamReader isr = new InputStreamReader(fis);
            BufferedReader br = new BufferedReader(isr);
            String aux = br.readLine();
            while (aux != null) {
                StringTokenizer st = new StringTokenizer(aux);
                numberofColumn = st.countTokens();
                Double[] rowInfo = new Double[numberofColumn];
                int i = 0;
                while (st.hasMoreTokens()) {
                    double value = (new Double(st.nextToken())).doubleValue();
                    rowInfo[i] = value;
                    i++;
                }
                vectors.add(rowInfo);
                aux = br.readLine();
            }
            br.close();
        } catch (Exception e) {
            System.out.println("readConstraintInfo: failed when reading for file: " + fileName);
            e.printStackTrace();
        }

        double[][] datas = new double[vectors.size()][numberofColumn];
        for (int i = 0; i < vectors.size(); i++) {
            for (int j = 0; j < numberofColumn; j++) {
                datas[i][j] = vectors.get(i)[j];
            }

        }
        return datas;
    }

    public static boolean outputResult2File(String fileName, SolutionSet pop) {
        createEmtpyFile(fileName);
        int recordNo = pop.size();

        FileOutputStream fos;
        try {
            fos = new FileOutputStream(fileName, true);
            OutputStreamWriter osw = new OutputStreamWriter(fos);
            BufferedWriter bw = new BufferedWriter(osw);

            for (int i = 0; i < recordNo; i++) {
                String obj = pop.get(i).toString();
                String cv  = Double.toString(pop.get(i).getOverallConstraintViolation());
                bw.write(obj.concat(cv));
                bw.newLine();
            }
            bw.close();
            return true;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

}
