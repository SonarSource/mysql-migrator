package diagramme;

import java.sql.SQLException;

/**
 * Created with IntelliJ IDEA.
 * User: stephen.broyer
 * Date: 11/15/13
 * Time: 9:39 AM
 * To change this template use File | Settings | File Templates.
 */
public class DbCopyApp {

    public static void main(String[] args) throws SQLException, ClassNotFoundException {

    /* BUILD DB OBJECT */
        BddBuider bddBuider = new BddBuider();

    /* DO CONNECTION */
        BddConnecter bddConnecter = new BddConnecter();
        bddConnecter.doSourceConnectionAndStatement();
        bddConnecter.doOnlyDestinationConnection();

    /* DO COPY */
        new BddDataReproducer(bddConnecter,bddBuider);

    /* DO VERIFYING */

    /* DO CLOSE CONNECTION */
        bddConnecter.closeSourceConnection();
        bddConnecter.closeDestConnection();
    }
}
