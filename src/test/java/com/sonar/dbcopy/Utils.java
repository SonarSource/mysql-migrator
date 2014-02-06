/*
 * Copyright (C) 2013 SonarSource SA
 * All rights reserved
 * mailto:contact AT sonarsource DOT com
 */
package com.sonar.dbcopy;

import org.h2.jdbcx.JdbcConnectionPool;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;

public class Utils {

  public Utils() {
  }

  /* H2 */
  public Connection makeH2(String dataBaseName) throws SQLException, ClassNotFoundException {

    String connectionPoolParameters = "jdbc:h2:mem:" + dataBaseName + ";DB_CLOSE_ON_EXIT=-1;";
    JdbcConnectionPool jdbcConnectionPool = JdbcConnectionPool.create(connectionPoolParameters, "sonar", "sonar");
    Connection connection = jdbcConnectionPool.getConnection();
    return connection;
  }

  private Connection makeH2WithTables(String dataBaseName) throws SQLException, ClassNotFoundException {

    Connection connection = makeH2(dataBaseName);

    PreparedStatement preparedStatement = null;
    try {
      /* PREPARE STATEMENT TO CREATE TABLES */
      preparedStatement = connection.prepareStatement("DROP TABLE IF EXISTS table_for_test");
      preparedStatement.executeUpdate();
      preparedStatement = connection.prepareStatement("DROP TABLE IF EXISTS empty_table_for_test");
      preparedStatement.executeUpdate();
      preparedStatement = connection.prepareStatement("CREATE TABLE table_for_test (columninteger integer NOT NULL PRIMARY KEY, columnstring varchar(50), columntimestamp timestamp)");
      preparedStatement.executeUpdate();
      preparedStatement = connection.prepareStatement("CREATE TABLE empty_table_for_test (id integer PRIMARY KEY, colstring varchar(50), coltimestamp timestamp)");
      preparedStatement.executeUpdate();

      return connection;
    } catch (SQLException e) {
      throw new DbException("Problem to make H2 for tests", e);
    } finally {
      preparedStatement.close();
    }
  }

  public Connection makeFilledH2(String databaseName) {

    PreparedStatement preparedStatement = null;
    try {
      Connection connection = this.makeH2WithTables(databaseName);

      /* PREPARE STATEMENT TO INSERT DATAS */
      String stringToInsert = "INSERT INTO table_for_test (columninteger , columnstring , columntimestamp ) VALUES (?,?,?)";
      preparedStatement = connection.prepareStatement(stringToInsert);

      /* CREATE DATAS */
      Object idForColumnInteger = 8;
      Object stringForColumnString = "This is a first string for test";
      Object timestampForColumnTimestamp = new Timestamp(123456);

      /* INSERT A FIRST ROW OF DATAS IN DATABASE */
      preparedStatement.setObject(1, idForColumnInteger);
      preparedStatement.setObject(2, stringForColumnString);
      preparedStatement.setObject(3, timestampForColumnTimestamp);
      preparedStatement.executeUpdate();

      /* MODIFY DATAS FOR SECOND ROW */
      idForColumnInteger = 5;
      stringForColumnString = "This is a second string for test";
      Object timestampForColumnTimestamp2 = new Timestamp(456789);

      /* INSERT A SECOND ROW OF DATAS IN DATABASE */
      preparedStatement.setObject(1, idForColumnInteger);
      preparedStatement.setObject(2, stringForColumnString);
      preparedStatement.setObject(3, timestampForColumnTimestamp2);
      preparedStatement.executeUpdate();

      return connection;

    } catch (SQLException e) {
      throw new DbException("Problem to insert data in H2 for test", e);
    } catch (ClassNotFoundException e) {
      throw new DbException("Problem to insert data in H2 for test", e);
    } finally {
      try {
        preparedStatement.close();
      } catch (SQLException e) {
        throw new DbException("Problem to iclose h2 connection", e);
      }
    }
  }

  public Connection makeEmptyH2(String databaseName) {
    try {
      return this.makeH2WithTables(databaseName);
    } catch (SQLException e) {
      throw new DbException("Problem to insert data in H2 for test", e);
    } catch (ClassNotFoundException e) {
      throw new DbException("Problem to insert data in H2 for test", e);
    }
  }

  /* JAVA OBJECTS */
  public Database makeDatabase() {
    Database database = new Database();
    database.addTable("table_for_test");
    database.addTable("empty_table_for_test");

    database.getTable(0).addColumn("columninteger");
    database.getTable(0).addColumn("columnstring");
    database.getTable(0).addColumn("columntimestamp");
    database.getTable(1).addColumn("id");
    database.getTable(1).addColumn("colstring");
    database.getTable(1).addColumn("coltimestamp");

    database.getTable(0).setNbRows(2);

    return database;
  }
}
