package com.github.wreulicke.bean.validation;

import java.io.IOException;

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
}
