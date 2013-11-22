package diagramme;

import java.sql.Statement;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: stephen.broyer
 * Date: 11/21/13
 * Time: 12:06 PM
 * To change this template use File | Settings | File Templates.
 */
public class DataPutInBase {

    private SonarBDD sonarBDD;
    private Statement statement;
    private List<SonarTable> listOfTables;

    public DataPutInBase(Statement statement, SonarBDD sonarBDD){
        this.statement = statement;
        this.sonarBDD = sonarBDD;
        this.listOfTables = sonarBDD.getBDDTables();
    }

    public void doInsertInto (){


        for(int indexTable=0;indexTable<listOfTables.size();indexTable++){

            SonarTable sonarTable = listOfTables.get(indexTable);
            String tableName =  sonarTable.getTableName();
            int nbRowsInTable = sonarTable.getNbRows();

            DataRowJavaSonarTable dataRow = new DataRowJavaSonarTable(sonarTable);
            //String sqlColumnsString = dataRow.getColumnsString();


            for(int indexRow=0;indexRow<nbRowsInTable;indexRow++){

                //String sqlValuesString = dataRow.getValuesString(indexRow);

                //statement.execute("INSERT INTO "+tableName+" ("+sqlColumnsString+") VALUES ("+sqlValuesString+");");
            }
        }
    }
}
