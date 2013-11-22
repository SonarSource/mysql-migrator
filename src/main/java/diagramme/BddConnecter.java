package diagramme;

import java.sql.Connection;
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
    private SimpleConnection sourceConnection;
    protected Statement sourceStatement;
    private Connection connectionToPreparedStatement;

    private SimpleConnection destConnection;

    public BddConnecter(){
        sourceConnectionParameters = new ConnectionParameters("source");
        destConnectionParameters = new ConnectionParameters("destination");
    }

    /* SOURCE AND DESTINATION CONNECTION */
    public void doSourceConnectionAndStatement(){
        try{
            sourceConnection = new SimpleConnection();
            sourceConnection.addParamConnection(sourceConnectionParameters);
            sourceConnection.doConnection();
            sourceStatement = sourceConnection.getStatement();
        }
        catch (ClassNotFoundException e) {e.printStackTrace();}
        catch (SQLException e) {e.printStackTrace();}

    }

    public void doOnlyDestinationConnection (){

        try{
            destConnection = new SimpleConnection();
            destConnection.addParamConnection(destConnectionParameters);
            destConnection.doConnection();
            connectionToPreparedStatement=destConnection.getConnection();

        }
        catch (ClassNotFoundException e) {e.printStackTrace();}
        catch (SQLException e) {e.printStackTrace();}

    }

    /* GETTERS */
    public Statement getStatementSource(){
        return sourceStatement;
    }

    public Connection getConnectionDest(){
        return connectionToPreparedStatement;
    }

    /* CLOSE METHODS */
    public void closeSourceConnection(){
        try {
              sourceConnection.closeStatement();
              sourceConnection.closeConnection();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    public void closeDestConnection(){
        try {
            destConnection.closeConnection();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


}
