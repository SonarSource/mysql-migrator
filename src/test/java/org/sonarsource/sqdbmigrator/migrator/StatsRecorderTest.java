/*
 * SonarQube MySQL Database Migrator
 * Copyright (C) 2019-2019 SonarSource SA
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

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class StatsRecorderTest {

  private final StatsRecorder underTest = new StatsRecorder();

  @Test
  public void format_table_with_small_data() {
    underTest.add("foo", 1, 1, 2);
    assertThat(underTest.formatAsTable()).isEqualTo(
      "Tables  Records  Seconds  \n" +
      "------  -------  -------  \n" +
      "foo           1      0.0  \n");
  }

  @Test
  public void format_table_with_large_data() {
    underTest.add("longer_table_name", 123456789, 1, 123456789000L);
    assertThat(underTest.formatAsTable()).isEqualTo(
      "Tables             Records    Seconds      \n" +
      "-----------------  ---------  -----------  \n" +
      "longer_table_name  123456789  123456789.0  \n");
  }

  @Test
  public void format_table_with_small_and_large_data() {
    underTest.add("foo", 1, 1, 7777);
    underTest.add("longer_table_name", 123456789, 1, 123456789000L);
    assertThat(underTest.formatAsTable()).isEqualTo(
      "Tables             Records    Seconds      \n" +
      "-----------------  ---------  -----------  \n" +
      "foo                        1          7.8  \n" +
      "longer_table_name  123456789  123456789.0  \n");
  }
}
