/*
 * Copyright (C) 2013 SonarSource SA
 * All rights reserved
 * mailto:contact AT sonarsource DOT com
 */

package com.sonar.dbcopy.reproduce.process;

import com.sonar.dbcopy.utils.data.ConnecterDatas;
import com.sonar.dbcopy.utils.data.Database;
import com.sonar.dbcopy.utils.data.Table;
import com.sonar.dbcopy.utils.toolconfig.*;
import org.slf4j.LoggerFactory;

import java.sql.*;

public class LoopByTable {

  private static org.slf4j.Logger LOGGER = LoggerFactory.getLogger(org.slf4j.Logger.ROOT_LOGGER_NAME);
  private ConnecterDatas cdSource, cdDest;
  private Database databaseSource, databaseDest;
  private CharacteristicsRelatedToEditor chRelToEditor;

  public LoopByTable(ConnecterDatas cdSource, ConnecterDatas cdDest, Database databaseSource, Database databaseDest) {
    this.cdSource = cdSource;
    this.cdDest = cdDest;
    this.databaseSource = databaseSource;
    this.databaseDest = databaseDest;
    this.chRelToEditor = new CharacteristicsRelatedToEditor();
  }

  public void execute() {
    Closer closer = new Closer("LoopByTable");
    ModifySqlServerOption modifySqlServerOption = null;
    int indexTable = 0;
    Connection connectionSource = null;
    Connection connectionDestination = null;
    Statement statementCountDest = null;
    ResultSet resultSetCountDest = null;
    try {
      /* ****** FOR EACH TABLE: ****** */
      for (indexTable = 0; indexTable < databaseSource.getNbTables(); indexTable++) {

        // DO CONNECTIONS FOR EACH TABLE
        connectionSource = new Connecter().doConnection(cdSource);
        connectionDestination = new Connecter().doConnection(cdDest);
        DatabaseMetaData metaDest = connectionDestination.getMetaData();
        boolean destinationIsSqlServer = chRelToEditor.isSqlServer(metaDest);
        connectionDestination.setAutoCommit(false);

        // CREATE CLASS FOR SQL SERVER DESTINATION OPTION : TO AUTHORIZE INSERT ON TABLE
        if (destinationIsSqlServer) {
          modifySqlServerOption = new ModifySqlServerOption();
        }

        // PREPARE SQL REQUEST PARAMETERS
        Table tableSource = databaseSource.getTable(indexTable);
        String tableSourceName = tableSource.getName();
        Table tableDest = databaseDest.getTableByName(tableSourceName);

        // VERIFY IF TABLE EXISTS IN DESTINATION
        if (tableDest == null) {
          LOGGER.error("WARNING !! Can't WRITE in TABLE :" + tableSourceName + " because it doesn't exist in destination database. ");
        } else {

          //SQL SERVER DESTINATION OPTION :  PUT IDENTITY_INSERT AT on FOR THE CURRENT TABLE
          if (destinationIsSqlServer) {
            modifySqlServerOption.modifyIdentityInsert(connectionDestination, tableSourceName, "ON");
          }

          // READ AND WRITE
          PrepareCopyTable prepareCopyTable = new PrepareCopyTable(tableSource, tableDest);
          prepareCopyTable.makeToolsAndStartCopy(connectionSource, connectionDestination);

          // RESET SEQUENCE
          SequenceReseter sequenceReseter = new SequenceReseter(tableSourceName, cdDest);
          sequenceReseter.execute();

          //SQL SERVER DESTINATION OPTION : PUT IDENTITY_INSERT AT off FOR THE CURRENT TABLE
          if (destinationIsSqlServer) {
            modifySqlServerOption.modifyIdentityInsert(connectionDestination, tableSourceName, "OFF");
          }
        }
        // COUNT COLUMNS IN TABLE DESTINATION TO COMPARE IT TO
        if (tableDest != null) {
          tableDest.setNbRows(0);
          statementCountDest = connectionDestination.createStatement();
          resultSetCountDest = statementCountDest.executeQuery("SELECT count(*) FROM " + tableDest.getName());
          while (resultSetCountDest.next()) {
            int rowsInTableDest = resultSetCountDest.getInt(1);
            tableDest.setNbRows(rowsInTableDest);
          }
        }
        // CLOSE CONNECTION AND OBJECT AFTER EACH TABLE COPY
        closer.closeResultSet(resultSetCountDest);
        closer.closeStatement(statementCountDest);
        closer.closeConnection(connectionSource);
        closer.closeConnection(connectionDestination);
      }
    } catch (SQLException e) {
      throw new DbException("Problem when do loop for tables in LoopByTable at TABLE : " + databaseSource.getTableName(indexTable), e);
    } finally {
      closer.closeResultSet(resultSetCountDest);
      closer.closeStatement(statementCountDest);
      closer.closeConnection(connectionSource);
      closer.closeConnection(connectionDestination);
      LOGGER.info("EveryThing is finally closed in LoopByTable.");
    }
  }
}

