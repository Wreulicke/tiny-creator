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


import static com.github.wreulicke.bean.validation.ByteCodes.getByteCode;
import static com.github.wreulicke.bean.validation.Instruments.forInstruments;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import javassist.ClassPool;
import mockit.Mock;
import mockit.MockUp;

public class NullInstrumentationTest {

  @BeforeClass
  public static void setup() {

  }

  @AfterClass
  public static void tearDown() {

  }

  @SuppressWarnings({
    "unchecked",
    "rawtypes"
  })
  @Test
  public void test() throws Exception {
    new MockUp(Class.forName("javassist.CtClassType")) {
      @Mock
      public boolean isFrozen() {
        return false;
      }

      @Mock
      void checkModify() {}

      @Mock
      public boolean isModified() {
        return true;
      }
    };
    NotNullInstrumentation inst = new NotNullInstrumentation(ClassPool.getDefault());
    byte[] result = inst.transform(getClass().getClassLoader(), forInstruments(Example.class), null, null, getByteCode(Example.class));

    // Path root = Paths.get(".")
    // .toRealPath()
    // .toAbsolutePath();
    // Path temp = Files.createTempDirectory(root, "temp");
    // Decompiler decompiler = new FernflowerDecompiler();
    // Path path = Files.createTempFile(temp, "Example", ".class");
    // ByteCodes.dumpByteCode(path, result);
    //
    // DecompilationResult decompilationResult = decompiler.decompileClassFile(root, path, temp);
    // Optional<String> first = decompilationResult.getDecompiledFiles()
    // .values()
    // .stream()
    // .findFirst();
    // assertThat(first).matches(Optional::isPresent);
    //
    // Files.walk(temp)
    // .map(Path::toFile)
    // .forEach(File::deleteOnExit);



  }
}
