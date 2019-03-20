/*
 * SonarQube MySQL Database Migrator
 * Copyright (C) 2013-2019 SonarSource SA
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
package com.sonar.dbcopy.prepare;

import com.tngtech.java.junit.dataprovider.DataProvider;
import com.tngtech.java.junit.dataprovider.DataProviderRunner;
import com.tngtech.java.junit.dataprovider.UseDataProvider;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import org.junit.Test;
import org.junit.runner.RunWith;

import static com.sonar.dbcopy.prepare.Arguments.DEST_URL_OPTION;
import static com.sonar.dbcopy.prepare.Arguments.HELP_OPTION;
import static com.sonar.dbcopy.prepare.Arguments.SOURCE_URL_OPTION;
import static com.sonar.dbcopy.prepare.Arguments.VERSION_OPTION;
import static org.fest.assertions.Assertions.assertThat;

@RunWith(DataProviderRunner.class)
public class ArgumentsTest {

  private final Arguments underTest = new Arguments();

  @Test
  public void valid_args() {
    underTest.parseArgs(newValidArgsBuilder().args());
    assertThat(underTest.isValid()).isTrue();
  }

  @Test
  public void fails_when_required_options_are_missing() {
    underTest.parseArgs(new String[0]);

    assertThat(underTest.isValid()).isFalse();
    assertThat(underTest.getErrors()).containsExactly(
      "Missing required option: -urlSrc",
      "Missing required option: -userSrc",
      "Missing required option: -pwdSrc",
      "Missing required option: -urlDest",
      "Missing required option: -userDest",
      "Missing required option: -pwdDest"
    );
  }

  @Test
  @UseDataProvider("requiredOption")
  public void fails_when_any_required_option_is_missing(String name) {
    underTest.parseArgs(newValidArgsBuilder().removeOption(name).args());

    assertThat(underTest.isValid()).isFalse();
    assertThat(underTest.getErrors()).containsExactly("Missing required option: " + name);
  }

  @DataProvider
  public static Object[][] requiredOption() {
    return Stream.of(Arguments.OptionDescriptor.values())
      .filter(desc -> desc.required)
      .map(desc -> new Object[]{"-" + desc.name})
      .toArray(Object[][]::new);
  }

  @Test
  @UseDataProvider("invalidJdbcUrl")
  public void fails_when_urlSrc_is_invalid(String invalidJdbcUrl) {
    underTest.parseArgs(newValidArgsBuilder().setOption("-" + SOURCE_URL_OPTION, invalidJdbcUrl).args());

    assertThat(underTest.isValid()).isFalse();
    assertThat(underTest.getErrors()).containsExactly("Not a valid Jdbc Url: " + invalidJdbcUrl);
  }

  @Test
  @UseDataProvider("invalidJdbcUrl")
  public void fails_when_urlDest_is_invalid(String invalidJdbcUrl) {
    underTest.parseArgs(newValidArgsBuilder().setOption("-" + DEST_URL_OPTION, invalidJdbcUrl).args());

    assertThat(underTest.isValid()).isFalse();
    assertThat(underTest.getErrors()).containsExactly("Not a valid Jdbc Url: " + invalidJdbcUrl);
  }

  @DataProvider
  public static Object[][] invalidJdbcUrl() {
    return new Object[][] {
      {"foo"},
      {"foo:bar"},
      {"foo:bar:baz"},
    };
  }

  @Test
  @UseDataProvider("unsupportedSourceJdbcUrl")
  public void fails_when_urlSrc_is_unsupported(String unsupportedSourceJdbcUrl) {
    underTest.parseArgs(newValidArgsBuilder().setOption("-" + SOURCE_URL_OPTION, unsupportedSourceJdbcUrl).args());

    assertThat(underTest.isValid()).isFalse();

    String database = unsupportedSourceJdbcUrl.split(":")[1];
    assertThat(underTest.getErrors()).containsExactly("Unsupported database: " + database + ". Supported databases are: [mysql]");
  }

  @DataProvider
  public static Object[][] unsupportedSourceJdbcUrl() {
    return new Object[][] {
      {"jdbc:bar:baz"},
      {"jdbc:postgresql:foo"},
      {"jdbc:oracle:foo"},
      {"jdbc:sqlserver:foo"},
    };
  }

  @Test
  @UseDataProvider("unsupportedDestJdbcUrl")
  public void fails_when_urlDest_is_unsupported(String unsupportedDestJdbcUrl) {
    underTest.parseArgs(newValidArgsBuilder().setOption("-" + DEST_URL_OPTION, unsupportedDestJdbcUrl).args());

    assertThat(underTest.isValid()).isFalse();

    String database = unsupportedDestJdbcUrl.split(":")[1];
    assertThat(underTest.getErrors()).containsExactly("Unsupported database: " + database + ". Supported databases are: [postgresql, oracle, sqlserver]");
  }

  @DataProvider
  public static Object[][] unsupportedDestJdbcUrl() {
    return new Object[][] {
      {"jdbc:bar:baz"},
      {"jdbc:mysql:baz"},
    };
  }

  @Test
  public void do_not_fail_when_urlSrc_is_supported() {
    String supportedSourceJdbcUrl = "jdbc:mysql:foo";
    underTest.parseArgs(newValidArgsBuilder().setOption("-" + SOURCE_URL_OPTION, supportedSourceJdbcUrl).args());

    assertThat(underTest.isValid()).isTrue();
    assertThat(underTest.getErrors()).isEmpty();
  }

  @Test
  @UseDataProvider("supportedDestJdbcUrl")
  public void do_not_fail_when_urlDest_is_supported(String supportedDestJdbcUrl) {
    underTest.parseArgs(newValidArgsBuilder().setOption("-" + DEST_URL_OPTION, supportedDestJdbcUrl).args());

    assertThat(underTest.isValid()).isTrue();
    assertThat(underTest.getErrors()).isEmpty();
  }

  @DataProvider
  public static Object[][] supportedDestJdbcUrl() {
    return new Object[][] {
      {"jdbc:postgresql:foo"},
      {"jdbc:oracle:foo"},
      {"jdbc:sqlserver:foo"},
    };
  }

  @Test
  public void helpRequested() {
    underTest.parseArgs(newValidArgsBuilder().args());
    assertThat(underTest.helpRequested()).isFalse();

    Arguments argumentsWithHelp = new Arguments();
    argumentsWithHelp.parseArgs(newValidArgsBuilder().setFlag("-" + HELP_OPTION).args());
    assertThat(argumentsWithHelp.helpRequested()).isTrue();
  }

  @Test
  public void versionRequested() {
    underTest.parseArgs(newValidArgsBuilder().args());
    assertThat(underTest.versionRequested()).isFalse();

    Arguments argumentsWithVersion = new Arguments();
    argumentsWithVersion.parseArgs(newValidArgsBuilder().setFlag("-" + VERSION_OPTION).args());
    assertThat(argumentsWithVersion.versionRequested()).isTrue();
  }

  private ArgsBuilder newValidArgsBuilder() {
    String[] options = {
      "-driverSrc", "org.h2.Driver",
      "-urlSrc", "jdbc:h2:mem:StartAppTest_Source_Version_1_DB;DB_CLOSE_ON_EXIT=-1;",
      "-userSrc", "sonar",
      "-pwdSrc", "sonar",
      "-urlDest", "jdbc:h2:mem:StartAppTest_Destination_Version_1_DB;DB_CLOSE_ON_EXIT=-1;",
      "-userDest", "sonar",
      "-pwdDest", "sonar"
    };

    ArgsBuilder argsBuilder = new ArgsBuilder();
    IntStream.range(0, options.length / 2).forEach(i -> argsBuilder.setOption(options[2 * i], options[2 * i + 1]));
    return argsBuilder;
  }

  private static class ArgsBuilder {
    private final Set<String> flags = new HashSet<>();
    private final Map<String, String> options = new HashMap<>();

    ArgsBuilder setFlag(String name) {
      flags.add(name);
      return this;
    }

    ArgsBuilder setOption(String name, String value) {
      options.put(name, value);
      return this;
    }

    ArgsBuilder removeOption(String name) {
      options.remove(name);
      return this;
    }

    String[] args() {
      List<String> args = new ArrayList<>(flags);
      options.forEach((name, value) -> {
        args.add(name);
        args.add(value);
      });

      return args.toArray(new String[0]);
    }
  }

}
