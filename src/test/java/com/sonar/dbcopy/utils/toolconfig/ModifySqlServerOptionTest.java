/*
 * Copyright (C) 2013-2017 SonarSource SA
 * All rights reserved
 * mailto:info AT sonarsource DOT com
 */
package com.sonar.dbcopy.utils.toolconfig;

import com.sonar.dbcopy.utils.Utils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.sql.Connection;


public class ModifySqlServerOptionTest {

  private Connection connection;
  private ModifySqlServerOption modifySqlServerOption;

  @Before
  public void setUp() throws Exception {
    Utils utils = new Utils();
    connection = utils.makeFilledH2("ModifySqlServerOptionTestDB", false);
    modifySqlServerOption = new ModifySqlServerOption();
  }

  @After
  public void tearDown() throws Exception {
    connection.close();
  }

  @Test
  public void testModifyIdentityInsert() {
    modifySqlServerOption.modifyIdentityInsert(connection, "not_a_table", "ON");
  }
}
