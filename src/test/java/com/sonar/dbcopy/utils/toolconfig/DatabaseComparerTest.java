/*
 * Copyright (C) 2013 SonarSource SA
 * All rights reserved
 * mailto:contact AT sonarsource DOT com
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
  public void testDisplayAllTablesFoundIfExists() throws Exception {
    databaseComparer.displayAllTablesFoundIfExists(databaseSource, databaseCorrectDest);
  }

  @Test
  public void testDisplayMissingTableInDb() throws Exception {
    databaseComparer.displayMissingTableInDb(databaseSource, databaseUncorrectDest,"UNCORRECT DESTINATION" );
  }

  @Test
  public void testDisplayDiffNumberRows() throws Exception {
    databaseComparer.displayDiffNumberRows(databaseSource,databaseUncorrectDest);

  }
}
