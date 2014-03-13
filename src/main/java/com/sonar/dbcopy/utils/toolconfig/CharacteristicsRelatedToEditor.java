/*
 * Copyright (C) 2013 SonarSource SA
 * All rights reserved
 * mailto:contact AT sonarsource DOT com
 */

package com.sonar.dbcopy.utils.toolconfig;

import java.sql.*;

public class CharacteristicsRelatedToEditor {

  public String getSchema(DatabaseMetaData metaData) throws SQLException {
    // USED FOR metadata.getTables WHICH NEED "public" WITH POSTGRESQL AND UPPERCASE USER NAME WITH ORACLE
    String schema;
    if (isPostgresql(metaData)) {
      schema = "public";
    } else if (isOracle(metaData)) {
      schema = metaData.getUserName().toUpperCase();
    } else {
      schema = null;
    }
    return schema;
  }

  public String transfromCaseOfTableName(DatabaseMetaData metaData, String tableNameToChangeCase) throws SQLException {
    // USED FOR metadata.getColumns WHICH NEED UPPERCASE WITH ORACLE
    String tableNameToReturn;
    if (isOracle(metaData) || isH2(metaData)) {
      tableNameToReturn = tableNameToChangeCase.toUpperCase();
    } else {
      tableNameToReturn = tableNameToChangeCase.toLowerCase();
    }
    return tableNameToReturn;
  }

  public String makeDropSequenceRequest(String tableName) {
    return "DROP SEQUENCE " + tableName.toUpperCase() + "_SEQ";
  }

  public String makeAlterSequencesRequest(DatabaseMetaData metadata, String tableName, long idMaxPlusOne) throws SQLException {
    String sqlRequest;
    if (isSqlServer(metadata)) {
      sqlRequest = "dbcc checkident(" + tableName + ",reseed," + idMaxPlusOne + ");";
    } else if (isOracle(metadata)) {
      sqlRequest = "CREATE SEQUENCE " + tableName.toUpperCase() + "_SEQ INCREMENT BY 1 MINVALUE 1 START WITH " + idMaxPlusOne;
    } else if (isPostgresql(metadata)) {
      sqlRequest = "ALTER SEQUENCE " + tableName + "_id_seq RESTART WITH " + idMaxPlusOne + ";";
    } else if (isMySql(metadata)) {
      sqlRequest = "ALTER TABLE " + tableName + " AUTO_INCREMENT = " + idMaxPlusOne + ";";
    } else if (isH2(metadata)) {
      sqlRequest = "ALTER TABLE " + tableName + " ALTER COLUMN id RESTART WITH " + idMaxPlusOne + ";";
    } else {
      throw new DbException("Url does not correspond to a correct format to reset auto increment idMaxPlusOne.", new Exception());
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
      throw new DbException("Problem with sql request to select id max in Sequence Reseter at TABLE : " + tableName + ".", e);
    } finally {
      closer.closeResultSet(resultSet);
      closer.closeStatement(statement);
    }
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

  public boolean isPostgresql(DatabaseMetaData metaData) throws SQLException {
    boolean isPostgresql = false;
    String vendorUrl = metaData.getURL().substring(0, 7);
    if ("jdbc:po".equals(vendorUrl)) {
      isPostgresql = true;
    }
    return isPostgresql;
  }

  public boolean isMySql(DatabaseMetaData metaData) throws SQLException {
    boolean isMySql = false;
    String vendorUrl = metaData.getURL().substring(0, 7);
    if ("jdbc:my".equals(vendorUrl)) {
      isMySql = true;
    }
    return isMySql;
  }

  public boolean isH2(DatabaseMetaData metaData) throws SQLException {
    boolean isH2 = false;
    String vendorUrl = metaData.getURL().substring(0, 7);
    if ("jdbc:h2".equals(vendorUrl)) {
      isH2 = true;
    }
    return isH2;
  }
}
