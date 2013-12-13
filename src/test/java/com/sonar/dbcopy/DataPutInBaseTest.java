/*
 * Copyright (C) 2013 SonarSource SA
 * All rights reserved
 * mailto:contact AT sonarsource DOT com
 */
package com.sonar.dbcopy;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

import static junit.framework.Assert.assertEquals;

public class DataPutInBaseTest {

  private DataPutInBase dataPutInBase;
  private DatabaseUtils databaseUtils;
  private Database database;
  private Connection connectionFromUtils;
  private List<Table> databaseTables;
  private Statement statement;
  private ResultSet resultSet;

  @Before
  public void createInstance() throws SQLException, ClassNotFoundException, IOException {

    databaseUtils = new DatabaseUtils();
    /* MAKE DATABASE JAVA OBJECT */
    databaseUtils.makeDatabaseJavaObject();
    databaseUtils.addTablesToDatabaseJavaObject();
    databaseUtils.addColumnsToDatabaseJavaObject();
    databaseUtils.addDatasToDatabaseJavaObject();
    database = databaseUtils.getJavaDatabaseFromUtils();
    databaseTables = database.getTables();

    /* MAKE AN EMPTY DATABASE H2 : don't insert datas in h2 database */
    databaseUtils.makeDatabaseH2Withtables("sonarToWrite");
    connectionFromUtils = databaseUtils.getConnectionFromH2();


    /* DO THE COPY FROM JAVA OBJECT TO EMPTY DATABASE H2 */
    dataPutInBase = new DataPutInBase();
    dataPutInBase.insertDatasToDestination(connectionFromUtils, database);
  }

  @Test
  public void testInsertDataToDestination() throws SQLException, ClassNotFoundException {
    statement = connectionFromUtils.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY, ResultSet.HOLD_CURSORS_OVER_COMMIT);

    /* FOR EACH TABLE */
    for (int indexTable = 0; indexTable < databaseTables.size(); indexTable++) {
      Table tableToCompare = databaseTables.get(indexTable);
      String tableName = tableToCompare.getName();
      int nbColumnInTable = tableToCompare.getColumns().size();
      /* GET CONTENT OF TABLE */
      resultSet = statement.executeQuery("SELECT * FROM " + tableName + " ORDER BY 1;");
      int indexRow = 0;

      /* COMPARE BY ROW THE DATA BETWEEN DATABASE H2 AND JAVA OBJECT */
      /* FOR EACH ROW */
      while (resultSet.next()) {
        /* FOR EACH COLUMN */
        for (int indexColumn = 0; indexColumn < nbColumnInTable; indexColumn++) {
          /* DO JUNIT TEST */
          assertEquals(database.getData(indexTable, indexColumn, indexRow), resultSet.getObject(indexColumn));
        }
        indexRow++;
      }
      resultSet.beforeFirst();
    }
    statement.close();
  }

  @After
  public void closeEveryThing() throws SQLException {
    if (!resultSet.isClosed()) {
      resultSet.close();
    }
    if (!statement.isClosed()) {
      statement.close();
    }
    connectionFromUtils.close();
  }

}
