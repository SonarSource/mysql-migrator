package diagramme;

import java.sql.SQLException;
import java.sql.Statement;

/**
 * Created with IntelliJ IDEA.
 * User: stephen.broyer
 * Date: 11/21/13
 * Time: 11:06 AM
 * To change this template use File | Settings | File Templates.
 */
public class BddConnecter {

    private ConnectionParameters sourceConnectionParameters,destConnectionParameters;
    protected Statement sourceStatement,destStatement;

    private SimpleConnection sourceConnection;
    private SimpleConnection destConnection;

    public BddConnecter(){
        sourceConnectionParameters = new ConnectionParameters("source");
        destConnectionParameters = new ConnectionParameters("destination");
        try{
            sourceConnection = new SimpleConnection(sourceConnectionParameters);
            sourceStatement = sourceConnection.doConnection();
            destConnection = new SimpleConnection(destConnectionParameters);
            destStatement = destConnection.doConnection();
        }
        catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void closeConnections(){
        try {
            sourceConnection.closeConnection();
            destConnection.closeConnection();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

}
