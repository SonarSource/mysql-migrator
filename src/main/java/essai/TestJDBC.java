package essai;

import  java.sql.*;
import java.lang.*;
import java.util.Vector;
/**
 * Created with IntelliJ IDEA.
 * User: stephen.broyer
 * Date: 11/13/13
 * Time: 12:51 PM
 * To change this template use File | Settings | File Templates.
 *
 * To verify the size of database !!
 * SELECT pg_database_size('sonar'),pg_size_pretty(pg_database_size('sonar'));
 *
 *
 *   To select  table, view, index, sequence,  special
 *   http://golden13.blogspot.fr/2012/08/how-to-get-some-information-about_7.html
 SELECT 	n.nspname as "SonarBDD",
 c.relname AS datname,
 CASE c.relkind
 WHEN 'r' THEN 'table'
 WHEN 'v' THEN 'view'
 WHEN 'i' THEN 'index'
 WHEN 'S' THEN 'sequence'
 WHEN 's' THEN 'special'
 END as "Type",
 u.usename as "Owner",
 (SELECT obj_description(c.oid, 'pg_class')) AS comment
 FROM pg_catalog.pg_class c
 LEFT JOIN pg_catalog.pg_user u ON u.usesysid = c.relowner
 LEFT JOIN pg_catalog.pg_namespace n ON n.oid = c.relnamespace
 WHERE n.nspname='public' AND c.relkind IN ('r','i','S')
 AND n.nspname NOT IN ('pg_catalog', 'pg_toast', 'information_schema')
 ORDER BY 3,2 ASC
 */
public class TestJDBC {




    public static void main(String[] args){

        ResultSet rsData = null;
        ResultSet rsTable = null;
        ResultSet rsColumn = null;
        Statement statement = null;
        Connection connection = null;

        Statement newStatement = null;
        Connection createConnection = null;

        PostgreConnection postgreConn;

        Vector<String> listOfTable = new Vector<String>();
        String completeListInBase[][][] = new String[54][50][6];



        try {
//**********// connection à la base à copier
            postgreConn = new PostgreConnection();
            connection = postgreConn.doPostgreConnection();
            statement = connection.createStatement();

//**********// récupération des tables  de la base à copier
            rsTable = statement.executeQuery("SELECT table_name,count(column_name) FROM information_schema.columns WHERE table_schema='public' GROUP BY table_name ORDER BY 1");
            while (rsTable.next()) {
                String aTableName = rsTable.getString(1);
                listOfTable.add(aTableName);
            }

//**********// récupération des colonnes de chaque table  de la base à copier
            for(int rankOfTable=0 ; rankOfTable<listOfTable.size() ; rankOfTable++){
                    rsColumn = statement.executeQuery("SELECT column_name,data_type,character_maximum_length,is_nullable from information_schema.columns where table_name='"+listOfTable.get(rankOfTable)+"'");

                    int numColOnTable=0;
                    while(rsColumn.next()){
                       completeListInBase[rankOfTable][numColOnTable][0]=listOfTable.get(rankOfTable);
                       completeListInBase[rankOfTable][numColOnTable][1]=rsColumn.getString(1);
                       completeListInBase[rankOfTable][numColOnTable][2]=rsColumn.getString(2);
                       completeListInBase[rankOfTable][numColOnTable][3]=rsColumn.getString(3);
                       completeListInBase[rankOfTable][numColOnTable][4]=rsColumn.getString(4);
                       numColOnTable++;
                    }

            }

//**********// créer table et colonnes dans une nouvelle base
            createConnection = postgreConn.doAnotherConnectionToWrite();
            newStatement = createConnection.createStatement();

            // pour chaque table
            for(int rankOfTable=0 ; rankOfTable<listOfTable.size() ; rankOfTable++){
                newStatement.execute("DROP TABLE IF EXISTS "+completeListInBase[rankOfTable][0][0]);
                newStatement.execute("CREATE TABLE IF NOT EXISTS "+completeListInBase[rankOfTable][0][0]+"();");
                // pour chaque colonne de chaque table
                int rankOfColumn=0;
                while(completeListInBase[rankOfTable][rankOfColumn][1]!=null){
                    String mySQLString ="ALTER TABLE "+completeListInBase[rankOfTable][rankOfColumn][0]+
                                        " ADD "+completeListInBase[rankOfTable][rankOfColumn][1]+
                                        " "+completeListInBase[rankOfTable][rankOfColumn][2];

                                        if (completeListInBase[rankOfTable][rankOfColumn][3]!=null){
                                            mySQLString+=" ("+completeListInBase[rankOfTable][rankOfColumn][3]+") ";
                                        }
                                        if (completeListInBase[rankOfTable][rankOfColumn][4].equals("NO")){
                                            mySQLString+=" NOT NULL";
                                        }

                                        mySQLString+=" ;";

                    newStatement.execute(mySQLString);
                    rankOfColumn++;
                }
            }

//**********// récupération des data de chaque table  et réécriture instantannée dans la nouvelle base
            for(int rankOfTable=0 ; rankOfTable<listOfTable.size() ; rankOfTable++){
                rsData = statement.executeQuery("SELECT * FROM active_dashboards");
                int numColOnTable=0;
                while(rsData.next()){

                }
            }




        }
        catch(SQLException se){
                System.err.println(se);
                System.exit(0);
        }
        catch (ClassNotFoundException cnfe){
            System.err.println(cnfe);
            System.exit(0);
        }
//**********// fermeture de la connexion
        finally {
            try {
                rsData.close();
                rsTable.close();
                rsColumn.close();
                statement.close();
                connection.close();

                newStatement.close();
                createConnection.close();
            }catch(SQLException e) {
                System.err.println("SQLException while closing connection. "+ e.getMessage());
            }
        }

    }
}


