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
import com.sonar.dbcopy.utils.data.ConnecterData;
import com.sonar.dbcopy.utils.data.Database;
import com.sonar.dbcopy.utils.toolconfig.Closer;
import com.sonar.dbcopy.utils.toolconfig.SqlDbCopyException;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class LoopByTableTest {

  private Connection connectionSource, connectionDest;
  private ConnecterData cdSource, cdDest;
  private Database databaseSource, databaseDest;
  private Closer closer;
  private int commitSize;

  @Before
  public void setUp() {
    boolean threeTablesInSource = true;
    boolean threeTablesInDestination = false;

    closer = new Closer("LoopByTableTest");

    Utils utils = new Utils();
    connectionSource = utils.makeFilledH2("LoopByTableTestSourceDB", threeTablesInSource);
    connectionDest = utils.makeEmptyH2("LoopByTableTestDestinationDB", threeTablesInDestination);

    databaseSource = utils.makeDatabase(threeTablesInSource);
    databaseDest = utils.makeDatabase(threeTablesInDestination);

    /* BE CAREFUL AT THE H2 DATABASE NAME: "source" AND "destination" */

    cdSource = new ConnecterData("org.h2.Driver", "jdbc:h2:mem:LoopByTableTestSourceDB;DB_CLOSE_ON_EXIT=-1;", "sonar", "sonar");
    cdDest = new ConnecterData("org.h2.Driver", "jdbc:h2:mem:LoopByTableTestDestinationDB;DB_CLOSE_ON_EXIT=-1;", "sonar", "sonar");

    commitSize = 10;
  }

  @Test
  public void testLoopByTable() {
    LoopByTable loopByTable = new LoopByTable(cdSource, cdDest, databaseSource, databaseDest, commitSize);
    loopByTable.execute();

    Statement statementSource = null, statementDest = null;
    ResultSet resultSetSource = null, resultSetDest = null;
    try {
      statementSource = connectionSource.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_UPDATABLE);
      statementDest = connectionDest.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_UPDATABLE);

      resultSetSource = statementSource.executeQuery("SELECT * FROM table_for_test ORDER BY 1");
      resultSetDest = statementDest.executeQuery("SELECT * FROM table_for_test ORDER BY 1");

      while (resultSetSource.next()) {
        resultSetDest.next();
        // ONLY TEST IF EACH TABLE HAS BEEN COPIED ( TESTS FOR ALL COLUMNS ARE DONE IN CopierToolTest)
        Assert.assertEquals(resultSetSource.getObject(1), resultSetDest.getObject(1));
      }

      resultSetSource = statementSource.executeQuery("SELECT * FROM empty_table_for_test ORDER BY 1");
      resultSetDest = statementDest.executeQuery("SELECT * FROM empty_table_for_test ORDER BY 1");

      // IF isBeforerFirst MEANS THERE ISN'T ANY ROW IN THIS TABLE
      Assert.assertFalse(resultSetSource.isBeforeFirst());
      Assert.assertFalse(resultSetDest.isBeforeFirst());

    } catch (SQLException e) {
      throw new SqlDbCopyException("Problem when testing Reproducer.", e);
    } finally {
      closer.closeResultSet(resultSetSource);
      closer.closeResultSet(resultSetDest);
      closer.closeStatement(statementSource);
      closer.closeStatement(statementDest);
    }
  }

  @After
  public void tearDown() {
    closer.closeConnection(connectionSource);
    closer.closeConnection(connectionDest);
  }
}
