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

import com.sonar.dbcopy.reproduce.reader.ReaderTool;
import com.sonar.dbcopy.reproduce.writer.WriterTool;
import com.sonar.dbcopy.utils.Utils;
import com.sonar.dbcopy.utils.toolconfig.Closer;
import java.sql.Connection;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.fest.assertions.Assertions.assertThat;
import static org.junit.Assert.fail;

public class ToolBuilderTest {

  private Connection connectionSource, connectionDest;

  @Before
  public void setUp() {
    Utils utils = new Utils();
    connectionSource = utils.makeFilledH2("ToolBuilderTestSourceDB", false);
    connectionDest = utils.makeEmptyH2("ToolBuilderTestDestinationDB", false);
  }

  @Test
  public void testBuildReaderTool() {
    ToolBuilder toolBuilder = new ToolBuilder(null, null);
    try {
      toolBuilder.buildReaderTool();
      fail();
    } catch (Exception e) {
      assertThat(e).isInstanceOf(NullPointerException.class);
    }
    toolBuilder = new ToolBuilder(connectionSource, connectionDest);
    assertThat(toolBuilder.buildReaderTool()).isNotNull().isInstanceOf(ReaderTool.class);
  }

  @Test
  public void testBuildWriterTool() {
    ToolBuilder toolBuilder = new ToolBuilder(null, null);
    try {
      toolBuilder.buildWriterTool(null);
      fail();
    } catch (Exception e) {
      assertThat(e).isInstanceOf(NullPointerException.class);
    }
    toolBuilder = new ToolBuilder(connectionSource, connectionDest);
    assertThat(toolBuilder.buildWriterTool(null)).isNotNull().isInstanceOf(WriterTool.class);
  }

  @After
  public void  tearDown(){
    Closer closer = new Closer("ToolBuilderTest");
    closer.closeConnection(connectionSource);
    closer.closeConnection(connectionDest);
  }
}
