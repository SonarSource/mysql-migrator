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
package org.sonarsource.sqdbmigrator.argsparser;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class GlobalValidators {
  private GlobalValidators() {
    // utility class, forbidden constructor
  }

  public static GlobalValidator allPresent(String... names) {
    return rawOptions -> {
      List<String> missing = Stream.of(names).filter(name -> !rawOptions.containsKey(name)).collect(Collectors.toList());
      if (!missing.isEmpty()) {
        fail(String.format("Missing required %s: %s",
          pluralize("option", missing.size()),
          String.join(", ", missing)));
      }
    };
  }

  private static String pluralize(String name, int count) {
    return count == 1 ? name : name + "s";
  }

  public static void fail(String message) {
    throw new IllegalStateException(message);
  }
}
