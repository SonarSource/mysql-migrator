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


public class ConnecterDataTest {

  private ConnecterData connecterData;

  @Before
  public void setUp() {
    connecterData = new ConnecterData("driver", "url", "user", "pwd");
  }

  @Test
  public void testGetUrlSource() {
    assertEquals("url", connecterData.getUrl());
  }

  @Test
  public void testGetDriverSource() {
    assertEquals("driver", connecterData.getDriver());
  }

  @Test
  public void testGetUserSource() {
    assertEquals("user", connecterData.getUser());
  }

  @Test
  public void testGetPwdSource() {
    assertEquals("pwd", connecterData.getPwd());
  }

}
