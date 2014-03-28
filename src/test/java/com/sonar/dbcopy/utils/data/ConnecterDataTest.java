/*
 * Copyright (C) 2013 SonarSource SA
 * All rights reserved
 * mailto:contact AT sonarsource DOT com
 */

package com.sonar.dbcopy.utils.data;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.assertEquals;


public class ConnecterDataTest {

  private ConnecterData connecterData;

  @Before
  public void setUp() throws Exception {
    connecterData = new ConnecterData("driver","url","user","pwd");
  }

  @Test
  public void testGetUrlSource() throws Exception {
    assertEquals("url", connecterData.getUrl());
  }

  @Test
  public void testGetDriverSource() throws Exception {
    assertEquals("driver", connecterData.getDriver());
  }

  @Test
  public void testGetUserSource() throws Exception {
    assertEquals("user", connecterData.getUser());
  }

  @Test
  public void testGetPwdSource() throws Exception {
    assertEquals("pwd", connecterData.getPwd());
  }

}
