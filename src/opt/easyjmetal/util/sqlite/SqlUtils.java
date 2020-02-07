package opt.easyjmetal.util.sqlite;

import opt.easyjmetal.core.SolutionSet;
import opt.easyjmetal.core.Variable;
import opt.easyjmetal.util.JMException;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.Statement;

/**
 * Created by Administrator on 2015-06-09.
 */
public class SqlUtils {

    static String fileName_;

    public SqlUtils(){
    }

    public static void CreateTable(String tableName, String methodName){
        fileName_ = methodName;
        Connection con;
        Statement stmt;
        try {
            Class.forName("org.sqlite.JDBC");
            con = DriverManager.getConnection("jdbc:sqlite:"+fileName_+".db");
            System.out.println("Opened database successfully");
            stmt = con.createStatement();
            String sql = "CREATE TABLE IF NOT EXISTS" + " " + tableName + " " + "(OBJ   TEXT    NOT NULL, " + "CON  TEXT NOT NULL)";
//            String sql = "CREATE TABLE IF NOT EXISTS" + " " + tableName + " " + "(OBJ   TEXT    NOT NULL, " + " DEC TEXT  NOT NULL, " + "CON  TEXT NOT NULL)";
            stmt.executeUpdate(sql);
            stmt.close();
            con.close();

        } catch ( Exception e ) {
            System.err.println( e.getClass().getName() + ": " + e.getMessage() );
            System.exit(0);
        }
        System.out.println("Table created successfully");

    }

    public static void CreateTable(String problemName, int objNumber, int decNumber, int ConNumber){
        Connection con;
        Statement stmt;
        try {
            Class.forName("org.sqlite.JDBC");
            con = DriverManager.getConnection("jdbc:sqlite:"+fileName_+".db");
            System.out.println("Opened database successfully");

            stmt = con.createStatement();

            String createTable = "CREATE TABLE" + " " + problemName + " ";
            //String PrimaryKey  = "(ID INT PRIMARY KEY     NOT NULL,";
            String dataType    =  "    REAL    NOT NULL,";
            String conType    =  "    REAL    NOT NULL";

            String objColumn   = "";
            String conColumn   = "";

            for(int i = 0; i < objNumber; i ++){
                String tempString = "OBJ" + Integer.toString(i) + dataType;
                objColumn += tempString;
            }


            for(int i = 0; i < ConNumber; i ++){
                String tempString = "CON" + Integer.toString(i) + conType;
                conColumn += tempString;
            }

            String sql = createTable + "("+ objColumn  + conColumn + ")";
            stmt.executeUpdate(sql);
            stmt.close();
            con.close();
        } catch ( Exception e ) {
            System.err.println( e.getClass().getName() + ": " + e.getMessage() );
            System.exit(0);
        }
        System.out.println("Table created successfully");
    }

    public static void InsertData(String TableName, double[] record){

        Connection con;
        Statement stmt;
        try {
            Class.forName("org.sqlite.JDBC");
            con = DriverManager.getConnection("jdbc:sqlite:"+fileName_+".db");
            System.out.println("Opened database successfully");
            stmt = con.createStatement();
            String insertData = "(";
            for(int i = 0; i < record.length - 1; i++){
                insertData += Double.toString(record[i]) + ",";
            }
            insertData += Double.toString(record[record.length - 1]) + ")";
            String sql = "INSERT INTO "+TableName+" VALUES  " + insertData;
            stmt.executeUpdate(sql);
            stmt.close();
            con.close();
        } catch (Exception e){
            System.err.println( e.getClass().getName() + ": " + e.getMessage() );
            System.exit(0);
        }

    }

    public static void InsertSolutionSet(String TableName, SolutionSet pop) throws JMException {
        int recordNumber = pop.size();

        Connection conn;
        PreparedStatement ps;
        try {
            Class.forName("org.sqlite.JDBC");
            conn = DriverManager.getConnection("jdbc:sqlite:"+fileName_+".db");
            String sql = "insert into" + " " + TableName + " " + " values (?,?)";

            conn.setAutoCommit(false);
            ps = conn.prepareStatement(sql);
            for(int i = 0; i < recordNumber; i++){
                ps.setString(1,pop.get(i).toString());
//                ps.setString(2, Arrays.toString(pop.get(i).getDecisionVariables()));
//                ps.setString(3,Double.toString(pop.get(i).getOverallConstraintViolation()));
                ps.setString(2,Double.toString(pop.get(i).getOverallConstraintViolation()));
                ps.addBatch();
            }
            ps.executeBatch();
            conn.commit();
            conn.close();
        } catch (Exception e){
            System.err.println( e.getClass().getName() + ": " + e.getMessage() );
            System.exit(0);
        }

    }

    public static void InsertData(String TableName, SolutionSet pop) throws JMException{

        int objNumber = pop.get(0).getNumberOfObjectives();
        int decNumber = pop.get(0).numberOfVariables();
        int conNumber = 1;

        double record[] = new double[objNumber + decNumber + conNumber];

        for(int i = 0 ; i < objNumber; i++){
            record[i] = pop.get(0).getObjective(i);
        }

        Variable[] variables = pop.get(0).getDecisionVariables();
        for(int i = 0; i < decNumber; i++){
            record[objNumber + i] = variables[i].getValue();
        }

        for(int i = 0; i < conNumber; i++){
            record[objNumber + decNumber + i] = pop.get(0).getOverallConstraintViolation();
        }




        Connection con;
        Statement stmt;
        try {
            Class.forName("org.sqlite.JDBC");
            con = DriverManager.getConnection("jdbc:sqlite:"+fileName_+".db");
            System.out.println("Opened database successfully");
            stmt = con.createStatement();
            String insertData = "(";
            for(int i = 0; i < record.length - 1; i++){
                insertData += Double.toString(record[i]) + ",";
            }
            insertData += Double.toString(record[record.length - 1]) + ")";
            String sql = "INSERT INTO "+TableName+" VALUES  " + insertData;
            stmt.executeUpdate(sql);
            stmt.close();
            con.close();
        } catch (Exception e){
            System.err.println( e.getClass().getName() + ": " + e.getMessage() );
            System.exit(0);
        }

    }



}
