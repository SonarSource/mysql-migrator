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
import java.util.ArrayList;
import java.util.List;

public class DatabaseUtils {

  private Connection connection;
  private PreparedStatement preparedStatement;
  private Bdd bdd;
  private List<Table> tableList;
  private String databaseName;

  public DatabaseUtils() {
  }

  /* H2 */
  public void makeDatabaseH2Withtables(String databaseName) throws SQLException, ClassNotFoundException {
    this.databaseName = databaseName;
    /* CREATE H2 DATABASE */
    String connectionPoolParameters = "jdbc:h2:mem:" + this.databaseName + ";DB_CLOSE_ON_EXIT=-1;";

    JdbcConnectionPool jdbcConnectionPool = JdbcConnectionPool.create(connectionPoolParameters, "sonar", "sonar");

    /* CONNECT TO H2 DATABASE */
    connection = jdbcConnectionPool.getConnection();


    /* PREPARE STATEMENT TO CREATE TABLES */
    preparedStatement = connection.prepareStatement("DROP TABLE IF EXISTS table_for_test");
    preparedStatement.executeUpdate();
    preparedStatement = connection.prepareStatement("DROP TABLE IF EXISTS empty_table_for_test");
    preparedStatement.executeUpdate();
    preparedStatement = connection.prepareStatement("CREATE TABLE table_for_test (COLUMNINTEGER integer NOT NULL PRIMARY KEY, COLUMNSTRING varchar(50), COLUMNTIMESTAMP timestamp)");
    preparedStatement.executeUpdate();
    preparedStatement = connection.prepareStatement("CREATE TABLE empty_table_for_test (ID integer PRIMARY KEY, COLSTRING varchar(50), COLTIMESTAMP timestamp)");
    preparedStatement.executeUpdate();
  }

  public void insertDatasInH2Tables() throws SQLException {

    /* PREPARE STATEMENT TO INSERT DATAS */
    String stringToInsert = "INSERT INTO table_for_test (COLUMNINTEGER , COLUMNSTRING , COLUMNTIMESTAMP ) VALUES (?,?,?)";
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
  }

  public Connection getConnectionFromH2() throws SQLException, ClassNotFoundException {
    return this.connection;
  }

  /* JAVA DATABASE */
  public void makeBddJavaObject() {
    bdd = new Bdd("sonar");
  }

  public void addTablesToBddJavaObject() {
    tableList = new ArrayList<Table>();

    tableList.add(new Table("table_for_test"));
    tableList.add(new Table("empty_table_for_test"));

    bdd.setBddTables(tableList);
  }

  public void addColumnsToBddJavaObject() {
    tableList.get(0).addOneColumnToTable("COLUMNINTEGER");
    tableList.get(0).addOneColumnToTable("COLUMNSTRING");
    tableList.get(0).addOneColumnToTable("COLUMNTIMESTAMP");
    tableList.get(1).addOneColumnToTable("ID");
    tableList.get(1).addOneColumnToTable("COLSTRING");
    tableList.get(1).addOneColumnToTable("COLTIMESTAMP");
  }

  public void addDatasToBddJavaObject() {
    tableList.get(0).getColumns().get(0).addDataObjectInColumn(1);
    tableList.get(0).getColumns().get(0).addDataObjectInColumn(2);
    tableList.get(0).getColumns().get(1).addDataObjectInColumn("This is a first string for test");
    tableList.get(0).getColumns().get(1).addDataObjectInColumn("This is a second string for test");
    tableList.get(0).getColumns().get(2).addDataObjectInColumn(new Timestamp(123456));
    tableList.get(0).getColumns().get(2).addDataObjectInColumn(new Timestamp(456789));
  }

  public Bdd getJavaBddFromUtils() {
    return this.bdd;
  }


}
