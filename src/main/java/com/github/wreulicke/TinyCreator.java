package com.github.wreulicke;

import java.lang.reflect.Constructor;
import java.util.Arrays;
import java.util.Map;
import java.util.Optional;

public class TinyCreator {
  
  @SuppressWarnings("unchecked")
  public <T> T create(Class<T> clazz, Map<String, ?> parameter) {
    Constructor<T>[] ctors = (Constructor<T>[]) clazz.getConstructors();
    MapConstructor<T> mapConstructor = new MapConstructor<>(parameter);
    Optional<Constructor<T>> constructor = Arrays.stream(ctors)
      .filter(mapConstructor::available)
      .findFirst();
    return constructor
      .map(mapConstructor::create)
      .orElseThrow(()->new RuntimeException("cannot instantiate"));
  }
}
