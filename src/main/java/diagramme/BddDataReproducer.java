package diagramme;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: stephen.broyer
 * Date: 11/21/13
 * Time: 11:56 AM
 * To change this template use File | Settings | File Templates.
 */
public class BddDataReproducer {

    protected DataGetter dataGetter;
    protected DataRecorder dataRecorder;
    protected DataPutInBase dataPutInBase;
    private List<ResultSet> resultSets;


    public BddDataReproducer(BddConnecter bddConnecter,BddBuider bddBuider) throws SQLException {
        System.out.println("### DEBUG ### in BddDataReproducer");

        try{
        dataGetter = new DataGetter();
        dataGetter.doRequest(bddConnecter.sourceStatement, bddBuider.sonarBDD);
        /*
        dataRecorder = new DataRecorder();
        dataRecorder.recordData(resultSets,bddBuider.sonarBDD);
System.out.println("### DEBUG ### in BddDataReproducer datarecorder done");
          */

            //dataPutInBase = new DataPutInBase(bddConnecter.destStatement);
      }
      catch (Exception e){}

    }




}
