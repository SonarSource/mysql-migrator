package diagramme;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: stephen.broyer
 * Date: 11/20/13
 * Time: 11:04 AM
 * To change this template use File | Settings | File Templates.
 */
public class SonarTable {

    protected String tableName;
    protected List<SonarColumn> columns;
    // TODO find what to do with common rank for all columns of the table

    public SonarTable(String tableName){
        this.tableName = tableName;
        columns=new ArrayList<SonarColumn>();
    }

    public void addAllColumsToTable(String[] columnsList){
        for(int i=0;i<columnsList.length;i++){
            columns.add(new SonarColumn(columnsList[i],tableName));
        }
    }

    public SonarColumn addOneColumnToTable(String columnName, String tableName){
        System.out.println("### DEBUG ### in addOneCol : 1");

        SonarColumn columnToAdd = new SonarColumn(columnName,tableName);
        System.out.println("### DEBUG ### in addOneCol : 2");

        columns.add(columnToAdd);
        System.out.println("### DEBUG ### in addOneCol : 3");

        return columnToAdd;

    }

    public String getTableName(){
        return this.tableName;
    }
}
