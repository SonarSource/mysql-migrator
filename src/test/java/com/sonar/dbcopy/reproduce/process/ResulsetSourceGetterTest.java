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
package com.sonar.dbcopy.reproduce.process;

import com.sonar.dbcopy.utils.Utils;
import com.sonar.dbcopy.utils.toolconfig.Closer;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import static junit.framework.TestCase.fail;
import static org.fest.assertions.Assertions.assertThat;

public class ResulsetSourceGetterTest {

  private Connection connection;
  private Statement statement;
  private ResultSet resultSet;
  private ResulsetSourceGetter resulsetSourceGetter;


  @Before
  public void setUp() {
    Utils utils = new Utils();
    connection = utils.makeFilledH2("ResulsetSourceGetterTestDB", false);
    resulsetSourceGetter = new ResulsetSourceGetter("table_for_test");
  }

  @After
  public void tearDown() {
    Closer closer = new Closer("ResulsetSourceGetterTest");
    closer.closeResultSet(resultSet);
    closer.closeStatement(statement);
    closer.closeConnection(connection);
  }

  @Test
  public void testAllMethods() throws Exception {
    statement = resulsetSourceGetter.createAndReturnStatementSource(connection);
    assertThat(statement).isInstanceOf(Statement.class);
    resultSet = resulsetSourceGetter.createAndReturnResultSetSource(statement);
    resultSet.next();
    Assert.assertEquals(1, resultSet.getInt(1));
    resultSet.next();
    Assert.assertEquals(2, resultSet.getInt(1));
    resultSet.next();
    try {
      // BE CAREFUL TO utils.makeFilledH2()  : THERE MUST BE ONLY 2 TABLES TO PASS THE TEST
      resultSet.getInt(1);
      fail();
    } catch (Exception e) {
      assertThat(e).isInstanceOf(SQLException.class).hasMessage("No data is available [2000-197]");
    }
  }
}
