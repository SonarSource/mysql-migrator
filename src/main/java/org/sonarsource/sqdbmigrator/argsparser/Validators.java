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

public class Validators {
  private Validators() {
    // utility class, forbidden constructor
  }

  /**
   * Create a validator using the specified function to perform the validation.
   * <p>
   * Check with .used() if the validator was actually used,
   * and then .value() to get the valid value.
   */
  public static <T> Validator<T> create(Function<String, T> validator) {
    return new Validator<T>() {
      boolean used = false;
      T value = null;

      @Override
      public boolean used() {
        return used;
      }

      @Override
      public T value() {
        if (!used) {
          throw new IllegalStateException("Validator was not used");
        }
        return value;
      }

      @Override
      public void validate(String rawValue) {
        used = true;
        value = validator.apply(rawValue);
      }
    };
  }
}
