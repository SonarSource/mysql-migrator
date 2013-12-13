/*
 * Copyright (C) 2013 SonarSource SA
 * All rights reserved
 * mailto:contact AT sonarsource DOT com
 */
package com.sonar.dbcopy;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class DataPutInBase {

  private static final Logger LOGGER = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);
  private PreparedStatement statementDest;

  public DataPutInBase() throws IOException {
  }

  public void insertDatasToDestination(Connection connectionDest, Database database) throws SQLException {

    for (int indexTable = 0; indexTable < database.getNbTables(); indexTable++) {

      /* GET ( TABLE, TABLENAME, ROW NB OF TABLE, COLUMNS AND NB OF COLUMNS ) FROM JAVA DB OBJECT */
      Table table = database.getTable(indexTable);
      String tableName = table.getName();
      int nbRowsInTable = table.getNbRows();
      List<Column> columns = table.getColumns();
      int nbColumns = columns.size();

      /* MAKE STRINGS TO PUT IN SQL INSERT REQUEST */
      ListColumnsAsString lcas = new ListColumnsAsString(columns);
      String columnsAsString = lcas.makeColumnString();
      String questionMarkString = lcas.makeQuestionMarkString();
      String sqlRequest = "INSERT INTO " + tableName + " (" + columnsAsString + ") VALUES(" + questionMarkString + ");";

      /* PREPARE STATEMENT BEFORE SENDING */
      statementDest = connectionDest.prepareStatement(sqlRequest);
      try {
        /* ADD DATA IN PREPARED STATEMENT FOR EACH COLUMN AND ROW BY ROW */
        for (int indexRow = 0; indexRow < nbRowsInTable; indexRow++) {
          for (int indexColumn = 0; indexColumn < nbColumns; indexColumn++) {
            Object objectToInsert = database.getData(indexTable,indexColumn,indexRow);
            statementDest.setObject(indexColumn + 1, objectToInsert);
          }
          /* EXECUTE STATEMENT FOR EACH ROW */
          statementDest.executeUpdate();
        }
      } catch (SQLException e) {
        throw new DbException("Problem when inserting datas in database destination.", e);
      } finally {
        closeStatement();
      }
      LOGGER.log(Level.INFO, "DATAS ADDED IN " + tableName + " TABLE");
    }
  }

  private void closeStatement() {
    try {
      statementDest.close();
    } catch (SQLException e) {
      throw new DbException("Closing statement destination in DataPutInBase failed.", e);
    }
  }
}
