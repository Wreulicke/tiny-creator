package com.github.wreulicke.bean.validation;

import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.atomic.AtomicBoolean;

import javassist.ClassPool;
import javassist.CtClass;
import javassist.NotFoundException;

public class Instruments {
  public static String forInstruments(String className) {
    return className.replaceAll("\\,", "/");
  }

  public static String forInstruments(Class<?> clazz) {
    return forInstruments(clazz.getName());
  }

  public static ClassPool mock(Class<?> clazz) {
    String clazzName = clazz.getName();
    AtomicBoolean isCreated = new AtomicBoolean(false);
    return new ClassPool(ClassPool.getDefault()) {
      @Override
      public CtClass makeClass(InputStream classfile, boolean ifNotFrozen) throws IOException, RuntimeException {
        isCreated.set(true);
        return super.makeClass(classfile, false);
      }


      @Override
      protected synchronized CtClass get0(String classname, boolean useCache) throws NotFoundException {
        if (!isCreated.get() && clazzName.equals(classname))
          return null;
        return super.get0(classname, useCache);
      }
    };
  }
}
