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
    private String user = "sonar";
    private String password = "sonar";
    private String classForNameString;
    private String urlComplete;
    private Connection connection;
    private Statement statement;

    public SimpleConnection() throws SQLException, ClassNotFoundException {

    }

    public Statement getStatement() throws ClassNotFoundException, SQLException {
        statement = connection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE , ResultSet.CONCUR_UPDATABLE, ResultSet.HOLD_CURSORS_OVER_COMMIT);
        return statement;
    }

    public void doConnection() throws ClassNotFoundException, SQLException {
        Class.forName(classForNameString);
        connection =DriverManager.getConnection(urlComplete, user, password);
    }

    public void addParamConnection(ConnectionParameters connParam){
        switch (connParam.bdd){
            case 1: classForNameString="org.XXXX.Driver";
                    urlComplete ="jdbc:XXXX://localhost:"+connParam.port+"/"+connParam.getBaseName();
                    break;
            case 2: classForNameString="org.postgresql.Driver";
                    urlComplete ="jdbc:postgresql://localhost:"+connParam.port+"/"+connParam.getBaseName();
                    //System.out.println(urlComplete);
                    break;
            case 3: classForNameString="org.XXXX.Driver";
                    urlComplete ="jdbc:XXXX://localhost:"+connParam.port+"/"+connParam.getBaseName();
                    break;
            case 4: classForNameString="org.XXXX.Driver";
                    urlComplete ="jdbc:XXXX://localhost:"+connParam.port+"/"+connParam.getBaseName();
                    break;

        }
    }

    public Connection getConnection(){
        return connection;
    }

    public void closeStatement(){
        try {
            statement.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    public void closeConnection() throws SQLException {
        connection.close();
    }

}
