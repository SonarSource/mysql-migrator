/*
 * Copyright (C) 2013 SonarSource SA
 * All rights reserved
 * mailto:contact AT sonarsource DOT com
 */
package com.sonar.dbcopy;

import org.h2.jdbcx.JdbcConnectionPool;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DatabaseUtils {

  private BddConnecter bddConnecter;
  private Connection connection;
  private PreparedStatement preparedStatement;
  private Bdd bdd;
  private List<Table> tableList;

  public DatabaseUtils() { }

  public void makeDatabaseH2(String tableName) throws SQLException, ClassNotFoundException {

    /* CREATE DATABASE */
    JdbcConnectionPool.create("jdbc:h2:mem:"+tableName+";DB_CLOSE_DELAY=-1", "sonar", "sonar");

    /* CONNECTION TO DATABASE */
    bddConnecter = new BddConnecter();
    bddConnecter.doOnlyDestinationConnection("org.h2.Driver", "jdbc:h2:mem:sonar", "sonar", "sonar");
    connection = bddConnecter.getConnectionDest();

    /* PREPARE STATEMENT TO CREATE TABLES */
    preparedStatement = connection.prepareStatement("DROP TABLE IF EXISTS table_for_test");
    preparedStatement.executeUpdate();
    preparedStatement = connection.prepareStatement("DROP TABLE IF EXISTS empty_table_for_test");
    preparedStatement.executeUpdate();
    preparedStatement = connection.prepareStatement("CREATE TABLE table_for_test (COLUMNINTEGER integer PRIMARY KEY, COLUMNSTRING varchar(50), COLUMNTIMESTAMP timestamp)");
    preparedStatement.executeUpdate();
    preparedStatement = connection.prepareStatement("CREATE TABLE empty_table_for_test (ID integer PRIMARY KEY, COLSTRING varchar(50), COLTIMESTAMP timestamp)");
    preparedStatement.executeUpdate();

    System.out.println();
  }
  public void insertDatasInH2() throws SQLException {

    /* PREPARE STATEMENT TO INSERT DATAS */
    String stringToInsert = "INSERT INTO table_for_test (COLUMNINTEGER , COLUMNSTRING , COLUMNTIMESTAMP ) VALUES (?,?,?)";
    preparedStatement = connection.prepareStatement(stringToInsert);

    /* CREATE DATAS */
    Object idForColumnInteger = 1;
    Object stringForColumnString = "This is a first string for test";
    java.util.Date date= new java.util.Date();
    Object timestampForColumnTimestamp = new Timestamp(123456);

    /* INSERT A FIRST ROW OF DATAS IN DATABASE */
    preparedStatement.setObject(1,idForColumnInteger);
    preparedStatement.setObject(2,stringForColumnString);
    preparedStatement.setObject(3,timestampForColumnTimestamp);
    preparedStatement.executeUpdate();

    /* MODIFY DATAS FOR SECOND ROW */
    idForColumnInteger=2;
    stringForColumnString = "This is a second string for test";
    Object timestampForColumnTimestamp2= new Timestamp(456789);

    /* INSERT A SECOND ROW OF DATAS IN DATABASE */
    preparedStatement.setObject(1,idForColumnInteger);
    preparedStatement.setObject(2,stringForColumnString);
    preparedStatement.setObject(3,timestampForColumnTimestamp2);
    preparedStatement.executeUpdate();
  }
  public Bdd makeBddJavaObject(){
    bdd = new Bdd("sonar");

    Table table1 = new Table("table_for_test");
    Table table2 = new Table(("empty_table_for_test"));

    tableList =new ArrayList<Table>();
    tableList.add(table1);
    tableList.add(table2);

    bdd.setBddTables(tableList);

    return bdd;
  }
  public void addDatasToBddJavaObject(){
    tableList.get(0).addOneColumnToTable("COLUMNINTEGER");
    tableList.get(0).addOneColumnToTable("COLUMNSTRING");
    tableList.get(0).addOneColumnToTable("COLUMNTIMESTAMP");
    tableList.get(1).addOneColumnToTable("ID");
    tableList.get(1).addOneColumnToTable("COLSTRING");
    tableList.get(1).addOneColumnToTable("COLTIMESTAMP");

    tableList.get(0).getColumns().get(0).addDataObjectInColumn(1);
    tableList.get(0).getColumns().get(0).addDataObjectInColumn(2);
    tableList.get(0).getColumns().get(1).addDataObjectInColumn("This is a first string for test");
    tableList.get(0).getColumns().get(1).addDataObjectInColumn("This is a second string for test");
    tableList.get(0).getColumns().get(2).addDataObjectInColumn(new Timestamp(123456));
    tableList.get(0).getColumns().get(2).addDataObjectInColumn(new Timestamp(456789));
  }
  public Statement getStatementFromH2 () throws SQLException, ClassNotFoundException {
    bddConnecter = new BddConnecter();
    bddConnecter.doSourceConnectionAndStatement("org.h2.Driver", "jdbc:h2:mem:sonar", "sonar", "sonar");
    Statement statement = bddConnecter.getStatementSource();
    return statement;
  }
  public Connection getConnectionFromH2() throws SQLException, ClassNotFoundException {
    bddConnecter = new BddConnecter();
    bddConnecter.doOnlyDestinationConnection("org.h2.Driver", "jdbc:h2:mem:sonarToWrite", "sonar", "sonar");
    connection = bddConnecter.getConnectionDest();
    return connection;
  }
}
