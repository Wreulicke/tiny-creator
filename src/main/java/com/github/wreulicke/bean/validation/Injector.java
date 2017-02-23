package com.github.wreulicke.bean.validation;

import org.objectweb.asm.tree.ClassNode;

public interface Injector {
  public void inject(ClassNode classNode);
}
