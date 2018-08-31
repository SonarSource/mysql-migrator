/*
 * Copyright (C) 2013-2018 SonarSource SA
 * All rights reserved
 * mailto:info AT sonarsource DOT com
 */
package com.sonar.dbcopy.utils.toolconfig;

import com.sonar.dbcopy.utils.Utils;
import com.sonar.dbcopy.utils.data.Database;
import org.junit.Before;
import org.junit.Test;

public class DatabaseComparerTest {

  private Database databaseSource, databaseCorrectDest, databaseUncorrectDest;
  private DatabaseComparer databaseComparer;

  @Before
  public void setUp() {
    Utils utils = new Utils();
    databaseSource = utils.makeDatabase(true);
    databaseCorrectDest = utils.makeDatabase(true);
    databaseUncorrectDest = utils.makeDatabase(false);
    databaseComparer = new DatabaseComparer();
  }

  @Test
  public void testDisplayAllTablesFoundIfExists() {
    databaseComparer.displayAllTablesFoundIfExists(databaseSource, databaseCorrectDest);
  }

  @Test
  public void testDisplayMissingTableInDb() {
    databaseComparer.displayMissingTableInDb(databaseSource, databaseUncorrectDest, "UNCORRECT DESTINATION");
  }

  @Test
  public void testDisplayDiffNumberRows() {
    databaseComparer.displayDiffNumberRows(databaseSource, databaseUncorrectDest);
  }
}
