/*
 * Copyright (C) 2013 SonarSource SA
 * All rights reserved
 * mailto:contact AT sonarsource DOT com
 */
package com.sonar.dbcopy;

public class DatabaseComparer {

  private Database dbDest;

  public DatabaseComparer(Database dbD) {
    this.dbDest = dbD;
  }

  public boolean tableExistsInDestinationDatabase(String tableNameSource) {
    boolean tableExists = false;
    for (int indexTable = 0; indexTable < dbDest.getNbTables(); indexTable++) {
      if (tableNameSource.equals(dbDest.getTableName(indexTable))) {
        tableExists = true;
      }
    }
    return tableExists;
  }
}
