package diagramme;

import java.util.ArrayList;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: stephen.broyer
 * Date: 11/20/13
 * Time: 11:04 AM
 * To change this template use File | Settings | File Templates.
 */
public class SonarTable {

    private String tableName;
    private int nbRows;
    private List<SonarColumn> columns;

    public SonarTable(String tableName){
        this.tableName = tableName;
        this.nbRows=0;
        columns=new ArrayList<SonarColumn>();
    }

    public SonarColumn addOneColumnToTable(String columnName, String tableName){
        SonarColumn columnToAdd = new SonarColumn(columnName,tableName);
        columns.add(columnToAdd);
        return columnToAdd;
    }
    public void setTableName(String tableName){
        this.tableName = tableName;
    }
    public void setNbRows(int nbRows){
        this.nbRows = nbRows;
    }

    public String getTableName(){
        return this.tableName;
    }
    public int getNbRows(){
        return nbRows;
    }
    public List<SonarColumn> getColumns(){
        return this.columns;
    }
}
