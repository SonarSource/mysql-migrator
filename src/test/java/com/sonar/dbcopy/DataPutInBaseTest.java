/*
 * Copyright (C) 2013 SonarSource SA
 * All rights reserved
 * mailto:contact AT sonarsource DOT com
 */
package com.sonar.dbcopy;

import org.junit.Before;
import org.junit.Test;
import static junit.framework.Assert.assertEquals;

import java.sql.*;
import java.util.List;

public class DataPutInBaseTest {

  private DataPutInBase dataPutInBase;
  private DatabaseUtils databaseUtils;
  private Bdd bdd;
  private Connection connection;
  private Statement statement;
  private List<Table> tablesOfBdd;

  @Before
  public void createInstance() throws SQLException, ClassNotFoundException {

    databaseUtils = new DatabaseUtils();
    /* MAKE DATABASE JAVA OBJECT */
    bdd = databaseUtils.makeBddJavaObject();
    databaseUtils.addDatasToBddJavaObject();

    /* MAKE AN EMPTY DATABASE H2 */
    databaseUtils.makeDatabaseH2("sonarToWrite");
    connection = databaseUtils.getConnectionFromH2();
    /* DO THE COPY FROM JAVA OBJECT TO EMPTY DATABASE H2 */
    dataPutInBase = new DataPutInBase(connection,bdd);
  }

  @Test
  public void testDoInsertIntoTables() throws SQLException, ClassNotFoundException {
    /* CONNECT AND GET STATEMENT TO READ RESULT IN DATABASE H2 */
    statement = databaseUtils.getStatementFromH2();
    tablesOfBdd = bdd.getBddTables();
    /* FOR EACH TABLE */
    for(int indexTable=0;indexTable<tablesOfBdd.size();indexTable++){
      Table tableToCompare = tablesOfBdd.get(indexTable);
      String tableName = tableToCompare.getTableName();
      int nbColumnInTable =  tableToCompare.getColumns().size();
      /* GET CONTENT OF TABLE */
      ResultSet resultSet =  statement.executeQuery("SELECT * FROM " +tableName+" ORDER BY 1;");
      int indexRow = 0;

      /* COMPARE BY ROW THE DATA BETWEEN DATABASE H2 AND JAVA OBJECT */
      /* FOR EACH ROW */
      while (resultSet.next()) {
        /* FOR EACH COLUMN */
        for(int indexColumn=0; indexColumn<nbColumnInTable;indexColumn++){
          /* DO JUNIT TEST */
          assertEquals(bdd.getDataFromColumnFromTable(indexTable,indexColumn,indexRow),resultSet.getObject(indexColumn));
        }
        indexRow++;
      }
      resultSet.beforeFirst();
    }
    statement.close();
  }
}
