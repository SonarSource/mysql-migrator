/*
 * Copyright (C) 2013 SonarSource SA
 * All rights reserved
 * mailto:contact AT sonarsource DOT com
 */
package com.sonar.dbcopy.utils;

import com.sonar.dbcopy.utils.data.Database;
import com.sonar.dbcopy.utils.toolconfig.Closer;
import com.sonar.dbcopy.utils.toolconfig.DbException;
import org.h2.jdbcx.JdbcConnectionPool;

import javax.sql.rowset.serial.SerialBlob;
import java.sql.*;

public class Utils {

  private Closer closer;

  public Utils() {
    closer = new Closer("Utils");
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
      preparedStatement = connection.prepareStatement("CREATE TABLE table_for_test (id integer NOT NULL PRIMARY KEY AUTO_INCREMENT, columnstring varchar(50), columntimestamp timestamp, columnblob blob, columnclob clob, columnboolean boolean , columntobenull varchar(5))");
      preparedStatement.executeUpdate();
      preparedStatement = connection.prepareStatement("CREATE TABLE empty_table_for_test (id integer PRIMARY KEY AUTO_INCREMENT, colstring varchar(50), coltimestamp timestamp)");
      preparedStatement.executeUpdate();

      return connection;
    } catch (SQLException e) {
      throw new DbException("Problem to make H2 for tests", e);
    } finally {
      closer.closeStatement(preparedStatement);
    }
  }

  public Connection makeFilledH2(String databaseName) {

    PreparedStatement preparedStatement = null;
    try {
      Connection connection = this.makeH2WithTables(databaseName);

      /* PREPARE STATEMENT TO INSERT DATAS */
      String stringToInsert = "INSERT INTO table_for_test (id , columnstring , columntimestamp, columnBlob , columnClob , columnBoolean  , columnTobeNull ) VALUES (?,?,?,?,?,?,?)";
      preparedStatement = connection.prepareStatement(stringToInsert);

      /* CREATE DATAS */
      Integer idForColumnId = 1;
      String stringObj = "This is a first string for test";
      Timestamp timestampObj = new Timestamp(123456);
      byte[] bytes = "first string to be convert in byte".getBytes();
      Blob blobObj = new SerialBlob(bytes);

      //char[] charObj = "first string to be convert in clob".toCharArray();
      //Clob clobObj = new SerialClob(charObj);

      boolean booleanObj = true;



      /* INSERT A FIRST ROW OF DATAS IN DATABASE */
      preparedStatement.setObject(1, idForColumnId);
      preparedStatement.setObject(2, stringObj);
      preparedStatement.setObject(3, timestampObj);
      preparedStatement.setObject(4, blobObj);
      preparedStatement.setBytes(5, bytes);
      preparedStatement.setObject(6, booleanObj);
      preparedStatement.setObject(7, null);
      preparedStatement.executeUpdate();

      /* MODIFY DATAS FOR SECOND ROW */
      idForColumnId = 2;
      stringObj = "This is a second string for test";
      timestampObj = new Timestamp(456789);
      bytes = "second string to be convert in byte".getBytes();
      blobObj = new SerialBlob(bytes);
      //charObj = "second string to be convert in clob".toCharArray();
      //clobObj = new SerialClob(charObj);
      booleanObj = false;

      /* INSERT A SECOND ROW OF DATAS IN DATABASE */
      preparedStatement.setObject(1, idForColumnId);
      preparedStatement.setObject(2, stringObj);
      preparedStatement.setObject(3, timestampObj);
      preparedStatement.setObject(4, blobObj);
      preparedStatement.setBytes(5, bytes);
      preparedStatement.setObject(6, booleanObj);
      preparedStatement.setObject(7, null);
      preparedStatement.executeUpdate();

      return connection;

    } catch (SQLException e) {
      throw new DbException("Problem to insert data in H2 for test", e);
    } catch (ClassNotFoundException e) {
      throw new DbException("Problem to insert data in H2 for test", e);
    } finally {
      closer.closeStatement(preparedStatement);
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

    database.getTable(0).addColumn(0, "id", Types.INTEGER);
    database.getTable(0).addColumn(1, "columnstring", Types.VARCHAR);
    database.getTable(0).addColumn(2, "columntimestamp", Types.TIMESTAMP);
    database.getTable(1).addColumn(0, "id", Types.SMALLINT);
    database.getTable(1).addColumn(1, "colstring", Types.VARCHAR);
    database.getTable(1).addColumn(2, "coltimestamp", Types.TIMESTAMP);

    database.getTable(0).setNbRows(2);

    return database;
  }
}
