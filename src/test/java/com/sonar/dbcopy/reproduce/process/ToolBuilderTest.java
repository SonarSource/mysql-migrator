/*
 * Copyright (C) 2013-2016 SonarSource SA
 * All rights reserved
 * mailto:contact AT sonarsource DOT com
 */
package com.sonar.dbcopy.reproduce.process;

import com.sonar.dbcopy.reproduce.reader.ReaderTool;
import com.sonar.dbcopy.reproduce.writer.WriterTool;
import com.sonar.dbcopy.utils.Utils;
import com.sonar.dbcopy.utils.toolconfig.Closer;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.sql.Connection;
import java.sql.SQLException;

import static org.fest.assertions.Assertions.assertThat;
import static org.junit.Assert.fail;

public class ToolBuilderTest {

  private Connection connectionSource, connectionDest;

  @Before
  public void setUp() throws SQLException {
    Utils utils = new Utils();
    connectionSource = utils.makeFilledH2("ToolBuilderTestSourceDB", false);
    connectionDest = utils.makeEmptyH2("ToolBuilderTestDestinationDB", false);
  }

  @Test
  public void testBuildReaderTool() throws Exception {
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
  public void testBuildWriterTool() throws Exception {
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
