/*
 * Copyright (C) 2013 SonarSource SA
 * All rights reserved
 * mailto:contact AT sonarsource DOT com
 */

package com.sonar.dbcopy;

import java.sql.*;

public class CharacteristicsRelatedToEditor {

  public String getSchema(DatabaseMetaData metaData) throws SQLException {
    // USED FOR metadata.getTables WHICH NEED "public" WITH POSTGRESQL AND UPPERCASE  USER NAME WITH ORACLE
    String vendorUrl = metaData.getURL().substring(0, 7);
    String schema;
    if ("jdbc:po".equals(vendorUrl)) {
      schema = "public";
    } else if ("jdbc:or".equals(vendorUrl)) {
      schema = metaData.getUserName().toUpperCase();
    } else {
      schema = null;
    }
    return schema;
  }

  public String transfromCaseOfTableNameRelatedToEditor(DatabaseMetaData metaData, String tableNameToChangeCase) throws SQLException {
    // USED FOR metadata.getColumns WHICH NEED UPPERCASE WITH ORACLE
    String vendorUrl = metaData.getURL().substring(0, 7);
    String tableNameToReturn;
    if ("jdbc:or".equals(vendorUrl) || "jdbc:h2".equals(vendorUrl)) {
      tableNameToReturn = tableNameToChangeCase.toUpperCase();
    } else {
      tableNameToReturn = tableNameToChangeCase.toLowerCase();
    }
    return tableNameToReturn;
  }

  public boolean isSqlServer(DatabaseMetaData metaData) throws SQLException {
    boolean isSqlServer = false;
    String vendorUrl = metaData.getURL().substring(0, 7);
    if ("jdbc:jt".equals(vendorUrl)) {
      isSqlServer = true;
    }
    return isSqlServer;
  }

  public boolean isOracle(DatabaseMetaData metaData) throws SQLException {
    boolean isOracle = false;
    String vendorUrl = metaData.getURL().substring(0, 7);
    if ("jdbc:or".equals(vendorUrl)) {
      isOracle = true;
    }
    return isOracle;
  }

  public String makeRequestRelatedToEditor(DatabaseMetaData metadata, String tableName, long idMax) throws SQLException {

    String urlbeginning = metadata.getURL().substring(0, 7);
    String sqlRequest="";

    if ("jdbc:jt".equals(urlbeginning)) {
      sqlRequest = "dbcc checkident(" + tableName + ",reseed," + idMax + ");";
    } else if ("jdbc:or".equals(urlbeginning)) {
      sqlRequest = "ALTER SEQUENCE " + tableName + "_id_seq  MINVALUE " + idMax;
    } else if ("jdbc:po".equals(urlbeginning)) {
      sqlRequest = "ALTER SEQUENCE " + tableName + "_id_seq RESTART WITH " + idMax + ";";
    } else if ("jdbc:my".equals(urlbeginning)) {
      sqlRequest = "ALTER TABLE " + tableName + " AUTO_INCREMENT = " + idMax + ";";
    } else {
      throw new DbException("URL : "+urlbeginning+" does not correspond to a correct format to reset auto increment idMax.", new Exception());
    }
    return sqlRequest;
  }

  public long getIdMaxPlusOne(Connection connectionDest, String tableName) {
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
