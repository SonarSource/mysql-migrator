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

import java.util.function.Function;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class ArgumentsParserTest {

  @Test
  public void use_sensible_default_usage_line() {
    ArgumentsParser parser = ArgumentsParser.newBuilder().build();
    assertThat(parser.usageString()).isEqualTo("Usage: java -jar path/to/jar [-help] [OPTIONS...] [ARGS...]\n" +
      "\n" +
      "Options:\n" +
      "\n" +
      "-help\n" +
      "  Print this help\n");
  }

  @Test
  public void use_specified_usage_line() {
    String customUsageLine = "foo";
    ArgumentsParser parser = ArgumentsParser.newBuilder().setUsageLine(customUsageLine).build();
    assertThat(parser.usageString()).startsWith(customUsageLine + "\n");
  }

  @Test
  public void parse_arguments_with_no_errors() {
    ArgumentsParser.Result result = ArgumentsParser.newBuilder().build().parseArgs(new String[0]);
    assertThat(result.isValid()).isTrue();
  }

  @Test
  public void raise_error_for_unexpected_arguments() {
    ArgumentsParser.Result result = ArgumentsParser.newBuilder().build().parseArgs(new String[]{"foo"});
    assertThat(result.isValid()).isFalse();
    assertThat(result.errorString()).startsWith("Unexpected arguments: ");
  }

  @Test
  public void error_message_explicitly_names_unexpected_arguments() {
    ArgumentsParser.Result result = ArgumentsParser.newBuilder().build().parseArgs(new String[]{"foo", "bar"});
    assertThat(result.isValid()).isFalse();
    assertThat(result.errorString()).startsWith("Unexpected arguments: foo, bar");
  }

  @Test(expected = IllegalStateException.class)
  public void calling_errorString_raises_error_when_unexpected_because_result_is_actually_valid() {
    ArgumentsParser.Result result = ArgumentsParser.newBuilder().build().parseArgs(new String[0]);
    assertThat(result.isValid()).isTrue();
    result.errorString();
  }

  @Test(expected = IllegalArgumentException.class)
  public void raise_error_when_same_option_specified_multiple_times() {
    ArgumentsParser.Builder builder = ArgumentsParser.newBuilder();
    builder.addOption("foo", "bar", "baz", null);
    builder.addOption("foo", "bar", "baz", null);
  }

  @Test
  public void apply_validator() {
    Validator<String> validator = Validators.create(Function.identity());

    ArgumentsParser.Result result = ArgumentsParser.newBuilder()
      .addOption("-foo", "bar", "dummy description", validator)
      .build()
      .parseArgs(new String[]{"-foo", "bar"});

    assertThat(result.isValid()).isTrue();
    assertThat(validator.used()).isTrue();
    assertThat(validator.value()).isEqualTo("bar");
  }

  @Test
  public void raise_error_if_option_is_specified_multiple_times() {
    Validator<String> validator = Validators.create(Function.identity());

    ArgumentsParser.Result result = ArgumentsParser.newBuilder()
      .addOption("-foo", "bar", "dummy description", validator)
      .build()
      .parseArgs(new String[]{"-foo", "bar", "-foo", "baz"});

    assertThat(result.isValid()).isFalse();
    assertThat(result.errorString()).isEqualTo("Option -foo was already specified");
    assertThat(validator.used()).isFalse();
  }

  @Test
  public void raise_error_if_option_value_missing() {
    Validator<String> validator = Validators.create(Function.identity());

    ArgumentsParser.Result result = ArgumentsParser.newBuilder()
      .addOption("-foo", "bar", "dummy description", validator)
      .build()
      .parseArgs(new String[]{"-foo"});

    assertThat(result.isValid()).isFalse();
    assertThat(result.errorString()).isEqualTo("Option -foo requires a parameter");
    assertThat(validator.used()).isFalse();
  }

  @Test
  public void skip_validator_when_option_not_used() {
    Validator<String> validator = Validators.create(Function.identity());

    ArgumentsParser.Result result = ArgumentsParser.newBuilder()
      .addOption("-foo", "bar", "dummy description", validator)
      .build()
      .parseArgs(new String[0]);

    assertThat(result.isValid()).isTrue();
    assertThat(validator.used()).isFalse();
  }

  @Test
  public void report_validator_failure_as_error() {
    String validationErrorMessage = "bad argument";
    Validator<String> validator = Validators.create(s -> {
      throw new IllegalArgumentException(validationErrorMessage);
    });

    ArgumentsParser.Result result = ArgumentsParser.newBuilder()
      .addOption("-foo", "bar", "dummy description", validator)
      .build()
      .parseArgs(new String[]{"-foo", "bar"});

    assertThat(result.isValid()).isFalse();
    assertThat(result.errorString()).isEqualTo(validationErrorMessage);
    assertThat(validator.used()).isTrue();
  }

  @Test(expected = IllegalStateException.class)
  public void raise_error_when_trying_to_access_value_of_unused_validator() {
    Validator<String> validator = Validators.create(Function.identity());

    ArgumentsParser.Result result = ArgumentsParser.newBuilder()
      .addOption("-foo", "bar", "dummy description", validator)
      .build()
      .parseArgs(new String[0]);

    assertThat(result.isValid()).isTrue();
    assertThat(validator.used()).isFalse();
    validator.value();
  }

  @Test
  public void skip_validators_when_help_requested() {
    ArgumentsParser.Result result = ArgumentsParser.newBuilder()
      .build()
      .parseArgs(new String[]{"-foo", "bar", "-help"});

    assertThat(result.isHelpRequested()).isTrue();
  }

  @Test
  public void report_global_validator_failure_as_error() {
    String validationErrorMessage = "bad argument";
    ArgumentsParser.Result result = ArgumentsParser.newBuilder()
      .addGlobalValidator(rawOptions -> {
        throw new IllegalStateException(validationErrorMessage);
      })
      .build()
      .parseArgs(new String[0]);

    assertThat(result.isValid()).isFalse();
    assertThat(result.errorString()).isEqualTo(validationErrorMessage);
  }

  @Test
  public void skip_global_validators_when_help_requested() {
    ArgumentsParser.Result result = ArgumentsParser.newBuilder()
      .addGlobalValidator(rawOptions -> {
        throw new IllegalStateException();
      })
      .build()
      .parseArgs(new String[]{"-foo", "bar", "-help"});

    assertThat(result.isHelpRequested()).isTrue();
  }

  @Test
  public void raise_error_for_unknown_option() {
    ArgumentsParser.Result result = ArgumentsParser.newBuilder().build().parseArgs(new String[]{"-foo", "bar"});
    assertThat(result.isValid()).isFalse();
    assertThat(result.errorString()).isEqualTo("Unknown option: -foo");
  }

  @Test
  public void report_when_help_requested() {
    ArgumentsParser.Result result = ArgumentsParser.newBuilder().build().parseArgs(new String[]{"-help"});
    assertThat(result.isHelpRequested()).isTrue();
  }

  @Test(expected = IllegalStateException.class)
  public void raise_error_for_isValid_when_help_requested() {
    ArgumentsParser.Result result = ArgumentsParser.newBuilder().build().parseArgs(new String[]{"-help"});
    assertThat(result.isHelpRequested()).isTrue();
    result.isValid();
  }
}
