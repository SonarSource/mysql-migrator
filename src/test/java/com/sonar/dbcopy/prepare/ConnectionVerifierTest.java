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
package com.sonar.dbcopy.prepare;

import com.sonar.dbcopy.utils.Utils;
import com.sonar.dbcopy.utils.data.ConnecterData;
import com.sonar.dbcopy.utils.toolconfig.MessageException;
import com.sonar.dbcopy.utils.toolconfig.SqlDbCopyException;
import org.junit.Before;
import org.junit.Test;

import static org.fest.assertions.Assertions.assertThat;
import static org.junit.Assert.fail;

public class ConnectionVerifierTest {

  private ConnectionVerifier connectionVerifier;

  @Before
  public void setUp() throws Exception {
    Utils utils = new Utils();
    utils.makeH2("ConnectionVerifierTestDB");
    connectionVerifier = new ConnectionVerifier();
  }

  @Test
  public void testDatabaseIsReached() {
    ConnecterData availableCd = new ConnecterData("org.h2.Driver", "jdbc:h2:mem:ConnectionVerifierTestDB;DB_CLOSE_ON_EXIT=-1;", "sonar", "sonar");
    ConnectionVerifier connectionVerifier = new ConnectionVerifier();
    connectionVerifier.databaseIsReached(availableCd);
  }

  @Test
  public void testDatabaseIsNotReached() {
    ConnecterData notAvailableURL = new ConnecterData("org.h2.Driver", "wrongUrl", "sonar", "sonar");
    try {
      connectionVerifier.databaseIsReached(notAvailableURL);
      fail();
    } catch (Exception e) {
      assertThat(e).isInstanceOf(SqlDbCopyException.class).hasMessage("Database can not be reached at url wrongUrl. Verify url, user name and password.");
    }

    ConnecterData notAvailableDriver = new ConnecterData("wrong.Driver", "jdbc:h2:mem:ConnectionVerifierTestDB;DB_CLOSE_ON_EXIT=-1;", "sonar", "sonar");
    try {
      connectionVerifier.databaseIsReached(notAvailableDriver);
      fail();
    } catch (Exception e) {
      assertThat(e).isInstanceOf(MessageException.class).hasMessage("Driver wrong.Driver does not exists : wrong.Driver");
    }
  }
}
