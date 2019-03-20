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
package com.sonar.dbcopy.reproduce.process;

import com.sonar.dbcopy.utils.Utils;
import com.sonar.dbcopy.utils.data.ConnecterData;
import com.sonar.dbcopy.utils.toolconfig.SqlDbCopyException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.sql.Connection;
import java.sql.SQLException;

import static org.fest.assertions.Assertions.assertThat;
import static org.junit.Assert.fail;


public class SequenceReseterTest {

  private Connection connection;

  @Before
  public void setUp() {
    Utils utils = new Utils();
    connection = utils.makeFilledH2("SequenceReseterTestDB",true);
  }

  @Test
  public void shouldFailOnMissingJdbcDriver() {
    ConnecterData connecterData = new ConnecterData("Not a Driver", "jdbc:h2:mem:SequenceReseterTestDB;DB_CLOSE_ON_EXIT=-1;", "sonar", "sonar");
    SequenceReseter sequenceReseter = new SequenceReseter("table_for_test", connecterData);
    try {
      sequenceReseter.execute();
      fail();
    } catch (Exception e) {
      assertThat(e).isInstanceOf(SqlDbCopyException.class).hasMessage("Impossible to get the jdbc DRIVER Not a Driver.");
    }
  }

  @Test
  public void shouldSucceedOnLegacyTableWithId() {
    ConnecterData connecterData = new ConnecterData("org.h2.Driver", "jdbc:h2:mem:SequenceReseterTestDB;DB_CLOSE_ON_EXIT=-1;", "sonar", "sonar");
    SequenceReseter sequenceReseter = new SequenceReseter("table_for_test", connecterData);
    sequenceReseter.execute();
  }

  @Test
  public void shouldSucceedOnNewTableWithoutId() {
    ConnecterData connecterData = new ConnecterData("org.h2.Driver", "jdbc:h2:mem:SequenceReseterTestDB;DB_CLOSE_ON_EXIT=-1;", "sonar", "sonar");
    SequenceReseter sequenceReseter = new SequenceReseter("table_without_id", connecterData);
    sequenceReseter.execute();
  }

  @After
  public void tearDown() throws SQLException {
    connection.close();
  }
}
