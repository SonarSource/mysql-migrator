/*
 * SonarQube MySQL Database Migrator
 * Copyright (C) 2019-2021 SonarSource SA
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
package org.sonarsource.sqdbmigrator.migrator;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Properties;
import java.util.function.Function;

/**
 * A wrapper class around a database Connection,
 * providing common operations in a backend-agnostic way.
 */
public abstract class Database implements AutoCloseable {

  private static final int DEFAULT_FETCH_SIZE = 10;

  final Connection connection;

  protected Database(Connection connection) {
    this.connection = connection;
  }

  String canonicalTableName(String tableName) {
    return tableName.toLowerCase(Locale.ENGLISH);
  }

  abstract void resetSequence(String tableName, long value) throws SQLException;

  public void setIdentityInsert(String tableName, boolean value) throws SQLException {
    // noop in general; MSSQL-only feature
  }

  int getFetchSize() {
    return DEFAULT_FETCH_SIZE;
  }

  /**
   * Create a new connection, and wrap it in a Database instance.
   * Call this using try-with-resources, to ensure the wrapped connection will get closed.
   */
  static Database create(ConnectionConfig config) throws SQLException {
    Function<Connection, Database> factory = computeDatabaseFactory(config);

    Properties properties = new Properties();
    if (config.username != null) {
      properties.setProperty("user", config.username);
    }
    if (config.password != null) {
      properties.setProperty("password", config.password);
    }
    Connection connection = createConnection(config.url, properties);
    connection.setAutoCommit(false);

    return factory.apply(connection);
  }

  private static Connection createConnection(String url, Properties properties) throws SQLException {
    if (!url.startsWith("jdbc:oracle:")) {
      return DriverManager.getConnection(url, properties);
    }
    try {
      return DriverManager.getConnection(url, properties);
    } catch (SQLException e) {
      String message = e.getMessage();
      String additionalMessage = "Make sure the JDBC Oracle driver is copied to the lib folder. The file must be named 'oracle.jar'";
      throw new DatabaseException(message + "\n" + additionalMessage);
    }
  }

  private static Function<Connection, Database> computeDatabaseFactory(ConnectionConfig config) {
    if (config.url.startsWith("jdbc:mysql:")) {
      return MysqlDatabase::new;
    }

    if (config.url.startsWith("jdbc:postgresql:")) {
      return PostgresqlDatabase::new;
    }

    if (config.url.startsWith("jdbc:sqlserver:")) {
      return SqlServerDatabase::new;
    }

    if (config.url.startsWith("jdbc:oracle:")) {
      return OracleDatabase::new;
    }

    if (config.url.startsWith("jdbc:h2:")) {
      return H2Database::new;
    }

    throw new DatabaseException("Unsupported database: %s", config.url);
  }

  public Connection getConnection() {
    return connection;
  }

  private static class MysqlDatabase extends Database {
    private MysqlDatabase(Connection connection) {
      super(connection);
    }

    @Override
    protected void resetSequence(String tableName, long value) throws SQLException {
      String sql = String.format("ALTER TABLE %s AUTO_INCREMENT = %s", tableName, value);
      executeUpdate(sql);
    }

    @Override
    int getFetchSize() {
      // Without this, MySQL driver would load all records at once
      // See also https://stackoverflow.com/questions/26046234/is-there-a-mysql-jdbc-that-will-respect-fetchsize
      return Integer.MIN_VALUE;
    }
  }

  private static class PostgresqlDatabase extends Database {
    private PostgresqlDatabase(Connection connection) {
      super(connection);
    }

    @Override
    protected void resetSequence(String tableName, long value) throws SQLException {
      String sql = String.format("ALTER SEQUENCE %s RESTART WITH %s", selectSequenceId(tableName), value);
      executeUpdate(sql);
    }

    private String selectSequenceId(String tableName) throws SQLException {
      String sql = String.format("SELECT pg_get_serial_sequence('%s', 'id')", tableName);
      try (PreparedStatement pst = connection.prepareStatement(sql);
        ResultSet rs = pst.executeQuery()) {
        if (rs.next()) {
          return rs.getString(1);
        } else {
          throw new DatabaseException("Could not find sequence ID for table: %s", tableName);
        }
      }
    }
  }

  private static class SqlServerDatabase extends Database {
    private SqlServerDatabase(Connection connection) {
      super(connection);
    }

    @Override
    protected void resetSequence(String tableName, long value) throws SQLException {
      String sql = String.format("dbcc checkident(%s,reseed,%s)", tableName, value);
      executeUpdate(sql);
    }

    @Override
    public void setIdentityInsert(String tableName, boolean value) throws SQLException {
      String sql = String.format("SET IDENTITY_INSERT %s %s", tableName, value ? "ON" : "OFF");
      executeUpdate(sql);
    }
  }

  private static class OracleDatabase extends Database {
    private OracleDatabase(Connection connection) {
      super(connection);
    }

    @Override
    protected String canonicalTableName(String tableName) {
      return tableName.toUpperCase(Locale.ENGLISH);
    }

    @Override
    protected void resetSequence(String tableName, long value) throws SQLException {
      String canonicalTableName = canonicalTableName(tableName);

      String dropSequenceSql = String.format("DROP SEQUENCE %s_SEQ", canonicalTableName);
      executeUpdate(dropSequenceSql);

      String createSequenceSql = String.format(
        "CREATE SEQUENCE %s_SEQ INCREMENT BY 1 MINVALUE 1 START WITH %s",
        canonicalTableName, value);
      executeUpdate(createSequenceSql);
    }
  }

  private static class H2Database extends Database {
    private H2Database(Connection connection) {
      super(connection);
    }

    @Override
    protected String canonicalTableName(String tableName) {
      return tableName.toUpperCase(Locale.ENGLISH);
    }

    @Override
    protected void resetSequence(String tableName, long value) {
      // nothing to do, H2 seems to pick up correct value automatically
    }
  }

  public void executeUpdate(String sql) throws SQLException {
    try (Statement statement = connection.createStatement()) {
      statement.executeUpdate(sql);
    }
  }

  void truncateTable(String tableName) throws SQLException {
    String truncateTableSql = String.format("truncate table %s", tableName);
    executeUpdate(truncateTableSql);
  }

  public List<String> getTables() throws SQLException {
    DatabaseMetaData metaData = connection.getMetaData();

    List<String> tables = new ArrayList<>();
    try (ResultSet rs = metaData.getTables(connection.getCatalog(), connection.getSchema(), "%", new String[] {"TABLE"})) {
      while (rs.next()) {
        tables.add(rs.getString("TABLE_NAME").toLowerCase(Locale.ENGLISH));
      }
    }

    return tables;
  }

  public List<String> getColumnNames(String tableName) throws SQLException {
    String schema = connection.getSchema();
    try (ResultSet rs = connection.getMetaData().getColumns(null, schema, canonicalTableName(tableName), "%")) {
      List<String> columnNames = new ArrayList<>();
      while (rs.next()) {
        columnNames.add(rs.getString("COLUMN_NAME"));
      }
      return columnNames;
    }
  }

  public long countRows(String tableName) throws SQLException {
    return queryForLong(String.format("select count(*) from %s", tableName));
  }

  public long queryForLong(String sql) throws SQLException {
    try (PreparedStatement preparedStatement = connection.prepareStatement(sql); ResultSet rs = preparedStatement.executeQuery()) {
      return rs.next() ? rs.getLong(1) : 0;
    }
  }

  boolean tableHasIdColumn(String tableName) throws SQLException {
    try (ResultSet rs = connection.getMetaData().getPrimaryKeys(null, null, canonicalTableName(tableName))) {
      while (rs.next()) {
        if ("id".equalsIgnoreCase(rs.getString("COLUMN_NAME"))) {
          return true;
        }
      }
      return false;
    }
  }

  long selectMaxId(String tableName) throws SQLException {
    return queryForLong(String.format("select max(id) from %s", tableName));
  }

  public int getSchemaVersion() {
    String sql = "select version from schema_migrations";
    String versionString = null;
    int version = -1;

    try (PreparedStatement statement = connection.prepareStatement(sql);
         ResultSet rs = statement.executeQuery()) {
      // note: for lack of a standard way to sort int-valued strings, manually computing max value
      while (rs.next()) {
        versionString = rs.getString(1);
        version = Math.max(version, Integer.parseInt(versionString));
      }
    } catch (SQLException e) {
      throw new DatabaseException("Could not select version from schema_migrations. %s", e.getMessage());
    } catch (NumberFormatException e) {
      throw new DatabaseException("Malformed version: '%s'; expected integer value", versionString);
    }

    if (version < 0) {
      throw new DatabaseException("The schema_migrations table must not be empty");
    }

    return version;
  }

  @Override
  public void close() throws SQLException {
    this.connection.close();
  }

  public static class DatabaseException extends RuntimeException {
    DatabaseException(String format, Object... args) {
      super(String.format(format, args));
    }
  }
}
