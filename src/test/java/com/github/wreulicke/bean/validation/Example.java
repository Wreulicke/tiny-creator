package com.github.wreulicke.bean.validation;

import java.util.List;

import javax.validation.constraints.NotNull;

public class Example {
  public static void staticMethod(@NotNull String notNullString) {}

  public void method(@NotNull String notNullString) {}

  public void thisAnnotated(@NotNull Example this,@NotNull String notNullString) {}

  @NotNull
  public void methodAnnotated(@NotNull Example this,@NotNull String notNullString) {}

  public List<@NotNull String> typeAnnotated() {
    return null;
  }
}
