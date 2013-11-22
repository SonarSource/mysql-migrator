package diagramme;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: stephen.broyer
 * Date: 11/21/13
 * Time: 12:06 PM
 * To change this template use File | Settings | File Templates.
 */
public class DataPutInBase {

    private SonarBDD sonarBDD;
    private Connection connectionDest;
    private List<SonarTable> listOfTables;

    public DataPutInBase(Connection connection, SonarBDD sonarBDD){
        this.connectionDest = connection;
        this.sonarBDD = sonarBDD;
        this.listOfTables = sonarBDD.getBDDTables();
    }

    public void doInsertIntoTables (){


        for(int indexTable=0;indexTable<listOfTables.size();indexTable++){

            SonarTable sonarTable = listOfTables.get(indexTable);
            String tableName =  sonarTable.getTableName();
            int nbRowsInTable = sonarTable.getNbRows();
            List<SonarColumn> columns = sonarTable.getColumns();
            int nbColumns = columns.size();

            ListColumnsAsString lcas = new ListColumnsAsString(columns);
            String columnsAsString = lcas.makeString();
            String questionMarkString = lcas.makeQuestionMarkString();

            String sql = "INSERT INTO "+tableName+" ("+columnsAsString+") VALUES("+questionMarkString+");";
            //sql must be : INSERT INTO tableName (column1,column2,...) VALUES (?,?,...);
            try {
                PreparedStatement statementDest = connectionDest.prepareStatement(sql);
                for(int indexRow=0;indexRow<nbRowsInTable;indexRow++){
                    for(int indexColumn=0;indexColumn<nbColumns;indexColumn++){
                            Object objectToInsert = columns.get(indexColumn).getDataWithIndex(indexRow);
                            //System.out.println("objectToInsert in table: "+tableName+"at row:"+indexRow+" and col: "+indexColumn+" is "+objectToInsert);
                            statementDest.setObject(indexColumn+1,objectToInsert);
                    }
                    statementDest.executeUpdate();
                }

            }
            catch(SQLException sqle){
                System.out.println("Exception SQL : ");
                while (sqle != null) {
                    String message = sqle.getMessage();
                    String sqlState = sqle.getSQLState();
                    int errorCode = sqle.getErrorCode();
                    System.out.println("Message = "+message);
                    System.out.println("SQLState = "+sqlState);
                    System.out.println("ErrorCode = "+errorCode);
                    sqle.printStackTrace();
                    sqle = sqle.getNextException();
                }
            }
        }
    }
}
