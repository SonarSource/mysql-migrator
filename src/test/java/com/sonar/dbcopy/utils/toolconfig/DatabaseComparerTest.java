/*
 * Copyright (C) 2013-2018 SonarSource SA
 * All rights reserved
 * mailto:info AT sonarsource DOT com
 */
package com.sonar.dbcopy.utils.toolconfig;

import com.sonar.dbcopy.utils.Utils;
import com.sonar.dbcopy.utils.data.Database;
import org.junit.Test;

public class DatabaseComparerTest {

  private Utils utils = new Utils();
  private Database databaseSource = utils.makeDatabase(true);
  private Database databaseCorrectDest = utils.makeDatabase(true);
  private Database databaseUncorrectDest = utils.makeDatabase(false);
  private DatabaseComparer databaseComparer = new DatabaseComparer();

  // FIXME those tests does not test any output

  @Test
  public void testDisplayAllTablesFoundIfExists() {
    databaseComparer.displayAllTablesFoundIfExists(databaseSource, databaseCorrectDest);
  }

  @Test
  public void testDisplayAdditionalTables() {
    Database oneTableDB = new Database();
    oneTableDB.addToTablesList("new_table");
    databaseComparer.displayAllTablesFoundIfExists(databaseSource, oneTableDB);
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
