package opt.easyjmetal.util.sqlite;

import opt.easyjmetal.core.Solution;
import opt.easyjmetal.core.SolutionSet;
import opt.easyjmetal.core.Variable;
import opt.easyjmetal.encodings.solutiontype.variable.Real;
import opt.easyjmetal.util.JMException;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2015-06-09.
 */
public class SqlUtils {

    static String fileName_;

    public SqlUtils() {
    }

    public static void CreateTable(String tableName, String methodName) {
        fileName_ = methodName;
        Connection con;
        Statement stmt;
        try {
            Class.forName("org.sqlite.JDBC");
            con = DriverManager.getConnection("jdbc:sqlite:" + fileName_ + ".db");
            System.out.println("Opened database successfully");
            stmt = con.createStatement();
            String sql = "CREATE TABLE IF NOT EXISTS" + " " + tableName + " "
                    + "(OBJ   TEXT    NOT NULL, "
                    + "CON   TEXT    NOT NULL, "
                    + "VAR  TEXT NOT NULL)";
            stmt.executeUpdate(sql);
            stmt.close();
            con.close();

        } catch (Exception e) {
            System.err.println(e.getClass().getName() + ": " + e.getMessage());
            System.exit(0);
        }
        System.out.println("Table created successfully");

    }

    public static void CreateTable(String problemName, int objNumber, int decNumber, int ConNumber) {
        Connection con;
        Statement stmt;
        try {
            Class.forName("org.sqlite.JDBC");
            con = DriverManager.getConnection("jdbc:sqlite:" + fileName_ + ".db");
            System.out.println("Opened database successfully");

            stmt = con.createStatement();

            String createTable = "CREATE TABLE" + " " + problemName + " ";
            //String PrimaryKey  = "(ID INT PRIMARY KEY     NOT NULL,";
            String dataType = "    REAL    NOT NULL,";
            String conType = "    REAL    NOT NULL";
            String varType = "    REAL    NOT NULL";

            String objColumn = "";
            String conColumn = "";
            String varColumn = "";

            for (int i = 0; i < objNumber; i++) {
                String tempString = "OBJ" + Integer.toString(i) + dataType;
                objColumn += tempString;
            }


            for (int i = 0; i < ConNumber; i++) {
                String tempString = "CON" + Integer.toString(i) + conType;
                conColumn += tempString;
            }

            for (int i = 0; i < decNumber; i++) {
                String tempString = "VAR" + Integer.toString(i) + varType;
                varColumn += tempString;
            }

            String sql = createTable + "(" + objColumn + conColumn + varColumn + ")";
            stmt.executeUpdate(sql);
            stmt.close();
            con.close();
        } catch (Exception e) {
            System.err.println(e.getClass().getName() + ": " + e.getMessage());
            System.exit(0);
        }
        System.out.println("Table created successfully");
    }

    public static void InsertData(String TableName, double[] record) {

        Connection con;
        Statement stmt;
        try {
            Class.forName("org.sqlite.JDBC");
            con = DriverManager.getConnection("jdbc:sqlite:" + fileName_ + ".db");
            System.out.println("Opened database successfully");
            stmt = con.createStatement();
            String insertData = "(";
            for (int i = 0; i < record.length - 1; i++) {
                insertData += Double.toString(record[i]) + ",";
            }
            insertData += Double.toString(record[record.length - 1]) + ")";
            String sql = "INSERT INTO " + TableName + " VALUES  " + insertData;
            stmt.executeUpdate(sql);
            stmt.close();
            con.close();
        } catch (Exception e) {
            System.err.println(e.getClass().getName() + ": " + e.getMessage());
            System.exit(0);
        }

    }

    public static void InsertSolutionSet(String TableName, SolutionSet pop) throws JMException {
        int recordNumber = pop.size();

        Connection conn;
        PreparedStatement ps;
        try {
            Class.forName("org.sqlite.JDBC");
            conn = DriverManager.getConnection("jdbc:sqlite:" + fileName_ + ".db");
            String sql = "insert into" + " " + TableName + " " + " values (?,?,?)";

            conn.setAutoCommit(false);
            ps = conn.prepareStatement(sql);
            for (int i = 0; i < recordNumber; i++) {
                ps.setString(1, pop.get(i).toString());
                ps.setString(2, Double.toString(pop.get(i).getOverallConstraintViolation()));
                StringBuilder stringBuilder = new StringBuilder();
                Variable[] variables = pop.get(i).getDecisionVariables();
                for (int j = 0; j < variables.length; j++) {
                    stringBuilder.append(variables[j].getValue());
                    if(j < variables.length - 1){
                        stringBuilder.append(" ");
                    }
                }
                ps.setString(3, stringBuilder.toString());
                ps.addBatch();
            }
            ps.executeBatch();
            conn.commit();
            conn.close();
        } catch (Exception e) {
            System.err.println(e.getClass().getName() + ": " + e.getMessage());
            System.exit(0);
        }

    }

    public static void InsertData(String TableName, SolutionSet pop) throws JMException {

        int objNumber = pop.get(0).getNumberOfObjectives();
        int decNumber = pop.get(0).numberOfVariables();
        int conNumber = 1;

        double record[] = new double[objNumber + decNumber + conNumber];

        for (int i = 0; i < objNumber; i++) {
            record[i] = pop.get(0).getObjective(i);
        }

        Variable[] variables = pop.get(0).getDecisionVariables();
        for (int i = 0; i < decNumber; i++) {
            record[objNumber + i] = variables[i].getValue();
        }

        for (int i = 0; i < conNumber; i++) {
            record[objNumber + decNumber + i] = pop.get(0).getOverallConstraintViolation();
        }

        Connection con;
        Statement stmt;
        try {
            Class.forName("org.sqlite.JDBC");
            con = DriverManager.getConnection("jdbc:sqlite:" + fileName_ + ".db");
            System.out.println("Opened database successfully");
            stmt = con.createStatement();
            String insertData = "(";
            for (int i = 0; i < record.length - 1; i++) {
                insertData += Double.toString(record[i]) + ",";
            }
            insertData += Double.toString(record[record.length - 1]) + ")";
            String sql = "INSERT INTO " + TableName + " VALUES  " + insertData;
            stmt.executeUpdate(sql);
            stmt.close();
            con.close();
        } catch (Exception e) {
            System.err.println(e.getClass().getName() + ": " + e.getMessage());
            System.exit(0);
        }
    }

    /**
     * 从sqlite数据库中查询解
     *
     * @param DataBaseName
     * @param TableName
     * @return
     * @throws JMException
     */
    public static SolutionSet SelectData(String DataBaseName, String TableName) throws JMException {

        List<Solution> dataSet = new ArrayList<>();
        Connection con;
        Statement stmt;
        try {
            Class.forName("org.sqlite.JDBC");
            con = DriverManager.getConnection("jdbc:sqlite:" + DataBaseName + ".db");
            stmt = con.createStatement();
            String sql = "SELECT * from " + TableName;
            ResultSet resultSet = stmt.executeQuery(sql);

            // 判断是否读取结束
            while (resultSet.next()) {
                List<Double> line = new ArrayList<>();
                String objs = resultSet.getString("OBJ");
                String cons = resultSet.getString("CON");
                String vars = resultSet.getString("VAR");

                String[] r1 = objs.split(" ");// 目标值
                String[] r2 = cons.split(" ");// 约束
                String[] r3 = vars.split(" ");// 决策变量

                Solution solution = new Solution(r1.length, r2.length);

                for (int i = 0; i < r1.length; i++) {
                    //line.add(Double.parseDouble(r1[i]));
                    solution.setObjective(i, Double.parseDouble(r1[i]));
                }

                double overallConstraint = 0;
                for (int i = 0; i < r2.length; i++) {
                    // line.add(Double.parseDouble(r2[i]));
                    overallConstraint += Double.parseDouble(r2[i]);
                }
                solution.setOverallConstraintViolation(overallConstraint);

                // 目前仅仅支持实数类型的参数，如有需要，请自行修改
                Variable[] variables = new Variable[r3.length];
                for (int i = 0; i < r3.length; i++) {
                    //line.add(Double.parseDouble(r1[i]));
                    variables[i] = new Real();
                    variables[i].setValue(Double.parseDouble(r3[i]));
                }
                solution.setDecisionVariables(variables);

                dataSet.add(solution);
            }

            stmt.close();
            con.close();
        } catch (Exception e) {
            System.err.println(e.getClass().getName() + ": " + e.getMessage());
            System.exit(0);
        }

        SolutionSet solutionSet = new SolutionSet(dataSet.size());
        for (int i = 0; i < dataSet.size(); i++) {
            solutionSet.add(dataSet.get(i));
        }
        return solutionSet;
    }
}
