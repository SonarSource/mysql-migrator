/*
 * Copyright (C) 2013 SonarSource SA
 * All rights reserved
 * mailto:contact AT sonarsource DOT com
 */

package com.sonar.dbcopy.reproduce;

import com.sonar.dbcopy.utils.DbException;
import com.sonar.dbcopy.utils.Utils;
import com.sonar.dbcopy.utils.objects.ConnecterDatas;
import com.sonar.dbcopy.utils.objects.Database;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class ReproducerTest {

  private Connection connectionSource, connectionDest;
  private ConnecterDatas cdSource, cdDest;

  @Before
  public void setUp() throws Exception {
    Utils utils = new Utils();
    connectionSource = utils.makeFilledH2("source");
    connectionDest = utils.makeEmptyH2("destination");

    Database databaseSource = utils.makeDatabase();
    Database databaseDest = utils.makeDatabase();

    /* BE CAREFUL AT THE H2 DATABASE NAME: "source" AND "destination" */

    cdSource = new ConnecterDatas("org.h2.Driver", "jdbc:h2:mem:source;DB_CLOSE_ON_EXIT=-1;", "sonar", "sonar");
    cdDest = new ConnecterDatas("org.h2.Driver", "jdbc:h2:mem:destination;DB_CLOSE_ON_EXIT=-1;", "sonar", "sonar");

    Reproducer reproducer = new Reproducer(cdSource, cdDest, databaseSource, databaseDest);
    reproducer.execute();
  }

  @Test
  public void testExecute() {
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
        throw new DbException("Problem when closing object in ReproducerTest.", e);
      }

      try {
        resultSetDest.close();
      } catch (SQLException e) {
        throw new DbException("Problem when closing object in ReproducerTest.", e);
      }

      try {
        statementSource.close();
      } catch (SQLException e) {
        throw new DbException("Problem when closing object in ReproducerTest.", e);
      }

      try {
        statementDest.close();
      } catch (SQLException e) {
        throw new DbException("Problem when closing object in ReproducerTest.", e);
      }
    }
  }

  @After
  public void tearDown() throws Exception {
    connectionSource.close();
    connectionDest.close();
  }
}
