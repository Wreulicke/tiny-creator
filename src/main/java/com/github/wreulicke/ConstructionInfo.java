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
    ConstructorProperties properties = Objects.requireNonNull(
      constructor.getAnnotation(ConstructorProperties.class),
      "construction target is not annotated ConstructorProperties");
    
    String[] names=properties.value();
    
    namedType=new HashMap<>();
    
    Class<?>[] types = constructor.getParameterTypes();
    for (int i = 0; i < names.length; i++) {
      namedType.put(names[i], types[i]);
    }
    
    this.names=names;
  }

  public Class<?> getType(String name) {
    return Objects.requireNonNull(namedType.get(name), "cannot find name:"+name+"type");
  }
  
  public <E> boolean isAssignable(String name, Object element){
    return getType(name).isInstance(element);
  }
  
  
}
