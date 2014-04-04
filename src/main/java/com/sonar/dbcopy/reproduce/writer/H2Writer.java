/*
 * Copyright (C) 2013 SonarSource SA
 * All rights reserved
 * mailto:contact AT sonarsource DOT com
 */

package com.sonar.dbcopy.reproduce.writer;

import com.sonar.dbcopy.reproduce.reader.DefaultReader;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;

public class H2Writer extends DefaultWriter implements WriterTool {

  private PreparedStatement destinationStatement;

  public H2Writer(PreparedStatement destinationStatement) {
    super(destinationStatement);
    this.destinationStatement = destinationStatement;
  }
}

