/*
 * Copyright (C) 2013-2016 SonarSource SA
 * All rights reserved
 * mailto:contact AT sonarsource DOT com
 */
package com.sonar.dbcopy.reproduce.writer;

import java.sql.PreparedStatement;

public class MySqlWriter extends DefaultWriter {

  public MySqlWriter(PreparedStatement destinationStatement) {
    super(destinationStatement);
  }
}

