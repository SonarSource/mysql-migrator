/*
 * SonarQube MySQL Database Migrator
 * Copyright (C) 2013-2019 SonarSource SA
 * mailto:info AT sonarsource DOT com
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package com.sonar.dbcopy.reproduce.process;


import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Connection;
import java.sql.Statement;
import java.sql.DatabaseMetaData;

import com.sonar.dbcopy.utils.data.ConnecterData;
import com.sonar.dbcopy.utils.data.Database;
import com.sonar.dbcopy.utils.data.Table;
import com.sonar.dbcopy.utils.toolconfig.CharacteristicsRelatedToEditor;
import com.sonar.dbcopy.utils.toolconfig.Closer;
import com.sonar.dbcopy.utils.toolconfig.Connecter;
import com.sonar.dbcopy.utils.toolconfig.ModifySqlServerOption;
import com.sonar.dbcopy.utils.toolconfig.SqlDbCopyException;
import org.slf4j.LoggerFactory;


public class LoopByTable {

  private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(org.slf4j.Logger.ROOT_LOGGER_NAME);
  private ConnecterData cdSource;
  private ConnecterData cdDest;
  private Database databaseSource;
  private Database databaseDest;
  private int commitSize;

  public LoopByTable(ConnecterData cdSource, ConnecterData cdDest, Database databaseSource, Database databaseDest,
                     int commitSize) {
    this.cdSource = cdSource;
    this.cdDest = cdDest;
    this.databaseSource = databaseSource;
    this.databaseDest = databaseDest;
    this.commitSize = commitSize;
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
        boolean destinationIsSqlServer = CharacteristicsRelatedToEditor.isSqlServer(metaDest);

        connectionSource.setAutoCommit(false);
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
          LOGGER.warn("Can't WRITE in TABLE :{} because it doesn't exist in destination database. ", tableSourceName);
        } else {

          //SQL SERVER DESTINATION OPTION :  PUT IDENTITY_INSERT AT on FOR THE CURRENT TABLE
          putIdentityInsertOnSqlServerIfNeeded(modifySqlServerOption, connectionDestination, destinationIsSqlServer, tableSourceName, "ON");

          // READ AND WRITE
          PrepareCopyTable prepareCopyTable = new PrepareCopyTable(tableSource, tableDest, commitSize);
          prepareCopyTable.makeToolsAndStartCopy(connectionSource, connectionDestination);

          // RESET SEQUENCE
          SequenceReseter sequenceReseter = new SequenceReseter(tableSourceName, cdDest);
          sequenceReseter.execute();

          //SQL SERVER DESTINATION OPTION : PUT IDENTITY_INSERT AT off FOR THE CURRENT TABLE
          putIdentityInsertOnSqlServerIfNeeded(modifySqlServerOption, connectionDestination, destinationIsSqlServer, tableSourceName, "OFF");
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
      throw new SqlDbCopyException("Problem when do loop for tables in LoopByTable at TABLE : " + databaseSource.getTableName(indexTable), e);
    } finally {
      closer.closeResultSet(resultSetCountDest);
      closer.closeStatement(statementCountDest);
      closer.closeConnection(connectionSource);
      closer.closeConnection(connectionDestination);
    }
  }

  private void putIdentityInsertOnSqlServerIfNeeded(ModifySqlServerOption modifySqlServerOption, Connection connectionDestination, boolean destinationIsSqlServer, String tableSourceName, String onOrOff) {
    if (destinationIsSqlServer) {
      try {
        modifySqlServerOption.modifyIdentityInsert(connectionDestination, tableSourceName, onOrOff);
      } catch (SQLException noIdentityOnTable) {
        LOGGER.warn("No identity on table, skipping PUT IDENTITY_INSERT");
      }
    }
  }
}

