/*
 * Copyright (C) 2013-2017 SonarSource SA
 * All rights reserved
 * mailto:info AT sonarsource DOT com
 */
package com.sonar.dbcopy.reproduce.writer;

import java.sql.PreparedStatement;

public class PostgresqlWriter extends DefaultWriter {

  public PostgresqlWriter(PreparedStatement destinationStatement) {
    super(destinationStatement);
  }
}

