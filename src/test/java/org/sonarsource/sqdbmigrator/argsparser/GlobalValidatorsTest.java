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
package org.sonarsource.sqdbmigrator.argsparser;

import com.tngtech.java.junit.dataprovider.DataProvider;
import com.tngtech.java.junit.dataprovider.DataProviderRunner;
import com.tngtech.java.junit.dataprovider.UseDataProvider;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(DataProviderRunner.class)
public class GlobalValidatorsTest {
  @Test
  public void allPresent_passes_when_both_present() {
    // expected to run without exceptions
    Map<String, String> bothPresent = new HashMap<>();
    bothPresent.put("name1", "foo");
    bothPresent.put("name2", "bar");

    GlobalValidators.allPresent("name1", "name2").validate(bothPresent);
  }

  @Test(expected = IllegalStateException.class)
  @UseDataProvider("eitherName1OrName2IsMissing")
  public void allPresent_fails_when_any_missing(Map<String, String> rawOptions) {
    GlobalValidators.allPresent("name1", "name2").validate(rawOptions);
  }

  @DataProvider
  public static Object[][] eitherName1OrName2IsMissing() {
    return new Object[][]{
      {Collections.emptyMap()},
      {Collections.singletonMap("name1", "foo")},
      {Collections.singletonMap("name2", "bar")},
    };
  }
}
