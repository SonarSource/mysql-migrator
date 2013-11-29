/*
 * Copyright (C) 2013 SonarSource SA
 * All rights reserved
 * mailto:contact AT sonarsource DOT com
 */
package com.sonar.dbcopy;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;

public class MetadataGetter {

  private Statement statementSource;
  private Bdd bdd;

  public MetadataGetter (Statement statement, Bdd bdd){
    statementSource = statement;
    bdd = bdd;
  }



  public void getSchemaOfBddSource() throws SQLException {


  }

}
