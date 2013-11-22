package diagramme;

import java.util.ArrayList;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: stephen.broyer
 * Date: 11/22/13
 * Time: 1:26 PM
 * To change this template use File | Settings | File Templates.
 */
public class DataRowJavaSonarTable {

    private SonarTable sonarTable;
    private List<SonarColumn> listOfColumns;
    private int nbOfColumns;

    private List<String> listOfColumnTypes;
    private List<Object> listOfObjectAsRow;

    public DataRowJavaSonarTable(SonarTable sonarTable){
        this.sonarTable = sonarTable;
        this.listOfColumns = this.sonarTable.getColumns();
        this.nbOfColumns = listOfColumns.size();

        this.listOfColumnTypes = new ArrayList<String>();
        this.listOfObjectAsRow = new ArrayList<Object>();

        for(int indexColumn=0;indexColumn<nbOfColumns;indexColumn++){
            listOfColumnTypes.add(listOfColumns.get(indexColumn).getColumnType());
        }
    }


    /* GETTERS */
    public void doListOfObjectAsRow(int indexRow){
        for(int indexColumn=0;indexColumn<nbOfColumns;indexColumn++){
            listOfObjectAsRow.add(listOfColumns.get(indexColumn).getDataWithIndex(indexRow));
        }
    }
    public String makeStringOfColumns(){
        String stringOfColumns = new String();
            for(int indexColumn=0;indexColumn<listOfColumns.size();indexColumn++){
                stringOfColumns+=","+listOfColumns.get(indexColumn).getColumnName();
            }
        stringOfColumns = stringOfColumns.substring(1);

        return stringOfColumns;
    }

    public String makeStringOfValues(){
        String stringOfValues=new String();

        return stringOfValues;
    }


}
