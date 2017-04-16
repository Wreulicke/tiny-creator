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

import java.util.Set;
import java.util.stream.Collectors;

import javax.validation.ConstraintViolation;


public class Constraints {
  public static <T> void throwIfNeeded(Set<ConstraintViolation<T>> violations) {
    if (violations.size() == 0) {
      return;
    }
    String message = violations.stream()
      .map((m) -> m.getPropertyPath() + " : " + m.getMessage())
      .collect(Collectors.joining("\r\n"));
    throw new ConstraintException("occured constraint exception" + "\r\n" + message);
  }

  public static class ConstraintException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public ConstraintException(String message) {
      super(message);
    }
  }
}
