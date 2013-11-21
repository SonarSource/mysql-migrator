package diagramme;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: stephen.broyer
 * Date: 11/21/13
 * Time: 12:03 PM
 * To change this template use File | Settings | File Templates.
 */
public class DataRecorder {

    public DataRecorder(){}

    public void recordData(ResultSet resultSet, SonarTable sonarTable) throws SQLException {
System.out.println("### DEBUG ### in DataRecorder : recordData");
            /* GET THE TABLE TO SET AND ITS NAME */
            SonarTable sonarTableToSet = sonarTable;
            String sonarTableName = sonarTableToSet.tableName;

            /* GET THE RESUTLSET  CORRESPONDING TO THIS TABLE */
            ResultSet resultSetToGet =  resultSet;

            /* GET THE METADATA OF THIS TABLE */
            ResultSetMetaData resultSetMetaData = resultSetToGet.getMetaData();
            int columnNb = resultSetMetaData.getColumnCount();
System.out.println("### DEBUG ### in DataRecorder : metadata");
            /* ADD THE COLUMN NAME OF THE TABLE TO SET  */
            for(int j=1;j<=columnNb;j++){
System.out.println("### DEBUG ### in DataRecorder : 1st fot loop ");
                try{
                    String columnName = resultSetMetaData.getColumnName(j);

                SonarColumn sonarColumnToSet = sonarTableToSet.addOneColumnToTable(columnName,sonarTableName);
System.out.println("### DEBUG ### in DataRecorder : after addOneCol... ");

                while (resultSetToGet.next()) {
System.out.println("### DEBUG ### in DataRecorder : into the while ");

                    sonarColumnToSet.addDataObjectInTable(resultSetToGet.getObject(j));
                }
                resultSetToGet.beforeFirst();

            /* DEBUG */
                System.out.println("***"+sonarTableToSet.tableName+"***");

               for(int k=0;k<columnNb;k++){
                   System.out.println("--------------");
                   System.out.println(sonarTableToSet.columns.get(k).columnName);

                   for(int m=0;m<sonarTableToSet.columns.size();m++){
                       System.out.print(sonarTableToSet.columns.get(k).getData(m));
                   }
               }
            }catch (Exception e){System.out.print(e);}
        }
    }
}
     /*
    ResultSet resultSetSource = statementSource.executeQuery("SELECT * FROM " + myBddSource.tables_of_bdd.get(i).getTableName());

     /*
    for(int i=0;i<myBddSource.tables_of_bdd.size();i++){

        ResultSet resultSetSource = statementSource.executeQuery("SELECT * FROM " + myBddSource.tables_of_bdd.get(i).getTableName());
        ResultSetMetaData rsmdSource = resultSetSource.getMetaData();
        int columnNb = rsmdSource.getColumnCount();
        for(int j=1; j<=columnNb;j++){
            String columnName = rsmdSource.getColumnName(j);
            String typeOfColumn = rsmdSource.getColumnTypeName(j);


        }


        //List<Object> data;

        while (resultSetSource.next()) {

            for(int j=1; j<=columnNb;j++){
                String columnName = rsmdSource.getColumnName(j);
                //String typeOfColumn = rsmdSource.getColumnTypeName(j);
                data.add (resultSetSource.getObject(j));
                //System.out.print(data+" - "); //debug
            }
            resultSetSource.beforeFirst();



            ResultSet resultSetDestination = statementDestination.executeQuery("INSERT INTO " + myBddSource.tables_of_bdd.get(i).getTableName()+" VALUES(XXXXXXXX)");

        }

        resultSetSource.close();
    }
}    */

