package diagramme;

import java.sql.SQLException;

/**
 * Created with IntelliJ IDEA.
 * User: stephen.broyer
 * Date: 11/21/13
 * Time: 11:56 AM
 * To change this template use File | Settings | File Templates.
 */
public class BddDataReproducer {

    protected DataGetter dataGetter;
    protected DataPutInBase dataPutInBase;


    public BddDataReproducer(BddConnecter bddConnecter,BddBuider bddBuider) throws SQLException {

        try{
            dataGetter = new DataGetter(bddConnecter.sourceStatement, bddBuider.sonarBDD);
            dataGetter.doRequest();


            dataPutInBase = new DataPutInBase(bddConnecter.destStatement, bddBuider.sonarBDD);
            dataPutInBase.doInsertInto();

        }
        catch (Exception e){e.getStackTrace();}

    }




}
