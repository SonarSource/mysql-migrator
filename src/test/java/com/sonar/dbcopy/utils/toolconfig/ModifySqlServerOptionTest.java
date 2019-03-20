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
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.sql.Connection;


public class ModifySqlServerOptionTest {

  private Connection connection;
  private ModifySqlServerOption modifySqlServerOption;

  @Before
  public void setUp() {
    Utils utils = new Utils();
    connection = utils.makeFilledH2("ModifySqlServerOptionTestDB", false);
    modifySqlServerOption = new ModifySqlServerOption();
  }

  @After
  public void tearDown() throws Exception {
    connection.close();
  }

  @Test
  public void testModifyIdentityInsert() throws Exception {
    modifySqlServerOption.modifyIdentityInsert(connection, "not_a_table", "ON");
  }
}
