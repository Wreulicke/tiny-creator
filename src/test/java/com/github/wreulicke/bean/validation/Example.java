/**
 * MIT License
 *
 * Copyright (c) 2017 Wreulicke
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package com.github.wreulicke.bean.validation;

import java.util.List;

import javax.validation.Validation;
import javax.validation.constraints.NotNull;
import javax.validation.executable.ExecutableValidator;

public class Example {
  public static void staticMethod(@NotNull String notNullString) {}

  public void method(@NotNull String notNullString) {}

  public void thisAnnotated(@NotNull Example this,@NotNull String notNullString) {
    try {
      ExecutableValidator validator = Validation.buildDefaultValidatorFactory()
        .getValidator()
        .forExecutables();
      validator.validateParameters(this, Example.class.getMethod("thisAnnotated", String.class), new Object[] {
        notNullString
      })
        .stream()
        .findFirst()
        .ifPresent((x) -> {
          throw new RuntimeException(x.toString());
        });
    } catch (Exception e) {
      throw new RuntimeException(e);
    }

  }

  public static void main(String[] args) {
    new Example().thisAnnotated(null);
  }

  public void methodAnnotated(@NotNull Example this,@NotNull String notNullString) {}

  public List<String> typeAnnotated() {
    return null;
  }
}
