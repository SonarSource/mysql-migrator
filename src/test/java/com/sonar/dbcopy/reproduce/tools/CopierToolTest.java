/*
 * Copyright (C) 2013 SonarSource SA
 * All rights reserved
 * mailto:contact AT sonarsource DOT com
 */

package com.sonar.dbcopy.reproduce.tools;

import com.sonar.dbcopy.utils.Utils;
import com.sonar.dbcopy.utils.Closer;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.sql.*;
import java.util.ArrayList;

public class CopierToolTest {

  private ArrayList<CopierTool> copierToolList;
  private Closer closer;
  private PreparedStatement prStatementDest;
  private Statement statementSource, statementDest;
  private Connection connectionSource, connectionDest;
  private ResultSet resultSetSource,resultSetDest;

  @Before
  public void setUp() throws Exception {
    closer = new Closer("CopierToolTest");
    Utils utils = new Utils();
    connectionSource = utils.makeFilledH2("filledDB");
    statementSource = connectionSource.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);

    connectionDest = utils.makeEmptyH2("emptyDB");
    connectionDest.setAutoCommit(false);
    prStatementDest = connectionDest.prepareStatement("INSERT INTO table_for_test (id , columnstring , columntimestamp, columnBlob , columnClob , columnBoolean  , columnTobeNull ) VALUES (?,?,?,?,?,?,?)");
    copierToolList = new ArrayList<CopierTool>(6);

  }

  @After
  public void tearDown() throws Exception {
    closer.closeResultSet(resultSetSource);
    closer.closeResultSet(resultSetDest);
    closer.closeStatement(prStatementDest);
    closer.closeStatement(statementSource);
    closer.closeStatement(statementDest);
    closer.closeConnection(connectionSource);
    closer.closeConnection(connectionDest);
  }

  @Test
  public void testAllCopierTool() throws Exception {


    CopierTool defaultCopierTool = new DefaultCopierTool(prStatementDest);
    copierToolList.add(defaultCopierTool);

    CopierTool oracleToMySql = new OracleToMySql(prStatementDest);
    copierToolList.add(oracleToMySql);

    CopierTool oracleToPostgresql = new OracleToPostgresql(prStatementDest);
    copierToolList.add(oracleToPostgresql);

    CopierTool sqlServerToOracle = new SqlServerToOracle(prStatementDest);
    copierToolList.add(sqlServerToOracle);

    CopierTool sqlServerToPostgresql = new SqlServerToPostgresql(prStatementDest);
    copierToolList.add(sqlServerToPostgresql);

    CopierTool oracleToSqlServer = new OracleToSqlServer(prStatementDest);
    copierToolList.add(oracleToSqlServer);

    for (int copierToolIndex = 0; copierToolIndex < copierToolList.size(); copierToolIndex++) {
      resultSetSource = statementSource.executeQuery("SELECT * FROM TABLE_FOR_TEST");

      while (resultSetSource.next()) {
        copierToolList.get(copierToolIndex).copy(resultSetSource, 0);
        copierToolList.get(copierToolIndex).copy(resultSetSource, 1);
        copierToolList.get(copierToolIndex).copyTimestamp(resultSetSource, 2);
        copierToolList.get(copierToolIndex).copyBlob(resultSetSource, 3);
        copierToolList.get(copierToolIndex).copyClob(resultSetSource, 4);
        copierToolList.get(copierToolIndex).copyBoolean(resultSetSource, 5);
        copierToolList.get(copierToolIndex).copyWhenNull(6);
        prStatementDest.addBatch();

      }
      prStatementDest.executeBatch();
      connectionDest.commit();
      resultSetSource.beforeFirst();

      statementDest = connectionDest.createStatement();
      resultSetDest = statementDest.executeQuery("SELECT * FROM TABLE_FOR_TEST");

      while (resultSetDest.next()) {
        resultSetSource.next();

        Assert.assertEquals(resultSetSource.getObject(1), resultSetDest.getObject(1));

        Assert.assertEquals(resultSetSource.getObject(2), resultSetDest.getObject(2));

        Assert.assertEquals(resultSetSource.getObject(3), resultSetDest.getObject(3));

        Blob blobSource = (Blob) resultSetSource.getObject(4);
        byte[] bytesSource = blobSource.getBytes(0, 100);
        Blob blobDest = (Blob) resultSetDest.getObject(4);
        byte[] bytesDest = blobDest.getBytes(0, 100);
        Assert.assertArrayEquals(bytesSource, bytesDest);

        byte[] bytesSourceClob = resultSetSource.getBytes(5);
        byte[] bytesDestClob = resultSetSource.getBytes(5);
        Assert.assertArrayEquals(bytesSourceClob,bytesDestClob);

        Assert.assertEquals(resultSetDest.getObject(6), resultSetSource.getObject(6));

        Assert.assertEquals(resultSetDest.getObject(7), resultSetSource.getObject(7));
      }

      // DELETE TABLE TO TRY ANOTHER COPIERTOOL
      closer.closeResultSet(resultSetDest);
      statementDest.executeUpdate("DELETE FROM TABLE_FOR_TEST");
      closer.closeStatement(statementDest);
    }
  }
}
