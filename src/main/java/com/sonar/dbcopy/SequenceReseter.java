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
  private String sqlRequest, tableName, urlbeginning;
  private Connection connectionDest;

  public SequenceReseter(String urlbeginning, String tableName, Connection connectiondest) {
    this.tableName = tableName;
    this.connectionDest = connectiondest;
    this.urlbeginning = urlbeginning;
  }

  public void execute() {

    Closer closer = new Closer("SequenceReseter");
    Statement statement = null;
    ResultSet resultSet = null;

    try {
      DatabaseMetaData dm = connectionDest.getMetaData();
      resultSet = dm.getPrimaryKeys(null, null, tableName);
      // if resultset is not null that means there is a primary key
      if (resultSet.isBeforeFirst()) {
        closer.closeResultSet(resultSet);
        makeRequestRelatedToVendor();

        statement = connectionDest.createStatement();
        statement.execute(sqlRequest);

        LOGGER.info("SEQUENCE RESETED IN : " + tableName);
      }
    } catch (SQLException e) {
      throw new DbException("Problem to execute the reset autoincrement request with last id :"+sqlRequest+" at TABLE : "+tableName+".", e);
    } finally {
      closer.closeResultSet(resultSet);
      closer.closeStatement(statement);
    }
  }

  private void makeRequestRelatedToVendor() {
    if ("jdbc:jt".equals(urlbeginning)) {
      sqlRequest = "dbcc checkident(" + tableName + ",reseed," + getIdMaxPlusOne() + ");";
    } else if ("jdbc:or".equals(urlbeginning)) {
      sqlRequest = "ALTER SEQUENCE " + tableName + "_id_seq  MINVALUE " + getIdMaxPlusOne();
    } else if ("jdbc:po".equals(urlbeginning)) {
      sqlRequest = "ALTER SEQUENCE " + tableName + "_id_seq RESTART WITH " + getIdMaxPlusOne() + ";";
    } else if ("jdbc:my".equals(urlbeginning)) {
      sqlRequest = "ALTER TABLE " + tableName + " AUTO_INCREMENT = " + getIdMaxPlusOne() + ";";
    } else {
      throw new DbException("URL : "+urlbeginning+" does not correspond to a correct format to reset auto increment id.", new Exception());
    }
  }

  private long getIdMaxPlusOne() {
    Closer closer = new Closer("getIdMax");
    Statement statement = null;
    ResultSet resultSet = null;
    long idMaxToReturn = 1;
    try {
      statement = connectionDest.createStatement();
      resultSet = statement.executeQuery("SELECT max(id) FROM " + tableName);
      while (resultSet.next()) {
        idMaxToReturn = resultSet.getLong(1);
      }
      return idMaxToReturn + 1;
    } catch (SQLException e) {
      throw new DbException("Problem with sql request to select id max in Sequence Reseter at TABLE : "+tableName+".", new Exception());
    } finally {
      closer.closeResultSet(resultSet);
      closer.closeStatement(statement);
    }
  }

}
