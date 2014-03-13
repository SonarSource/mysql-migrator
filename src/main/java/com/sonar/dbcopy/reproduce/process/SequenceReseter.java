/*
 * Copyright (C) 2013 SonarSource SA
 * All rights reserved
 * mailto:contact AT sonarsource DOT com
 */
package com.sonar.dbcopy.reproduce.process;

import com.sonar.dbcopy.utils.toolconfig.CharacteristicsRelatedToEditor;
import com.sonar.dbcopy.utils.toolconfig.Closer;
import com.sonar.dbcopy.utils.toolconfig.DbException;
import org.slf4j.LoggerFactory;

import java.sql.*;

public class SequenceReseter {

  private static org.slf4j.Logger LOGGER = LoggerFactory.getLogger(org.slf4j.Logger.ROOT_LOGGER_NAME);
  private String sqlRequestToReset, tableName;
  private Connection connectionDest;

  public SequenceReseter(String tableName, Connection connectiondest) {
    this.tableName = tableName;
    this.connectionDest = connectiondest;
  }

  public void execute() {

    Closer closer = new Closer("SequenceReseter");
    Statement statement = null;
    ResultSet resultSet = null;
    CharacteristicsRelatedToEditor relToEditor = new CharacteristicsRelatedToEditor();

    try {
      DatabaseMetaData metaDest = connectionDest.getMetaData();

      String tableNameWithGoodCase = relToEditor.transfromCaseOfTableName(metaDest, tableName);
      boolean destinationIsOracle = relToEditor.isOracle(metaDest);

      resultSet = metaDest.getPrimaryKeys(null, null, tableNameWithGoodCase);

      // IF REULTSET IS NOT "beforeFirst" THAT MEANS THERE IS A PRIMARY KEY
      if (resultSet.isBeforeFirst()) {
        closer.closeResultSet(resultSet);
        long idMaxPlusOne = relToEditor.getIdMaxPlusOne(connectionDest, tableName);
        sqlRequestToReset = relToEditor.makeAlterSequencesRequest(metaDest, tableName, idMaxPlusOne);

        // THEN RESET SEQUENCE REQUEST IS EXECUTED
        statement = connectionDest.createStatement();
        if (destinationIsOracle) {
          statement.execute(relToEditor.makeDropSequenceRequest(tableName));
        }
        statement.execute(sqlRequestToReset);

        LOGGER.info("SEQUENCE ADJUSTED IN : " + tableName + " at " + idMaxPlusOne);
      }
    } catch (SQLException e) {
      throw new DbException("Problem to execute the adjustment of autoincrement  with last id +1 :" + sqlRequestToReset + " at TABLE : " + tableName + ".", e);
    } finally {
      closer.closeResultSet(resultSet);
      closer.closeStatement(statement);
    }
  }
}

