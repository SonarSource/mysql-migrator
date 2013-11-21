package essai;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Created with IntelliJ IDEA.
 * User: stephen.broyer
 * Date: 11/13/13
 * Time: 3:24 PM
 * To change this template use File | Settings | File Templates.
 */
public class PostgreConnection {

    private Connection cnt;

    public PostgreConnection(){
    }

    public Connection doPostgreConnection ()throws SQLException,ClassNotFoundException{

        Class.forName("org.postgresql.Driver");
        String usr = "sonar", pwd = "sonar";
        String url = "jdbc:postgresql://localhost:5432/sonar";
        cnt = DriverManager.getConnection(url, usr, pwd);
        return cnt;
    }

    public Connection doAnotherConnectionToWrite() throws SQLException,ClassNotFoundException{
        Class.forName("org.postgresql.Driver");
        String usr = "sonar", pwd = "sonar";
        String url = "jdbc:postgresql://localhost:5432/sonarToWrite";
        cnt = DriverManager.getConnection(url, usr, pwd);
        return cnt;

    }
}
