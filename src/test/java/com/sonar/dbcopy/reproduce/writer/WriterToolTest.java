/*
 * Copyright (C) 2013-2016 SonarSource SA
 * All rights reserved
 * mailto:contact AT sonarsource DOT com
 */
package com.sonar.dbcopy.reproduce.writer;

import com.sonar.dbcopy.utils.Utils;
import com.sonar.dbcopy.utils.toolconfig.Closer;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.sql.*;
import java.util.ArrayList;

public class WriterToolTest {
  private ArrayList<WriterTool> writerToolList;
  private Connection connection;
  private PreparedStatement preparedStatement;
  private Closer closer;

  @Before
  public void setUp() throws Exception {
    closer = new Closer("ReaderTootTest");
    Utils utils = new Utils();

    connection = utils.makeEmptyH2("ReaderTootTestDestinationDB", false);
    connection.setAutoCommit(false);
    String stringToInsert = "INSERT INTO table_for_test (id , columnstring , columntimestamp, columnBlob , columnClob , columnBoolean  , columnTobeNull ) VALUES (?,?,?,?,?,?,?)";
    preparedStatement = connection.prepareStatement(stringToInsert);

    writerToolList = new ArrayList<WriterTool>();
    writerToolList.add(new H2Writer(preparedStatement));
    writerToolList.add(new MySqlWriter(preparedStatement));
    writerToolList.add(new OracleWriter(preparedStatement));
    writerToolList.add(new PostgresqlWriter(preparedStatement));
    writerToolList.add(new SqlServerWriter(preparedStatement));
  }

  @After
  public void tearDown() throws Exception {
    closer.closeStatement(preparedStatement);
    closer.closeConnection(connection);
  }

  @Test
  public void testWriteAllColumns() throws Exception {
                           /* CREATE DATA */
    Integer idForColumnId = 1;
    String stringObj = "This is a first string for test";
    Timestamp timestampObj = new Timestamp(123456);
    byte[] bytes = "first string to be convert in byte".getBytes();
    boolean booleanObj = true;

    // USE EACH WRITERTOOLS
    for (int indexWriter = 0; indexWriter < writerToolList.size(); indexWriter++) {

      // WRITE IN DATABASE EACH TYPES OF DATA
      writerToolList.get(indexWriter).writeObject(idForColumnId, 0);
      writerToolList.get(indexWriter).writeVarchar(stringObj, 1);
      writerToolList.get(indexWriter).writeTimestamp(timestampObj, 2);
      writerToolList.get(indexWriter).writeBlob(bytes, 3);
      writerToolList.get(indexWriter).writeClob(stringObj, 4);
      writerToolList.get(indexWriter).writeBoolean(booleanObj, 5);
      writerToolList.get(indexWriter).writeWhenNull(6);
      preparedStatement.executeUpdate();
      connection.commit();

      // VERIFY WRITTING WORKED FOR EACH TYPES OF DATA
      Statement statementToVerifyFilled = connection.createStatement();
      ResultSet resultSetToVerifyFilled = statementToVerifyFilled.executeQuery("SELECT * FROM table_for_test");
      while (resultSetToVerifyFilled.next()) {
        Assert.assertEquals(idForColumnId, resultSetToVerifyFilled.getObject(1));
        Assert.assertEquals(stringObj, resultSetToVerifyFilled.getString(2));
        Assert.assertEquals(timestampObj, resultSetToVerifyFilled.getTimestamp(3));
        Assert.assertEquals(bytes.length, resultSetToVerifyFilled.getBlob(4).length());
        Assert.assertEquals(stringObj, resultSetToVerifyFilled.getString(5));
        Assert.assertEquals(booleanObj, resultSetToVerifyFilled.getBoolean(6));
        Assert.assertEquals(null, resultSetToVerifyFilled.getTimestamp(7));
      }
      closer.closeResultSet(resultSetToVerifyFilled);
      closer.closeStatement(statementToVerifyFilled);

      // TRUNCATE TABLE TO USE THE NEXT WRITERTOOL
      Statement statementToTruncate = connection.createStatement();
      statementToTruncate.execute("TRUNCATE TABLE table_for_test");
      closer.closeStatement(statementToTruncate);

      // VERIFY TRUNCATING WORKED
      Statement statementToVerifyEmpty = connection.createStatement();
      ResultSet resultSetToVerifyEmpty = statementToVerifyEmpty.executeQuery("SELECT * FROM table_for_test");
      boolean t = resultSetToVerifyEmpty.isBeforeFirst();
      Assert.assertFalse(resultSetToVerifyEmpty.isBeforeFirst());
      closer.closeResultSet(resultSetToVerifyEmpty);
      closer.closeStatement(statementToVerifyEmpty);
    }
  }
}
