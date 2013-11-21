package diagramme;

import java.util.Arrays;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: stephen.broyer
 * Date: 11/20/13
 * Time: 11:03 AM
 * To change this template use File | Settings | File Templates.
 */
public class SonarBDD {

    protected String bddName;
    protected List<SonarTable> tables_of_bdd;

    public SonarBDD(String bddName){
        this.bddName = bddName;
    }


    public List<SonarTable>  getBDDTables(){
        return tables_of_bdd;
    }
    public String getBddNameName(){
        return this.bddName;
    }
}
