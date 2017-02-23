package com.github.wreulicke.bean.validation;

import static com.github.wreulicke.bean.validation.ByteCodes.getByteCode;

import java.io.IOException;

import org.junit.Test;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.tree.ClassNode;

public class MethodParameterValidationInjectorTest {
  @Test
  public void firstCase() throws IOException {
    ClassWriter writer = new ClassWriter(0);
    ClassReader reader = new ClassReader(getByteCode(Example.class));
    ClassNode node = new ClassNode();
    reader.accept(node, ClassReader.EXPAND_FRAMES);
    MethodParameterValidationInjector injector = new MethodParameterValidationInjector();
    injector.inject(node);
    node.accept(writer);
    byte[] result = writer.toByteArray();
    ByteCodes.dumpAndDecomplie((p) -> p.resolve("Example.class"), result);
  }
}
