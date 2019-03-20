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
package com.sonar.dbcopy.utils.data;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class DatabaseTest {

  private Database database;

  @Before
  public void setUp() {
    database = new Database();
    database.addToTablesList("table1");
    database.addToTablesList("table2");
  }

  @Test
  public void testAddAndGetTable() {
    assertNotNull(database);
    assertNotNull(database.getTable(0));
    assertNotNull(database.getTable(0));
  }

  @Test
  public void testGetNbTables() {
    assertEquals(2, database.getNbTables());
  }

  @Test
  public void testGetTableName() {
    assertEquals("table1", database.getTableName(0));
    assertEquals("table2", database.getTableName(1));
  }
}
