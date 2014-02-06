/*
 * Copyright (C) 2013 SonarSource SA
 * All rights reserved
 * mailto:contact AT sonarsource DOT com
 */
package com.sonar.dbcopy;

public class DatabaseComparer {

  private Database database;

  public DatabaseComparer(Database db) {
    this.database = db;
  }

  public boolean tableExistsInDestinationDatabase(String tableNameToFindInDb) {
    boolean tableExists = false;
    for (int indexTable = 0; indexTable < database.getNbTables(); indexTable++) {
      if (tableNameToFindInDb.equals(database.getTableName(indexTable))) {
        tableExists = true;
      }
    }
    return tableExists;
  }
}
