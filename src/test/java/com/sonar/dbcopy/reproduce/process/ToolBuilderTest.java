/*
 * Copyright (C) 2013 SonarSource SA
 * All rights reserved
 * mailto:contact AT sonarsource DOT com
 */

package com.sonar.dbcopy.reproduce.process;

import com.sonar.dbcopy.reproduce.reader.ReaderTool;
import com.sonar.dbcopy.reproduce.writer.WriterTool;
import com.sonar.dbcopy.utils.Utils;
import org.junit.Before;
import org.junit.Test;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import static org.fest.assertions.Assertions.assertThat;
import static org.junit.Assert.fail;

public class ToolBuilderTest {

  private Connection connectionSource, connectionDest;
  private PreparedStatement preparedStatementDest;

  @Before
  public void setUp() throws SQLException {
    Utils utils = new Utils();
    connectionSource = utils.makeFilledH2("source",false);
    connectionDest = utils.makeEmptyH2("destination",false);
  }

  @Test
  public void testBuildReaderTool() throws Exception {
    ToolBuilder toolBuilder = new ToolBuilder(null, null);
    try {
      toolBuilder.buildReaderTool();
      fail();
    } catch (NullPointerException e) {
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
    } catch (NullPointerException e) {
      assertThat(e).isInstanceOf(NullPointerException.class);
    }
    toolBuilder = new ToolBuilder(connectionSource, connectionDest);
    assertThat(toolBuilder.buildWriterTool(null)).isNotNull().isInstanceOf(WriterTool.class);
  }
}
