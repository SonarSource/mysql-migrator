/*
 * Copyright (C) 2013 SonarSource SA
 * All rights reserved
 * mailto:contact AT sonarsource DOT com
 */

package com.sonar.dbcopy.reproduce.process;

import com.sonar.dbcopy.utils.Utils;
import com.sonar.dbcopy.utils.data.ConnecterDatas;
import com.sonar.dbcopy.utils.data.Database;
import com.sonar.dbcopy.utils.toolconfig.DbException;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class WholeCopyTest {

  private Connection connectionSource, connectionDest;
  private ConnecterDatas cdSource, cdDest;
  private Database databaseSource, databaseDest;

  @Before
  public void setUp() throws Exception {
    boolean threeTablesInSource=true;
    boolean threeTablesInDestination=false;

    Utils utils = new Utils();
    connectionSource = utils.makeFilledH2("source",threeTablesInSource);
    connectionDest = utils.makeEmptyH2("destination",threeTablesInDestination);

    databaseSource = utils.makeDatabase(threeTablesInSource);
    databaseDest = utils.makeDatabase(threeTablesInDestination);

    /* BE CAREFUL AT THE H2 DATABASE NAME: "source" AND "destination" */

    cdSource = new ConnecterDatas("org.h2.Driver", "jdbc:h2:mem:source;DB_CLOSE_ON_EXIT=-1;", "sonar", "sonar");
    cdDest = new ConnecterDatas("org.h2.Driver", "jdbc:h2:mem:destination;DB_CLOSE_ON_EXIT=-1;", "sonar", "sonar");

  }

  @Test
  public void testExecute() {
    LoopByTable loopByTable = new LoopByTable(cdSource, cdDest, databaseSource, databaseDest);
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
      throw new DbException("Problem when testing Reproducer.", e);

    } finally {
      try {
        resultSetSource.close();
      } catch (SQLException e) {
        throw new DbException("Problem when closing object in WholeCopyTest.", e);
      }

      try {
        resultSetDest.close();
      } catch (SQLException e) {
        throw new DbException("Problem when closing object in WholeCopyTest.", e);
      }

      try {
        statementSource.close();
      } catch (SQLException e) {
        throw new DbException("Problem when closing object in WholeCopyTest.", e);
      }

      try {
        statementDest.close();
      } catch (SQLException e) {
        throw new DbException("Problem when closing object in WholeCopyTest.", e);
      }
    }
  }

  @After
  public void tearDown() throws Exception {
    connectionSource.close();
    connectionDest.close();
  }
}
