/*
 * SonarQube MySQL Database Migrator
 * Copyright (C) 2019-2021 SonarSource SA
 * mailto:info AT sonarsource DOT com
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package org.sonarsource.sqdbmigrator.migrator;

import com.tngtech.java.junit.dataprovider.DataProvider;
import com.tngtech.java.junit.dataprovider.DataProviderRunner;
import com.tngtech.java.junit.dataprovider.UseDataProvider;
import org.junit.Rule;
import org.junit.Test;
import org.junit.contrib.java.lang.system.ExpectedSystemExit;
import org.junit.contrib.java.lang.system.SystemErrRule;
import org.junit.contrib.java.lang.system.SystemOutRule;
import org.junit.runner.RunWith;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(DataProviderRunner.class)
public class System2Test {

  @Rule
  public final SystemOutRule systemOutRule = new SystemOutRule().enableLog();

  @Rule
  public final SystemErrRule systemErrRule = new SystemErrRule().enableLog();

  @Rule
  public final ExpectedSystemExit exit = ExpectedSystemExit.none();

  private final System2 underTest = new System2();

  @Test
  public void writes_to_stdout() {
    underTest.printlnOut("foo bar");
    assertThat(systemOutRule.getLog()).isEqualTo("foo bar\n");
  }

  @Test
  public void writes_to_stderr() {
    underTest.printlnErr("foo bar");
    assertThat(systemErrRule.getLog()).isEqualTo("foo bar\n");
  }

  @Test
  @UseDataProvider("statusCodes")
  public void exits_with_status_code(int statusCode) {
    exit.expectSystemExitWithStatus(statusCode);
    underTest.exit(statusCode);
  }

  @DataProvider
  public static Object[][] statusCodes() {
    return new Object[][] {
      {0},
      {1},
      {2},
    };
  }
}
