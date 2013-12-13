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
  private Database database;
  private List<Table> tableList;
  private String h2DatabaseName;

  public DatabaseUtils() {
  }

  /* H2 */
  public void makeDatabaseH2Withtables(String h2DatabaseName) throws SQLException, ClassNotFoundException {
    this.h2DatabaseName = h2DatabaseName;
    /* CREATE H2 DATABASE */
    String connectionPoolParameters = "jdbc:h2:mem:" + this.h2DatabaseName + ";DB_CLOSE_ON_EXIT=-1;";

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
  public void makeDatabaseJavaObject() {
    database = new Database();
  }

  public void addTablesToDatabaseJavaObject() {
    List<Table> tableList = new ArrayList<Table>();

    tableList.add(new Table("table_for_test"));
    tableList.add(new Table("empty_table_for_test"));

    database.setTables(tableList);
  }

  public void addColumnsToDatabaseJavaObject() {
    database.getTable(0).addColumn("COLUMNINTEGER");
    database.getTable(0).addColumn("COLUMNSTRING");
    database.getTable(0).addColumn("COLUMNTIMESTAMP");
    database.getTable(1).addColumn("ID");
    database.getTable(1).addColumn("COLSTRING");
    database.getTable(1).addColumn("COLTIMESTAMP");
  }

  public void addDatasToDatabaseJavaObject() {
    // during insertion the order is important:
    // FIRST ROW
    database.addData(0,0,8);
    database.addData(0,1,"This is a first string for test");
    database.addData(0,2,new Timestamp(123456));
    //SECOND ROW
    database.addData(0,0,5);
    database.addData(0,1,"This is a second string for test");
    database.addData(0,2,new Timestamp(456789));
  }

  public Database getJavaDatabaseFromUtils() {
    return this.database;
  }


}
