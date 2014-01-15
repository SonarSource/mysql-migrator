/*
 * Copyright (C) 2013 SonarSource SA
 * All rights reserved
 * mailto:contact AT sonarsource DOT com
 */

package com.sonar.dbcopy;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.assertEquals;


public class ConnecterDatasTest {

  private ConnecterDatas connecterDatas;

  @Before
  public void setUp() throws Exception {
    connecterDatas = new ConnecterDatas("driver","url","user","pwd");
  }

  @Test
  public void testGetUrlSource() throws Exception {
    assertEquals("url",connecterDatas.getUrl());
  }

  @Test
  public void testGetDriverSource() throws Exception {
    assertEquals("driver",connecterDatas.getDriver());
  }

  @Test
  public void testGetUserSource() throws Exception {
    assertEquals("user",connecterDatas.getUser());
  }

  @Test
  public void testGetPwdSource() throws Exception {
    assertEquals("pwd",connecterDatas.getPwd());
  }

}
