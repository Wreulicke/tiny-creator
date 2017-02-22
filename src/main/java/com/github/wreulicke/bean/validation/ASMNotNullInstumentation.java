/**
 * MIT License
 *
 * Copyright (c) 2017 Wreulicke
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package com.github.wreulicke.bean.validation;

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.lang.reflect.Modifier;
import java.security.ProtectionDomain;
import java.util.List;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.TypeReference;
import org.objectweb.asm.tree.AnnotationNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.LdcInsnNode;
import org.objectweb.asm.tree.LocalVariableNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.TypeAnnotationNode;
import org.objectweb.asm.tree.VarInsnNode;

public class ASMNotNullInstumentation implements ClassFileTransformer {
  @Override
  public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain,
    byte[] classfileBuffer) throws IllegalClassFormatException {
    ClassWriter writer = new ClassWriter(0);
    ClassReader reader = new ClassReader(classfileBuffer);
    ClassNode node = new ClassNode();
    reader.accept(node, ClassReader.EXPAND_FRAMES);
    new InnerNotNullInstrumentation(node).visit();
    node.accept(writer);
    return writer.toByteArray();
  }

  private static class InnerNotNullInstrumentation {
    ClassNode node;

    public InnerNotNullInstrumentation(ClassNode node) {
      this.node = node;
    }

    public void visit() {
      @SuppressWarnings("unchecked")
      List<MethodNode> nodes = node.methods;
      nodes.forEach(this::accept);
    }

    @SuppressWarnings("unchecked")
    public void accept(MethodNode method) {
      InsnList insnList = new InsnList();

      List<TypeAnnotationNode> typeAnnotations = method.visibleTypeAnnotations;
      if (typeAnnotations != null) {
        typeAnnotations.stream()
          .filter((annotation) -> {
            return new TypeReference(annotation.typeRef).getSort() == TypeReference.METHOD_RECEIVER && "Ljavax/validation/constraints/NotNull;"
              .equals(annotation.desc);
          })
          .findFirst()
          .ifPresent((annotation) -> {
            insnList.add(new VarInsnNode(Opcodes.ALOAD, 0));
            insnList.add(new LdcInsnNode("this is required"));
            insnList.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "java/util/Objects", "requireNonNull",
              "(Ljava/lang/Object;Ljava/lang/String;)Ljava/lang/Object", false));
          });
      }
      List<AnnotationNode>[] parameters = method.visibleParameterAnnotations;
      if (parameters != null) {
        for (int i = 0; i < parameters.length; i++) {
          final int index = i + (Modifier.isStatic(method.access) ? 0 : 1);
          List<AnnotationNode> annotations = parameters[i];
          annotations.stream()
            .map((annnotation) -> annnotation.desc)
            .filter("Ljavax/validation/constraints/NotNull;"::equals)
            .findFirst()
            .ifPresent((found) -> {
              LocalVariableNode localVar = (LocalVariableNode) method.localVariables.get(index);
              insnList.add(new VarInsnNode(Opcodes.ALOAD, index));
              insnList.add(new LdcInsnNode(localVar.name + " is required"));
              insnList.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "java/util/Objects", "requireNonNull",
                "(Ljava/lang/Object;Ljava/lang/String;)Ljava/lang/Object", false));
            });;
        }
      }
      method.instructions.add(insnList);
    }
  }
}
