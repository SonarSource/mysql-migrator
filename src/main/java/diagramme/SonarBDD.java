package diagramme;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: stephen.broyer
 * Date: 11/20/13
 * Time: 11:03 AM
 * To change this template use File | Settings | File Templates.
 */
public class SonarBDD {

    private String bddName;
    private List<SonarTable> tables_of_bdd;

    public SonarBDD(String bddName){
        this.bddName = bddName;
    }

    /* GETTERS */
    public List<SonarTable>  getBDDTables(){
        return tables_of_bdd;
    }
    public String getBddNameName(){
        return this.bddName;
    }
    /* SETTERS */
    public void setBddTables (List<SonarTable> tables_of_bdd){
        this.tables_of_bdd = tables_of_bdd;
    }

    public void setBddName (String bddName){
        this.bddName = bddName;
    }
}
