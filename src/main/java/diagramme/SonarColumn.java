package diagramme;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.Date;
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
    protected String columnName;
    protected String typeOfColumn;
    protected int sizeOfType;
    protected boolean canBeNull;
    protected boolean anIndexIsPresent;
    protected SonarSequence sequence_if_id = null;
    protected List<Object> objectTableData;



    /* CONSTRUCTEUR */

    public SonarColumn (String columnName , String tableName){

        this.columnName = columnName;
        if (columnName.equals("id")){
            this.sequence_if_id = new SonarSequence("seq_"+tableName);
        }
    }

    /* METHODES */
    public void addCharacteristicOfColumn(String type, int size, boolean canBeNull, boolean anIndexIsPresent){
        this.typeOfColumn = type;
        this.sizeOfType = size;
        this.canBeNull = canBeNull;
        this.anIndexIsPresent = anIndexIsPresent;
    }

    public void addSequenceOnId(String tableName){
        this.sequence_if_id = new SonarSequence(tableName+"_id_seq");
    }


    public void addDataObjectInTable(Object object){
        this.objectTableData.add(object);
    }

    public Object getData(int index){
        return objectTableData.get(index);
    }
    public String getName(){
        return this.columnName;
    }
}
