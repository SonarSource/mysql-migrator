/*
 * Copyright (C) 2013 SonarSource SA
 * All rights reserved
 * mailto:contact AT sonarsource DOT com
 */
package com.sonar.dbcopy;

import org.slf4j.LoggerFactory;

import java.sql.*;

public class SequenceReseter {

  private static org.slf4j.Logger LOGGER = LoggerFactory.getLogger(org.slf4j.Logger.ROOT_LOGGER_NAME);
  private String sqlRequest, tableName;
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
      DatabaseMetaData metadata = connectionDest.getMetaData();

      String tableNameWithGoodCase = relToEditor.transfromCaseOfTableNameRelatedToEditor(metadata,tableName);
      boolean destIsOracle =  relToEditor.isOracle(metadata);

        resultSet = metadata.getPrimaryKeys(null, null, tableNameWithGoodCase);

      // if resultset is not "beforeFirst" that means there is a primary key
      if (resultSet.isBeforeFirst()) {
        closer.closeResultSet(resultSet);
        long idMaxPlusOne = relToEditor.getIdMaxPlusOne(connectionDest, tableName);
        sqlRequest = relToEditor.makeRequestRelatedToEditor(metadata, tableName, idMaxPlusOne);

        statement = connectionDest.createStatement();
        if(destIsOracle){
         statement.execute(relToEditor.makeDropSequenceRequest(tableName));
        }
        statement.execute(sqlRequest);

        LOGGER.info("SEQUENCE ADJUSTED IN : " + tableName + " at "+ idMaxPlusOne);
      }
    } catch (SQLException e) {
      throw new DbException("Problem to execute the adjustment of autoincrement  with last id + 1 :" + sqlRequest + " at TABLE : " + tableName + ".", e);
    } finally {
      closer.closeResultSet(resultSet);
      closer.closeStatement(statement);
    }
  }


}
