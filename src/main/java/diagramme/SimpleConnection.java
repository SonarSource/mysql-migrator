package diagramme;

import java.sql.*;

/**
 * Created with IntelliJ IDEA.
 * User: stephen.broyer
 * Date: 11/15/13
 * Time: 2:48 PM
 * To change this template use File | Settings | File Templates.
 */
public class SimpleConnection {
    protected String user = "sonar";
    protected String password = "sonar";
    protected String bddName = "sonar";
    protected String classForNameString;
    protected String urlComplete;
    protected Connection connection;
    protected Statement statement;

    public SimpleConnection(ConnectionParameters connParam) throws SQLException, ClassNotFoundException {
        addParamConnection(connParam);
    }

    protected Statement doConnection() throws ClassNotFoundException, SQLException {
        Class.forName(classForNameString);
        connection =DriverManager.getConnection(urlComplete, user, password);
        statement = connection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE , ResultSet.CONCUR_UPDATABLE, ResultSet.HOLD_CURSORS_OVER_COMMIT);

        return statement;
    }

    private void addParamConnection(ConnectionParameters connParam){
        switch (connParam.bdd){
            case 1: classForNameString="org.XXXX.Driver";
                    urlComplete ="jdbc:XXXX://localhost:"+connParam.port+"/"+bddName;
                    break;
            case 2: classForNameString="org.postgresql.Driver";
                    urlComplete ="jdbc:postgresql://localhost:"+connParam.port+"/"+bddName;
                    System.out.println(urlComplete);
                    break;
            case 3: classForNameString="org.XXXX.Driver";
                    urlComplete ="jdbc:XXXX://localhost:"+connParam.port+"/"+bddName;
                    break;
            case 4: classForNameString="org.XXXX.Driver";
                    urlComplete ="jdbc:XXXX://localhost:"+connParam.port+"/"+bddName;
                    break;

        }
    }

    public void closeConnection() throws SQLException {
        statement.close();
        connection.close();
    }

}
