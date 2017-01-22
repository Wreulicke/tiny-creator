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
