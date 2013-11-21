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

    public ConnectionParameters sourceConnectionParameters,destConnectionParameters;
    public Statement sourceStatement,destStatement;


    public SimpleConnection sourceConnection;
    public SimpleConnection destConnection;

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
