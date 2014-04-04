/*
 * Copyright (C) 2013 SonarSource SA
 * All rights reserved
 * mailto:contact AT sonarsource DOT com
 */

package com.sonar.dbcopy.reproduce.writer;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;

public class SqlServerWriter extends DefaultWriter implements WriterTool {

  private PreparedStatement destinationStatement;

  public SqlServerWriter(PreparedStatement destinationStatement) {
    super(destinationStatement);
    this.destinationStatement = destinationStatement;
  }
}

