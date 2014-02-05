/*
 * Copyright (C) 2013 SonarSource SA
 * All rights reserved
 * mailto:contact AT sonarsource DOT com
 */

package com.sonar.dbcopy;

import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.SQLException;

public class Reproducer {

  private static org.slf4j.Logger LOGGER = LoggerFactory.getLogger(org.slf4j.Logger.ROOT_LOGGER_NAME);
  private ConnecterDatas cdSource, cdDest;
  private Database databaseSource;

  public Reproducer(ConnecterDatas cdSource, ConnecterDatas cdDest, Database dbS) {
    this.cdSource = cdSource;
    this.cdDest = cdDest;
    this.databaseSource = dbS;
  }

  public void execute(Database databaseDest) {
    Closer closer = new Closer("Reproducer");
    DatabaseComparer dbComparer = new DatabaseComparer(databaseDest);
    ModifySqlServerOption modifySqlServerOption = null;
    boolean destinationIsSqlServer;
    int indexTable = 0;
    Connection connectionSource = new Connecter().doConnection(cdSource);
    Connection connectionDestination = new Connecter().doConnection(cdDest);

    try {
      String urlBeginning = connectionDestination.getMetaData().getURL().substring(0, 7);
      destinationIsSqlServer = "jdbc:jt".equals(urlBeginning);

      connectionDestination.setAutoCommit(false);
      //SQL SERVER DESTINATION OPTION
      if (destinationIsSqlServer) {
        modifySqlServerOption = new ModifySqlServerOption();
      }
      // FOR EACH TABLE:
      for (indexTable = 0; indexTable < databaseSource.getNbTables(); indexTable++) {

        // PREPARE REQUEST
        Table table = databaseSource.getTable(indexTable);
        String tableName = table.getName();


        // VERIFY IF TABLE EXISTS IN DESTINATION
        if (dbComparer.tableExistsInDestinationDatabase(tableName)) {

          int nbColInTable = table.getNbColumns();
          int nbRowsInTable = table.getNbRows();
          ListColumnsAsString lcas = new ListColumnsAsString(table);
          String sqlRequest = "INSERT INTO " + tableName + " (" + lcas.makeColumnString() + ") VALUES(" + lcas.makeQuestionMarkString() + ")";

          //SQL SERVER DESTINATION OPTION
          if (destinationIsSqlServer) {
            modifySqlServerOption.modifyIdentityInsert(connectionDestination, tableName, "ON");
          }

          // READ AND WRITE
          LOGGER.info("START COPY IN : " + indexTable + "   " + tableName + ".");
          LoopWriter loopWriter = new LoopWriter(nbColInTable, indexTable, tableName, nbRowsInTable, sqlRequest);
          loopWriter.readAndWrite(connectionSource, connectionDestination);
          LOGGER.info("DATA COPIED IN : " + indexTable + "   " + tableName + ".");

          // RESET SEQUENCE
          SequenceReseter sequenceReseter = new SequenceReseter(urlBeginning, tableName, connectionDestination);
          sequenceReseter.execute();

          //SQL SERVER DESTINATION OPTION
          if (destinationIsSqlServer) {
            modifySqlServerOption.modifyIdentityInsert(connectionDestination, tableName, "OFF");
          }
        } else {
          LOGGER.error("WARNING !! Can't WRITE in TABLE :" + tableName + " because it doesn't exist in destination database. ");
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
