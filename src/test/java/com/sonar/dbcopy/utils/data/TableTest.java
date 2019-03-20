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

import java.sql.Types;

import static org.junit.Assert.assertEquals;

public class TableTest {

  private Table table;

  @Before
  public void setUp() {
    table = new Table("table1");
    table.setNbRows(12);
    table.addColumn(0, "col0", Types.VARCHAR);
    table.addColumn(1, "col1", Types.INTEGER);
    table.addColumn(2, "col2", Types.TIMESTAMP);
    table.addColumn(3, "col3", Types.BLOB);
    table.addColumn(4, null, null);
    table.makeStringsUsedForTable();
  }

  @Test
  public void testGetNbColumns() {
    assertEquals(5, table.getNbColumns());
  }

  @Test
  public void testAddAndGetColumn() {
    assertEquals("col0", table.getColumnName(0));
    assertEquals("col1", table.getColumnName(1));
    assertEquals("col2", table.getColumnName(2));
    assertEquals("col3", table.getColumnName(3));
    assertEquals("null", table.getColumnName(4));
  }

  @Test
  public void testGetName() {
    assertEquals("table1", table.getName());
  }

  @Test
  public void testSetAndGetNbRows() {
    assertEquals(12, table.getNbRows());
  }

  @Test
  public void testSetAndGetTypes() {
    assertEquals((long) Types.VARCHAR, (long) table.getType(0));
    assertEquals((long) Types.INTEGER, (long) table.getType(1));
    assertEquals((long) Types.TIMESTAMP, (long) table.getType(2));
    assertEquals((long) Types.BLOB, (long) table.getType(3));
    assertEquals(0, (long) table.getType(4));

  }

  @Test
  public void testMakeColumnString() {
    assertEquals("col0,col1,col2,col3,null", table.getColumnNamesAsString());
  }

  @Test
  public void testMakeQuestionMarkString() {
    assertEquals("?,?,?,?,?", table.getQuestionMarksAsString());
  }

  @Test
  public void testmakeStringOfTypes() {
    assertEquals("VARCHAR,INTEGER,TIMESTAMP,BLOB,NULL", table.getTypesAsString());

  }
}
