/*
 * Copyright (C) 2013 SonarSource SA
 * All rights reserved
 * mailto:contact AT sonarsource DOT com
 */

package com.sonar.dbcopy.reproduce;

import com.sonar.dbcopy.utils.*;
import com.sonar.dbcopy.utils.objects.ConnecterDatas;
import com.sonar.dbcopy.utils.objects.Database;
import com.sonar.dbcopy.utils.objects.Table;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;

public class Reproducer {

  private static org.slf4j.Logger LOGGER = LoggerFactory.getLogger(org.slf4j.Logger.ROOT_LOGGER_NAME);
  private ConnecterDatas cdSource, cdDest;
  private Database databaseSource, databaseDest;
  private CharacteristicsRelatedToEditor chRelToEditor;

  public Reproducer(ConnecterDatas cdSource, ConnecterDatas cdDest, Database databaseSource, Database databaseDest) {
    this.cdSource = cdSource;
    this.cdDest = cdDest;
    this.databaseSource = databaseSource;
    this.databaseDest = databaseDest;
    this.chRelToEditor = new CharacteristicsRelatedToEditor();
  }

  public void execute() {
    Closer closer = new Closer("Reproducer");
    DatabaseComparer dbComparer = new DatabaseComparer();
    ModifySqlServerOption modifySqlServerOption = null;
    int indexTable = 0;
    Connection connectionSource = new Connecter().doConnection(cdSource);
    Connection connectionDestination = new Connecter().doConnection(cdDest);
    try {
      DatabaseMetaData metaDest = connectionDestination.getMetaData();
      boolean destinationIsSqlServer = chRelToEditor.isSqlServer(metaDest);

      connectionDestination.setAutoCommit(false);

      //SQL SERVER DESTINATION OPTION : TO AUTHORIZE INSERT ON TATBLE
      if (destinationIsSqlServer) {
        modifySqlServerOption = new ModifySqlServerOption();
      }
      /* ****** FOR EACH TABLE: ****** */
      for (indexTable = 0; indexTable < databaseSource.getNbTables(); indexTable++) {

        // PREPARE REQUEST
        Table tableSource = databaseSource.getTable(indexTable);
        String tableSourceName = tableSource.getName();

        // VERIFY IF TABLE EXISTS IN DESTINATION
        if (dbComparer.findTableByNameInDb(databaseDest, tableSourceName) == null) {
          LOGGER.error("WARNING !! Can't WRITE in TABLE :" + tableSourceName + " because it doesn't exist in destination database. ");
        } else {

          ListColumnsAsString lcas = new ListColumnsAsString(tableSource);
          String sqlRequest = "INSERT INTO " + tableSourceName + " (" + lcas.makeColumnString() + ") VALUES(" + lcas.makeQuestionMarkString() + ")";

          //SQL SERVER DESTINATION OPTION :  PUT IDENTITY_INSERT AT on FOR THE CURRENT TABLE
          if (destinationIsSqlServer) {
            modifySqlServerOption.modifyIdentityInsert(connectionDestination, tableSourceName, "ON");
          }

          // READ AND WRITE
          LOGGER.info("START COPY IN : " + indexTable + "   " + tableSourceName + ".");
          LoopToReadAndWrite loopToReadAndWrite = new LoopToReadAndWrite(tableSource, databaseDest.getTableByName(tableSourceName), indexTable, sqlRequest);
          loopToReadAndWrite.prepareStatementAndStartCopy(connectionSource,connectionDestination);
          LOGGER.info("DATA COPIED IN : " + indexTable + "   " + tableSourceName + ".");

          // RESET SEQUENCE
          SequenceReseter sequenceReseter = new SequenceReseter(tableSourceName, connectionDestination);
          sequenceReseter.execute();

          //SQL SERVER DESTINATION OPTION :  PUT IDENTITY_INSERT AT off FOR THE CURRENT TABLE
          if (destinationIsSqlServer) {
            modifySqlServerOption.modifyIdentityInsert(connectionDestination, tableSourceName, "OFF");
          }
        }
      }

    } catch (SQLException e) {
      throw new DbException("Problem when do loop for tables in Reproducer at TABLE : " + databaseSource.getTableName(indexTable), e);
    } finally {
      closer.closeConnection(connectionSource);
      closer.closeConnection(connectionDestination);
      LOGGER.info("EveryThing is finally closed in Reproducer.");
    }
  }
}
