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

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SeekableByteChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.function.UnaryOperator;

import org.jboss.windup.decompiler.api.DecompilationResult;
import org.jboss.windup.decompiler.api.Decompiler;
import org.jboss.windup.decompiler.fernflower.FernflowerDecompiler;

import javassist.CannotCompileException;
import javassist.ClassPool;
import javassist.NotFoundException;

public class ByteCodes {
  public static byte[] getByteCode(String className) {
    ClassPool pool = ClassPool.getDefault();
    try {
      return pool.get(className)
        .toBytecode();
    } catch (IOException | CannotCompileException | NotFoundException e) {
      throw new RuntimeException(e);
    }
  }

  public static byte[] getByteCode(Class<?> clazz) {
    return getByteCode(clazz.getName());
  }

  public static void dumpByteCode(Path path, byte[] bytes) throws IOException {
    try (SeekableByteChannel channel = Files.newByteChannel(path, StandardOpenOption.WRITE)) {
      channel.write(ByteBuffer.wrap(bytes));
    }

  }

  public static DecompilationResult dumpAndDecomplie(UnaryOperator<Path> pathGenerator, byte[] bytes) throws IOException {
    Path root = Paths.get(".")
      .toRealPath()
      .toAbsolutePath();
    Path temp = Files.createTempDirectory(root, "temp");
    Path path = pathGenerator.apply(temp);
    dumpByteCode(path, bytes);
    Decompiler decompiler = new FernflowerDecompiler();
    DecompilationResult decompilationResult = decompiler.decompileClassFile(root, path, temp);
    return decompilationResult;
  }
}
