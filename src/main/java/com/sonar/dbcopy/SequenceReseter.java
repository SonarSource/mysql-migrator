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
    CharacteristicsRelatedToEditor chRelToEd = new CharacteristicsRelatedToEditor();
    try {
      DatabaseMetaData metadata = connectionDest.getMetaData();

      String tableNameWithGoodCase = chRelToEd.transfromCaseOfTableNameRelatedToEditor(metadata,tableName);

      resultSet = metadata.getPrimaryKeys(null, null, tableNameWithGoodCase);

      // if resultset is not null that means there is a primary key
      //System.err.println("soso ----$$$ "+resultSet.getString("PK_NAME"));

      if (resultSet.isBeforeFirst()) {
        closer.closeResultSet(resultSet);
        long idMax = chRelToEd.getIdMaxPlusOne(connectionDest, tableName);
        sqlRequest = chRelToEd.makeRequestRelatedToEditor(metadata, tableName, idMax);

        statement = connectionDest.createStatement();
        statement.execute(sqlRequest);

        LOGGER.info("SEQUENCE RESETED IN : " + tableName);
      }
    } catch (SQLException e) {
      throw new DbException("Problem to execute the reset autoincrement request with last id :" + sqlRequest + " at TABLE : " + tableName + ".", e);
    } finally {
      closer.closeResultSet(resultSet);
      closer.closeStatement(statement);
    }
  }


}
