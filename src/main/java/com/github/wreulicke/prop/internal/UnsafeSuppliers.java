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
package com.github.wreulicke.prop.internal;

import java.lang.reflect.Field;
import java.util.function.Function;

import sun.misc.Unsafe;

public class UnsafeSuppliers {
  private final static Unsafe unsafe;

  static {
    try {
      Field unsafeField = Unsafe.class.getDeclaredField("theUnsafe");
      unsafeField.setAccessible(true);
      unsafe = (Unsafe) unsafeField.get(null);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  public static <A, B> Function<A, B> unsafed(UnsafedFunction<A, B, Exception> d) {
    return d;
  }

  @FunctionalInterface
  public static interface UnsafedFunction<T, R, E extends Exception> extends Function<T, R> {
    public R applyInternal(T t) throws E;

    @Override
    default R apply(T t) {
      try {
        return applyInternal(t);
      } catch (Exception e) {
        unsafe.throwException(e);
      }
      // Not Reach
      return null;
    };
  }
}
