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
package com.sonar.dbcopy.reproduce.reader;

import com.sonar.dbcopy.utils.Utils;
import com.sonar.dbcopy.utils.toolconfig.Closer;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;

public class ReaderToolTest {

  private ArrayList<ReaderTool> readerToolList;
  private Connection connection;
  private Statement statement;
  private ResultSet resultSet;

  @Before
  public void setUp() throws Exception {
    readerToolList = new ArrayList<ReaderTool>();
    readerToolList.add(new H2Reader());
    readerToolList.add(new MySqlReader());
    readerToolList.add(new OracleReader());
    readerToolList.add(new PostgresqlReader());
    readerToolList.add(new SqlServerReader());

    Utils utils = new Utils();
    connection = utils.makeFilledH2("ReaderToolTestDB", false);
    statement = connection.createStatement();
    resultSet = statement.executeQuery("SELECT * FROM table_for_test");
  }

  @After
  public void tearDown() {
    Closer closer = new Closer("ReaderTootTest");
    closer.closeResultSet(resultSet);
    closer.closeStatement(statement);
    closer.closeConnection(connection);
  }

  @Test
  public void testReadTimestamp() throws Exception {
    // indexColumn CORRESPONDS TO THE COLUMN IN H2 DATABASE TO HAVE A TIMESTAMP (GO AND SEE utils.makeFilledH2)
    int indexColumn = 2;
    resultSet.beforeFirst();
    while (resultSet.next()) {
      for (int indexReader = 0; indexReader < readerToolList.size(); indexReader++) {
        Assert.assertEquals(resultSet.getTimestamp(indexColumn + 1), readerToolList.get(indexReader).readTimestamp(resultSet, indexColumn));
      }
    }
  }

  @Test
  public void testReadBlob() throws Exception {
    // indexColumn CORRESPONDS TO THE COLUMN IN H2 DATABASE TO HAVE A BLOB (GO AND SEE utils.makeFilledH2)
    int indexColumn = 3;
    resultSet.beforeFirst();
    while (resultSet.next()) {
      for (int indexReader = 0; indexReader < readerToolList.size(); indexReader++) {
        // ONLY COMPARE LENGTH FOR BLOB TO MAKE IT EASIER
        Assert.assertEquals(resultSet.getBlob(indexColumn + 1).length(), readerToolList.get(indexReader).readBlob(resultSet, indexColumn).length);
      }
    }
  }

  @Test
  public void testReadClob() throws Exception {
    // indexColumn CORRESPONDS TO THE COLUMN IN H2 DATABASE TO HAVE A CLOB (GO AND SEE utils.makeFilledH2)
    int indexColumn = 4;
    resultSet.beforeFirst();
    while (resultSet.next()) {
      for (int indexReader = 0; indexReader < readerToolList.size(); indexReader++) {
        // ONLY COMPARE LENGTH FOR CLOB TO MAKE IT EASIER
        Assert.assertEquals(resultSet.getClob(indexColumn + 1).length(), readerToolList.get(indexReader).readBlob(resultSet, indexColumn).length);
      }
    }
  }

  @Test
  public void testReadBoolean() throws Exception {
    // indexColumn CORRESPONDS TO THE COLUMN IN H2 DATABASE TO HAVE A BOOLEAN (GO AND SEE utils.makeFilledH2)
    int indexColumn = 5;
    resultSet.beforeFirst();
    while (resultSet.next()) {
      for (int indexReader = 0; indexReader < readerToolList.size(); indexReader++) {
        Assert.assertEquals(resultSet.getBoolean(indexColumn + 1), readerToolList.get(indexReader).readBoolean(resultSet, indexColumn));
      }
    }
  }

  @Test
  public void testReadObject() throws Exception {
    // indexColumn CORRESPONDS TO THE COLUMN IN H2 DATABASE TO HAVE AN INTEGER(GO AND SEE utils.makeFilledH2)
    int indexColumn = 0;
    resultSet.beforeFirst();
    while (resultSet.next()) {
      for (int indexReader = 0; indexReader < readerToolList.size(); indexReader++) {
        Assert.assertEquals(resultSet.getObject(indexColumn + 1), readerToolList.get(indexReader).readObject(resultSet, indexColumn));
      }
    }
  }

  @Test
  public void testReadVarchar() throws Exception {
    // indexColumn CORRESPONDS TO THE COLUMN IN H2 DATABASE TO HAVE VARCHAR (GO AND SEE utils.makeFilledH2)
    int indexColumn = 1;
    resultSet.beforeFirst();
    while (resultSet.next()) {
      for (int indexReader = 0; indexReader < readerToolList.size(); indexReader++) {
        Assert.assertEquals(resultSet.getString(indexColumn + 1), readerToolList.get(indexReader).readVarchar(resultSet, indexColumn));
      }
    }
  }
}
