package diagramme;

import javax.swing.text.html.parser.DTD;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: stephen.broyer
 * Date: 11/21/13
 * Time: 12:03 PM
 * To change this template use File | Settings | File Templates.
 */
public class DataGetter {

    //protected DataRecorder dataRecorder;
    public ResultSet resultSet;
    public SonarColumn sonarColumnToSet;
    public DataGetter(){
        //dataRecorder = new DataRecorder();
        System.out.println("### DEBUG ### in DataGetter");

    }

    public void doRequest(Statement statement, SonarBDD sonarBDD) throws SQLException {
                                                                                                System.out.println("### DEBUG ### in DataGetter : doRequest");

        List <SonarTable> tables_of_bdd = sonarBDD.tables_of_bdd;

                                                                                                System.out.println("### DEBUG ### in DataGetter : doRequest after table_of_bdd");
        for(int tableIndex=0;tableIndex<tables_of_bdd.size();tableIndex++){
            SonarTable sonarTable= tables_of_bdd.get(tableIndex);
            String sonarTableName = sonarTable.tableName;
            System.out.println("### DEBUG ### in DataGetter : recordData  TABLE= "+sonarTableName);

            resultSet =  statement.executeQuery("SELECT * FROM " + sonarTable.getTableName());
                                                                                               System.out.println("### DEBUG ### in DataGetter : recordData");

            /* GET THE METADATA OF THIS TABLE */
            ResultSetMetaData resultSetMetaData = resultSet.getMetaData();
            int columnNb = resultSetMetaData.getColumnCount();
                                                                                                System.out.println("### DEBUG ### in DataGetter : metadata");
            /* ADD THE COLUMN NAME OF THE TABLE TO SET  */
            for(int columnIndex=1;columnIndex<=columnNb;columnIndex++){
                                                                                                System.out.println("### DEBUG ### in DataGetter : loop for column creations ");
                String columnName = resultSetMetaData.getColumnName(columnIndex);
                sonarColumnToSet = sonarTable.addOneColumnToTable(columnName,sonarTableName);
                                                                                                System.out.println("### DEBUG ### in DataGetter : after addOneCol...NAME= "+sonarColumnToSet.getName());
            }

            while (resultSet.next()) {
                                                                                                System.out.println("### DEBUG ### in DataGetter : into the while BEGINNING");
                    Object objectGetted =  resultSet.getObject(1);
                    System.out.println("### DEBUG ### in DataGetter : into the while : "+objectGetted.toString());
                    sonarColumnToSet.addDataObjectInTable(objectGetted);
                    System.out.println("### DEBUG ### in DataGetter : into the while END");

            }
            resultSet.beforeFirst();

        }

        new DebugTableContent(sonarBDD);
    }
}
