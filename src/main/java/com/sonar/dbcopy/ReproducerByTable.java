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
  private ConnecterDatas dc;
  private Database database;

  public ReproducerByTable(ConnecterDatas dc, Database database) {
    this.dc = dc;
    this.database = database;
  }

  public void execute() {

    /* OBJECT DECLARATION */
    int lineWritten, nbCommit;
    Object objectGetted;

    /* JDBC OBJECT DECLARATION */
    Statement sourceStatement = null;
    PreparedStatement destinationStatement = null;
    ResultSet resultSetSource = null;

    /* DO CONNECTION SOURCE AND DESTINATION */
    Connection connectionSource = new Connecter().doSourceConnection(dc);
    Connection connectionDestination = new Connecter().doDestinationConnection(dc);

    /* LOOP TO COPY BY TABLE , UNDER try-catch-finally NEEDED TO CLOSE JDBC ELEMENTS IF EXCEPTION THROWN */
    /* -------------------------------------------------------------------------- */
    try {

      /* SET AUTOCOMMIT FOR DESTINATION TO PUSH DATAS WHEN DEVELOPER DECIDE */
      try {
        connectionSource.setAutoCommit(false);
        connectionDestination.setAutoCommit(false);
      } catch (SQLException e) {
        throw new DbException("Problem to set autocommit for destination connection in Reproducer.", e);
      }

      /* FOR EACH TABLE */
      for (int indexTable = 0; indexTable < database.getNbTables(); indexTable++) {

      /* RESET COUNTERS */
        lineWritten = 0;
        nbCommit = 0;

      /* GET INFORMATION FROM EACH TABLE  */
        Table table = database.getTable(indexTable);
        String tableName = table.getName();
        int nbColInTable = table.getNbColumns();
        int nbRowsInTable = table.getNbRows();

      /* LOG THE BEGINNING OF COPY FOR THIS TABLE */
        LOGGER.info("START " + tableName + "  " + indexTable);

       /* MAKE STRINGS TO PUT IN SQL INSERT REQUEST TO DESTINATION */
        ListColumnsAsString lcas = new ListColumnsAsString(table);
        String columnsAsString = lcas.makeColumnString();
        String questionMarkString = lcas.makeQuestionMarkString();
        String sqlRequest = "INSERT INTO " + tableName + " (" + columnsAsString + ") VALUES(" + questionMarkString + ");";

      /* MAKE STATEMENTS SOURCE AND DESTINATION */
        try {
          sourceStatement = connectionSource.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
          sourceStatement.setFetchSize(1);
        } catch (SQLException e) {
          throw new DbException("Problem to create or fetch size of source statement in Reproducer.", e);
        }
        try {
          destinationStatement = connectionDestination.prepareStatement(sqlRequest);
        } catch (SQLException e) {
          throw new DbException("Problem to prepare destination statement in Reproducer.", e);
        }


      /* GET RESULTSET FROM SOURCE */
        try {
          resultSetSource = sourceStatement.executeQuery("SELECT * FROM " + tableName);
        } catch (SQLException e) {
          throw new DbException("Problem with source resulset in Reproducer.", e);
        }

      /* ITERATE ON SOURCE RESULTSET AND INSERT IN DESTINATION PREPARED STATEMENT */

        while (resultSetSource.next()) {
          lineWritten++;
          for (int indexColumn = 0; indexColumn < nbColInTable; indexColumn++) {
            /* GET */
            objectGetted = resultSetSource.getObject(indexColumn + 1);

            /* INSERT */
            if (objectGetted == null) {
              destinationStatement.setObject(indexColumn + 1, null);
            } else {
              destinationStatement.setObject(indexColumn + 1, objectGetted);
            }
          }
          /* ADD BATCH FOR EACH ROW */
          destinationStatement.addBatch();

          /* LOG , EXECUTE BATCH AND COMMIT EVERY 1000 ROWS */
          if (lineWritten > 1000 * nbCommit) {
            destinationStatement.executeBatch();
            connectionDestination.commit();
            LOGGER.info("Reading and Writing: " + indexTable + "   " + tableName + " LINES " + lineWritten + " / " + nbRowsInTable);
            nbCommit++;
          }
        }

        /* EXECUTE BATCH AND COMMIT FOR ULTIMATE ROWS */
        destinationStatement.executeBatch();
        connectionDestination.commit();

        /* CLOSE STATEMENTS AND RESULTSET FROM THIS TABLE */
        resultSetSource.close();
        sourceStatement.close();
        destinationStatement.close();

        /* LOG THIS TABLE COMPLETED */
        LOGGER.info("TABLE " + tableName + "   " + indexTable + " FINISHED.");

      }
    } catch (SQLException e) {
      throw new DbException("problem when reading datas from source in Reproducer", e);
    } finally {
      LOGGER.info(" | - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - | ");
      try {
        resultSetSource.close();
      } catch (Exception e) {
        LOGGER.error(" | ResultSet to read datas from source can't be closed or is already closed.       | " + e);
      }

      try {
        sourceStatement.close();
      } catch (Exception e) {
        LOGGER.error(" | SourceStatement to read datas from source can't be closed or is already closed. | " + e);
      }

      try {
        destinationStatement.close();
      } catch (Exception e) {
        LOGGER.error(" | Statement to write  datas from source can't be closed or is already closed.     | " + e);
      }

      try {
        connectionSource.close();
      } catch (Exception e) {
        LOGGER.error(" | ConnectionSource to read datas from source can't be closed or is already closed.| " + e);
      }

      try {
        connectionDestination.close();
      } catch (Exception e) {
        LOGGER.error(" | Connection to write  datas from source can't be closed or is already closed.    | " + e);
      }
      LOGGER.info(" | EveryThing is finally closed.                                                   | ");
      LOGGER.info(" | - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - | ");
    }
  }
}
