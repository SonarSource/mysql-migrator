package essai;

import java.util.*;

/**
 * Created with IntelliJ IDEA.
 * User: stephen.broyer
 * Date: 11/14/13
 * Time: 3:41 PM
 * To change this template use File | Settings | File Templates.
 */
public class ColumnOfTable{


    protected ArrayList data = new ArrayList();

    public ColumnOfTable (){

    }
    public void addData (Object theData){
        data.add(theData);
    }
    public Class getTypeOfData(){
        return data.getClass();
    }
    public ArrayList getDataList(){
        return data;
    }

}
