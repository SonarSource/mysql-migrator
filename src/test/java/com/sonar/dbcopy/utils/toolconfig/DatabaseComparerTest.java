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
