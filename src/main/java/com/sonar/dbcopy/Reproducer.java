/*
 * Copyright (C) 2013 SonarSource SA
 * All rights reserved
 * mailto:contact AT sonarsource DOT com
 */

package com.sonar.dbcopy;

import org.slf4j.LoggerFactory;

import java.sql.*;

public class Reproducer {

  private static org.slf4j.Logger LOGGER = LoggerFactory.getLogger(org.slf4j.Logger.ROOT_LOGGER_NAME);
  private ConnecterDatas dcSource, dcDest;
  private Database database;
  private Closer closer;

  public Reproducer(ConnecterDatas dcSource, ConnecterDatas dcDest, Database database) {
    this.dcSource = dcSource;
    this.dcDest = dcDest;
    this.database = database;
  }

  public void execute() {
    closer =new Closer("Reproducer");

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
        closer.closeResultSet(resultSetSource);
        closer.closeStatement(sourceStatement);
        closer.closeStatement(destinationStatement);
      }
    } catch (SQLException e) {
      throw new DbException("Problem when reading datas from source in Reproducer", e);
    } finally {
      closer.closeResultSet(resultSetSource);
      closer.closeStatement(sourceStatement);
      closer.closeStatement(destinationStatement);
      closer.closeConnection(connectionSource);
      closer.closeConnection(connectionDestination);
      LOGGER.info("EveryThing is finally closed.");
    }
  }
}
