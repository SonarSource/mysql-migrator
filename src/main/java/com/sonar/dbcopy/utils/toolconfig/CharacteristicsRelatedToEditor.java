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
package com.sonar.dbcopy.utils.toolconfig;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.ResultSet;
import java.util.Locale;

public class CharacteristicsRelatedToEditor {

  private CharacteristicsRelatedToEditor() {
    // no op
  }

  public static String transfromCaseOfTableName(DatabaseMetaData metaData, String tableNameToChangeCase) throws SQLException {
    // USED FOR metadata.getColumns WHICH NEED UPPERCASE WITH ORACLE
    String tableNameToReturn;
    if (isOracle(metaData) || isH2(metaData)) {
      tableNameToReturn = tableNameToChangeCase.toUpperCase(Locale.ENGLISH);
    } else {
      tableNameToReturn = tableNameToChangeCase.toLowerCase(Locale.ENGLISH);
    }
    return tableNameToReturn;
  }

  public static String makeDropSequenceRequest(String tableName) {
    return "DROP SEQUENCE " + tableName.toUpperCase(Locale.ENGLISH) + "_SEQ";
  }

  public static String makeAlterSequencesRequest(Connection connection, DatabaseMetaData metadata, String tableName, long idMaxPlusOne) throws SQLException {
    String sqlRequest;
    if (isSqlServer(metadata)) {
      sqlRequest = "dbcc checkident(" + tableName + ",reseed," + idMaxPlusOne + ");";
    } else if (isOracle(metadata)) {
      sqlRequest = "CREATE SEQUENCE " + tableName.toUpperCase(Locale.ENGLISH) + "_SEQ INCREMENT BY 1 MINVALUE 1 START WITH " + idMaxPlusOne;
    } else if (isPostgresql(metadata)) {
      String sequenceName = selectPostgresIdSequenceForTable(connection, tableName);
      sqlRequest = "ALTER SEQUENCE " + sequenceName + " RESTART WITH " + idMaxPlusOne + ";";
    } else if (isMySql(metadata)) {
      sqlRequest = "ALTER TABLE " + tableName + " AUTO_INCREMENT = " + idMaxPlusOne + ";";
    } else if (isH2(metadata)) {
      sqlRequest = "ALTER TABLE " + tableName + " ALTER COLUMN id RESTART WITH " + idMaxPlusOne + ";";
    } else {
      throw new MessageException("Url " + metadata.getURL() + " does not correspond to a correct format to reset auto increment.");
    }
    return sqlRequest;
  }

  private static String selectPostgresIdSequenceForTable(Connection connection, String tableName) throws SQLException {
    try (PreparedStatement pst = connection.prepareStatement("SELECT pg_get_serial_sequence('" + tableName + "', 'id')");
      ResultSet rs = pst.executeQuery()) {
      if (rs.next()) {
        return rs.getString(1);
      } else {
        throw new IllegalStateException("Could not find ID generation sequence for table " + tableName);
      }
    }
  }

  public static long getIdMaxPlusOne(Connection connectionDest, String tableName) {
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
      throw new SqlDbCopyException("Problem with sql request to select id max in Sequence Reseter at TABLE : " + tableName + ".", e);
    } finally {
      closer.closeResultSet(resultSet);
      closer.closeStatement(statement);
    }
  }

  public static String giveDriverWithUrlFromUser(String url) {
    String driverAsString;
    String urlBeginning = url.substring(0, 7);
    if ("jdbc:my".equals(urlBeginning)) {
      driverAsString = "com.mysql.jdbc.Driver";
    } else if ("jdbc:or".equals(urlBeginning)) {
      driverAsString = "oracle.jdbc.OracleDriver";
    } else if ("jdbc:h2".equals(urlBeginning)) {
      driverAsString = "org.h2.Driver";
    } else if ("jdbc:po".equals(urlBeginning)) {
      driverAsString = "org.postgresql.Driver";
    } else if ("jdbc:sq".equals(urlBeginning)) {
      // reference: https://docs.microsoft.com/en-us/sql/connect/jdbc/using-the-jdbc-driver
      driverAsString = "com.microsoft.sqlserver.jdbc.SQLServerDriver";
    } else {
      throw new MessageException("Url " + url + " does not correspond to a correct format to get the good jdbc driver.");
    }
    return driverAsString;
  }

  public static boolean isSqlServer(DatabaseMetaData metaData) throws SQLException {
    String vendorUrl = metaData.getURL();
    if (vendorUrl == null) {
      throw new IllegalStateException("can't get database url");
    } else {
      return vendorUrl.startsWith("jdbc:sqlserver");
    }
  }

  public static boolean isOracle(DatabaseMetaData metaData) throws SQLException {
    boolean isOracle = false;
    String vendorUrl = metaData.getURL().substring(0, 7);
    if ("jdbc:or".equals(vendorUrl)) {
      isOracle = true;
    }
    return isOracle;
  }

  public static boolean isPostgresql(DatabaseMetaData metaData) throws SQLException {
    boolean isPostgresql = false;
    String vendorUrl = metaData.getURL().substring(0, 7);
    if ("jdbc:po".equals(vendorUrl)) {
      isPostgresql = true;
    }
    return isPostgresql;
  }

  public static boolean isMySql(DatabaseMetaData metaData) throws SQLException {
    boolean isMySql = false;
    String vendorUrl = metaData.getURL().substring(0, 7);
    if ("jdbc:my".equals(vendorUrl)) {
      isMySql = true;
    }
    return isMySql;
  }

  public static boolean isH2(DatabaseMetaData metaData) throws SQLException {
    boolean isH2 = false;
    String vendorUrl = metaData.getURL().substring(0, 7);
    if ("jdbc:h2".equals(vendorUrl)) {
      isH2 = true;
    }
    return isH2;
  }
}
