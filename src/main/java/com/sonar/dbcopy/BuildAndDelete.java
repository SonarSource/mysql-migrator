/*
 * Copyright (C) 2013 SonarSource SA
 * All rights reserved
 * mailto:contact AT sonarsource DOT com
 */

package com.sonar.dbcopy;

public class BuildAndDelete {

  public void execute(ConnecterDatas dc, Database database) {

    MetadataGetter metadataGetter = new MetadataGetter(dc, database);
    metadataGetter.run();

    Deleter deleter = new Deleter(dc, database);
    deleter.execute();
  }
}
