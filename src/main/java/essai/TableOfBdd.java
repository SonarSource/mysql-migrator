package essai;

import java.util.ArrayList;
/**
 * Created with IntelliJ IDEA.
 * User: stephen.broyer
 * Date: 11/14/13
 * Time: 3:34 PM
 * To change this template use File | Settings | File Templates.
 */
public class TableOfBdd {

    protected String tableName;
    protected int nbColOfTable;
    protected ArrayList<String> columnListName;


    public TableOfBdd(String name, int nbCol){
        this.tableName = name;
        this.nbColOfTable = nbCol;
        this.columnListName = new ArrayList<String>();
    }

    public void addColumnNamesByOne(String colName,String colType){
        columnListName.add(colName);
        ColumnOfTable  colData = new ColumnOfTable();
    }

}
