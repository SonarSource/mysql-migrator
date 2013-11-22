package diagramme;

import java.util.ArrayList;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: stephen.broyer
 * Date: 11/20/13
 * Time: 11:08 AM
 * To change this template use File | Settings | File Templates.
 */
public class SonarColumn {

    /* ATTRIBUTS */
    private String columnName;
    private String columnType;
    private int sizeOfType;
    private boolean canBeNull;
    private boolean anIndexIsPresent;
    private SonarSequence sequence_if_id = null;
    private List<Object> objectTableData;

    /* CONSTRUCTEUR */
    public SonarColumn (String columnName , String tableName){

        this.columnName = columnName;
        objectTableData = new ArrayList<Object>();
        /*
        if (columnName.equals("id")){
            this.sequence_if_id = new SonarSequence("seq_"+tableName);
        } */
    }

    /* METHODES */

    /* GETTERS */
    public List<Object> getDataList(){
        return this.objectTableData;
    }
    public Object getDataWithIndex(int index){
        return objectTableData.get(index);
    }
    public String getColumnName(){
        return this.columnName;
    }
    public String getColumnType(){
        return this.columnType;
    }

    /* SETTERS */
    public void setColumnName(String columnName){
        this.columnName = columnName;
    }
    public void setColumnType(String columnType){
        this.columnType = columnType;
    }
    public void addSequenceOnId(String tableName){
        this.sequence_if_id = new SonarSequence(tableName+"_id_seq");
    }

    public void addColumnType(String type){
        this.columnType = type;
    }
    public void addDataObjectInTable(Object object){
        this.objectTableData.add(object);
    }
    public void addCharacteristicOfColumn(String type, int size, boolean canBeNull, boolean anIndexIsPresent){
        this.columnType = type;
        this.sizeOfType = size;
        this.canBeNull = canBeNull;
        this.anIndexIsPresent = anIndexIsPresent;
    }
}
