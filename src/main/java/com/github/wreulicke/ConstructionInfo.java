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

import java.beans.ConstructorProperties;
import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class ConstructionInfo<T> {
  public final String[] names;
  public final Map<String, Class<?>> namedType;

  public ConstructionInfo(Constructor<T> constructor) {
    ConstructorProperties properties = Objects.requireNonNull(constructor.getAnnotation(ConstructorProperties.class),
      "construction target is not annotated ConstructorProperties");

    String[] names = properties.value();

    namedType = new HashMap<>();

    Class<?>[] types = constructor.getParameterTypes();
    for (int i = 0; i < names.length; i++) {
      namedType.put(names[i], types[i]);
    }

    this.names = names;
  }

  public Class<?> getType(String name) {
    return Objects.requireNonNull(namedType.get(name), "cannot find name:" + name + "type");
  }

  public <E> boolean isAssignable(String name, Object element) {
    return getType(name).isInstance(element);
  }


}
