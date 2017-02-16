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
package com.github.wreulicke;

import java.lang.reflect.Constructor;
import java.util.Arrays;
import java.util.Map;
import java.util.WeakHashMap;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;

@RequiredArgsConstructor
public class MapConstructor<T> {
  private final Map<String, ?> parameter;
  private final Map<Constructor<T>, ConstructionInfo<T>> cache = new WeakHashMap<>();

  public boolean available(Constructor<T> ctor) {
    ConstructionInfo<?> info = takeConstructionInfo(ctor);
    return parameter.entrySet()
      .stream()
      .allMatch((e) -> info.isAssignable(e.getKey(), e.getValue()));
  }

  @SneakyThrows
  public T create(Constructor<T> ctor) {
    ConstructionInfo<?> info = takeConstructionInfo(ctor);
    Object[] args = Arrays.stream(info.names)
      .map(parameter::get)
      .toArray();
    return ctor.newInstance(args);
  }

  private ConstructionInfo<T> takeConstructionInfo(Constructor<T> ctor) {
    ConstructionInfo<T> info = cache.get(ctor);
    return info == null ? new ConstructionInfo<>(ctor) : info;
  }

}
