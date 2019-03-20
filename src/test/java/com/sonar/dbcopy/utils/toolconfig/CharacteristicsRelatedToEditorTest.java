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
import org.fest.assertions.Assertions;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class CharacteristicsRelatedToEditorTest {

  private CharacteristicsRelatedToEditor chRelToEd;
  private DatabaseMetaData metaData;
  private Connection connection;

  @Before
  public void setUp() throws Exception {
    Utils utils = new Utils();
    connection = utils.makeFilledH2("CharacteristicsRelatedToEditorTestDB", false);
    metaData = connection.getMetaData();
  }

  @After
  public void tearDown() {
    Closer closer = new Closer("CharacteristicsRelatedToEditorTest");
    closer.closeConnection(connection);
  }

  @Test
  public void testMakeDropSequenceRequest() {
    assertEquals("DROP SEQUENCE FOO_SEQ"
        , CharacteristicsRelatedToEditor.makeDropSequenceRequest("Foo"));
  }

  @Test
  public void testGetSchema() throws SQLException {
    assertEquals("PUBLIC", connection.getSchema());
  }


  @Test
  public void testGiveTableNameRelatedToVendor() throws SQLException {
    assertEquals("TABLE", CharacteristicsRelatedToEditor.transfromCaseOfTableName(metaData, "table"));
  }

  @Test
  public void testGiveDriverWithUrlFromUser() {
    assertEquals("com.mysql.jdbc.Driver", CharacteristicsRelatedToEditor.giveDriverWithUrlFromUser("jdbc:my"));
    assertEquals("oracle.jdbc.OracleDriver", CharacteristicsRelatedToEditor.giveDriverWithUrlFromUser("jdbc:or"));
    assertEquals("org.h2.Driver",         CharacteristicsRelatedToEditor.giveDriverWithUrlFromUser("jdbc:h2"));
    assertEquals("org.postgresql.Driver", CharacteristicsRelatedToEditor.giveDriverWithUrlFromUser("jdbc:po"));
    assertEquals("com.microsoft.sqlserver.jdbc.SQLServerDriver", CharacteristicsRelatedToEditor.giveDriverWithUrlFromUser("jdbc:sq"));

    try {
      CharacteristicsRelatedToEditor.giveDriverWithUrlFromUser("wrongUrl");
      fail();
    } catch (Exception e) {
      Assertions.assertThat(e).isInstanceOf(MessageException.class).hasMessage("Url wrongUrl does not correspond to a correct format to get the good jdbc driver.");
    }
  }
}
