/*
 * Copyright (C) 2013 SonarSource SA
 * All rights reserved
 * mailto:contact AT sonarsource DOT com
 */

package com.sonar.dbcopy;

import org.slf4j.LoggerFactory;

import java.sql.*;

public class ReproducerByTable {

  private static org.slf4j.Logger LOGGER = LoggerFactory.getLogger(org.slf4j.Logger.ROOT_LOGGER_NAME);
  private ConnecterDatas dcSource, dcDest;
  private Database database;

  public ReproducerByTable(ConnecterDatas dcSource, ConnecterDatas dcDest, Database database) {
    this.dcSource = dcSource;
    this.dcDest = dcDest;
    this.database = database;
  }

  public void execute() {
    Statement sourceStatement = null;
    PreparedStatement destinationStatement = null;
    ResultSet resultSetSource = null;

    /* DO CONNECTION SOURCE AND DESTINATION */
    Connection connectionSource = new Connecter().doConnection(dcSource);
    Connection connectionDestination = new Connecter().doConnection(dcDest);
    try {
      connectionDestination.setAutoCommit(false);

      /* FOR EACH TABLE */
      for (int indexTable = 0; indexTable < database.getNbTables(); indexTable++) {
        /* GET INFORMATION */
        Table table = database.getTable(indexTable);
        String tableName = table.getName();
        int nbColInTable = table.getNbColumns();
        int nbRowsInTable = table.getNbRows();

        /* MAKE STRINGS TO PUT IN SQL INSERT REQUEST */
        ListColumnsAsString lcas = new ListColumnsAsString(table);
        String sqlRequest = "INSERT INTO " + tableName + " (" + lcas.makeColumnString() + ") VALUES(" + lcas.makeQuestionMarkString() + ");";

        /* MAKE STATEMENTS AND RESULTSET */
        sourceStatement = connectionSource.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
        sourceStatement.setFetchSize(1);
        destinationStatement = connectionDestination.prepareStatement(sqlRequest);
        resultSetSource = sourceStatement.executeQuery("SELECT * FROM " + tableName);

        /* ITERATE ON SOURCE RESULTSET AND INSERT IN DESTINATION PREPARED STATEMENT */
        LOGGER.info("START : " + indexTable + "   " + tableName + ".");
        LoopWriter loopWriter = new LoopWriter(nbColInTable, indexTable, tableName, nbRowsInTable);
        loopWriter.readAndWrite(resultSetSource, destinationStatement, connectionDestination);
        LOGGER.info("FINISH : " + indexTable + "   " + tableName + ".");

        /* CLOSE STATEMENTS AND RESULTSET FROM THIS TABLE */
        resultSetSource.close();
        sourceStatement.close();
        destinationStatement.close();


      }
    } catch (SQLException e) {
      throw new DbException("problem when reading datas from source in Reproducer", e);
    } finally {
      LOGGER.info(" | - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - | ");
      try {
        resultSetSource.close();
      } catch (SQLException e) {
        LOGGER.error(" | ResultSet to read datas from source can't be closed or is already closed.       | " + e);
      }
      try {
        sourceStatement.close();
      } catch (SQLException e) {
        LOGGER.error(" | SourceStatement to read datas from source can't be closed or is already closed. | " + e);
      }
      try {
        destinationStatement.close();
      } catch (SQLException e) {
        LOGGER.error(" | Statement to write  datas from source can't be closed or is already closed.     | " + e);
      }
      try {
        connectionSource.close();
      } catch (SQLException e) {
        LOGGER.error(" | ConnectionSource to read datas from source can't be closed or is already closed.| " + e);
      }
      try {
        connectionDestination.close();
      } catch (SQLException e) {
        LOGGER.error(" | Connection to write  datas from source can't be closed or is already closed.    | " + e);
      }
      LOGGER.info(" | EveryThing is finally closed.                                                   | ");
      LOGGER.info(" | - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - | ");
    }
  }
}
