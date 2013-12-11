/*
 * Copyright (C) 2013 SonarSource SA
 * All rights reserved
 * mailto:contact AT sonarsource DOT com
 */
package com.sonar.dbcopy;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static junit.framework.Assert.assertEquals;

import java.io.IOException;
import java.sql.*;
import java.util.List;

public class DataPutInBaseTest {

  private DataPutInBase dataPutInBase;
  private DatabaseUtils databaseUtils;
  private Bdd bdd;
  private Connection connectionFromUtils;
  private List<Table> tablesOfBdd;

  @Before
  public void createInstance() throws SQLException, ClassNotFoundException, IOException {

    databaseUtils = new DatabaseUtils();
    /* MAKE DATABASE JAVA OBJECT */
    databaseUtils.makeBddJavaObjectWithTable();
    databaseUtils.addTablesToBddJavaObject();
    databaseUtils.addColumnsToBddJavaObject();
    databaseUtils.addDatasToBddJavaObject();
    bdd = databaseUtils.getJavaBddFromUtils();
    tablesOfBdd = bdd.getBddTables();

    /* MAKE AN EMPTY DATABASE H2 : don't insert datas in h2 database */
    databaseUtils.makeDatabaseH2Withtables("sonarToWrite");
    connectionFromUtils = databaseUtils.getConnectionFromH2();


    /* DO THE COPY FROM JAVA OBJECT TO EMPTY DATABASE H2 */
    dataPutInBase = new DataPutInBase();
    dataPutInBase.insertDatasFromJavaDatabaseToDestinationDatabase(connectionFromUtils,bdd.getBddTables());
  }

  @Test
  public void verifyInsertDatasFromJavaDatabaseToDestinationDatabase() throws SQLException, ClassNotFoundException {
     Statement statement = connectionFromUtils.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE , ResultSet.CONCUR_READ_ONLY, ResultSet.HOLD_CURSORS_OVER_COMMIT);

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

  @After
  public void  closeEveryThing() throws SQLException {
    connectionFromUtils.close();
  }

}
