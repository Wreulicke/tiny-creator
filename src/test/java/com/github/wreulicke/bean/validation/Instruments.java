package com.github.wreulicke.bean.validation;

public class Instruments {
  public static String forInstruments(String className) {
    return className.replaceAll("\\,", "/");
  }

  public static String forInstruments(Class<?> clazz) {
    return forInstruments(clazz.getName());
  }
}
