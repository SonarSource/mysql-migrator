/*
 * Copyright (C) 2013 SonarSource SA
 * All rights reserved
 * mailto:contact AT sonarsource DOT com
 */

package com.sonar.dbcopy;

public class ConnecterDatas {

  private String driverSource, urlSource, userSource, pwdSource, driverDest, urlDest, userDest, pwdDest;

  public ConnecterDatas(String driverSource, String urlSource, String userSource, String pwdSource, String driverDest, String urlDest, String userDest, String pwdDest) {
    this.driverSource = driverSource;
    this.urlSource = urlSource;
    this.userSource = userSource;
    this.pwdSource = pwdSource;
    this.driverDest = driverDest;
    this.urlDest = urlDest;
    this.userDest = userDest;
    this.pwdDest = pwdDest;
  }

  public String getUrlSource() {
    return urlSource;
  }

  public String getDriverSource() {
    return driverSource;
  }

  public String getUserSource() {
    return userSource;
  }

  public String getPwdSource() {
    return pwdSource;
  }

  public String getDriverDest() {
    return driverDest;
  }

  public String getUrlDest() {
    return urlDest;
  }

  public String getUserDest() {
    return userDest;
  }

  public String getPwdDest() {
    return pwdDest;
  }
}
