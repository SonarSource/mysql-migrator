package diagramme;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Created with IntelliJ IDEA.
 * User: stephen.broyer
 * Date: 11/15/13
 * Time: 9:39 AM
 * To change this template use File | Settings | File Templates.
 */
public class MyApp {

    public static void main(String[] args) throws SQLException, ClassNotFoundException {

    /* BUILD DB OBJECT */
        BddBuider bddBuider = new BddBuider();

    /* DO CONNECTION */
        BddConnecter bddConnecter = new BddConnecter();

    /* DO COPY */
        BddDataReproducer bddDataReproducer = new BddDataReproducer(bddConnecter,bddBuider);



    }
}
