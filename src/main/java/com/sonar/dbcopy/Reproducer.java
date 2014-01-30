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
  private Database database;

  public Reproducer(ConnecterDatas cdSource, ConnecterDatas cdDest, Database database) {
    this.cdSource = cdSource;
    this.cdDest = cdDest;
    this.database = database;
  }

  public void execute() {
    Closer closer = new Closer("Reproducer");
    ModifySqlServerOption modifySqlServerOption = null;
    boolean destinationIsSqlServer;

    Connection connectionSource = new Connecter().doConnection(cdSource);
    Connection connectionDestination = new Connecter().doConnection(cdDest);

    try {
      String urlBeginning =  connectionDestination.getMetaData().getURL().substring(0, 7);
      destinationIsSqlServer = "jdbc:jt".equals(urlBeginning);

      connectionDestination.setAutoCommit(false);

      if (destinationIsSqlServer) {
        modifySqlServerOption = new ModifySqlServerOption();
      }

      for (int indexTable = 0; indexTable < database.getNbTables(); indexTable++) {

        Table table = database.getTable(indexTable);
        String tableName = table.getName();
        int nbColInTable = table.getNbColumns();
        int nbRowsInTable = table.getNbRows();

        if (destinationIsSqlServer) {
          modifySqlServerOption.modifyIdentityInsert(connectionDestination, tableName, "ON");
        }

        ListColumnsAsString lcas = new ListColumnsAsString(table);
        String sqlRequest = "INSERT INTO " + tableName + " (" + lcas.makeColumnString() + ") VALUES(" + lcas.makeQuestionMarkString() + ")";

        LOGGER.info("START COPY IN : " + indexTable + "   " + tableName + ".");
        LoopWriter loopWriter = new LoopWriter(nbColInTable, indexTable, tableName, nbRowsInTable, sqlRequest);
        loopWriter.readAndWrite(connectionSource, connectionDestination);
        LOGGER.info("DATA COPIED IN : " + indexTable + "   " + tableName + ".");

        SequenceReseter sequenceReseter = new SequenceReseter(urlBeginning,tableName,connectionDestination);
        sequenceReseter.execute();

        if (destinationIsSqlServer) {
          modifySqlServerOption.modifyIdentityInsert(connectionDestination, tableName, "OFF");
        }
      }
    } catch (SQLException e) {
      throw new DbException("Problem when reading datas from source in Reproducer", e);
    } finally {
      closer.closeConnection(connectionSource);
      closer.closeConnection(connectionDestination);
      LOGGER.info("EveryThing is finally closed in Reproducer.");
    }
  }
}
