/*
 * Copyright (C) 2013-2018 SonarSource SA
 * All rights reserved
 * mailto:info AT sonarsource DOT com
 */
package com.sonar.dbcopy.reproduce.writer;

import java.sql.PreparedStatement;

public class OracleWriter extends DefaultWriter {

  public OracleWriter(PreparedStatement destinationStatement) {
    super(destinationStatement);
  }
}

