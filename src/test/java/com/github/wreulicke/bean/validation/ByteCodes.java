package com.github.wreulicke.bean.validation;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SeekableByteChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

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
}
