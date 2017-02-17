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
package com.github.wreulicke.prop;

import java.io.File;
import java.net.URI;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;
import java.util.function.Function;
import java.util.regex.Pattern;

import com.github.wreulicke.prop.internal.UnsafeSuppliers;

public class PropertyCreator {
  private static Map<Class<?>, Function<String, ?>> supplerMap = new HashMap<>();
  static {
    supplerMap.put(Path.class, Paths::get);
    supplerMap.put(File.class, File::new);
    supplerMap.put(String.class, (s) -> s);
    supplerMap.put(URL.class, UnsafeSuppliers.unsafed(URL::new));
    supplerMap.put(URI.class, UnsafeSuppliers.unsafed(URI::new));
    supplerMap.put(Pattern.class, Pattern::compile);
    supplerMap.put(Locale.class, Locale::new);
    supplerMap.put(TimeZone.class, TimeZone::getTimeZone);
  }

  public <T> T create(Class<T> interfaceType) {
    if (!interfaceType.isInterface()) {
      throw new RuntimeException("cannot create. use interface");
    }
    return null;
  }
}
