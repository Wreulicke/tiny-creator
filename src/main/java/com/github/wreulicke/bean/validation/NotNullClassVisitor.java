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

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.TypePath;

public class NotNullClassVisitor extends ClassVisitor {

  public final class MyMethodVisitor extends MethodVisitor {
    public boolean isStatic;
    public List<Runnable> list = new ArrayList<>();
    public Map<Integer, String> localVariableTable = new HashMap<>();

    public MyMethodVisitor(int api, MethodVisitor mv, boolean b) {
      super(api, mv);
      this.isStatic = b;
    }

    @Override
    public void visitEnd() {
      list.forEach(Runnable::run);
      super.visitEnd();
    }

    @Override
    public void visitCode() {
      super.visitCode();
    }

    @Override
    public AnnotationVisitor visitParameterAnnotation(int parameter, String desc, boolean visible) {
      AnnotationVisitor visitor = super.visitParameterAnnotation(parameter, desc, visible);
      System.out.println("visitParameteraaaa");
      if (visible) {
        if (Objects.equals(desc, "Ljavax/validation/constraints/NotNull;")) {
          list.add(() -> {
            int index = parameter + (isStatic ? 0 : 1);
            super.visitVarInsn(Opcodes.ALOAD, index);
            String name = localVariableTable.get(index);
            super.visitLdcInsn("required " + ((name == null) ? "unknownName" : name));
            super.visitMethodInsn(Opcodes.INVOKESTATIC, "java/util/Objects", "requireNonNull",
              "(Ljava/lang/Object;Ljava/lang/String;)Ljava/lang/Object", false);
          });
        }
      }
      return visitor;
    }

    @Override
    public void visitLocalVariable(String name, String desc, String signature, Label start, Label end, int index) {
      System.out.println("name:" + name + " index:" + index);
      localVariableTable.put(index, name);
      super.visitLocalVariable(name, desc, signature, start, end, index);
    }

    @Override
    public AnnotationVisitor visitTypeAnnotation(int typeRef, TypePath typePath, String desc, boolean visible) {
      if (typeRef >>> 24 == 0x15) {
        System.out.printf("typeRef:%s, typePath:%s, desc:%s \r\n", typeRef, typePath, desc);
        if (Objects.equals(desc, "Ljavax/validation/constraints/NotNull;")) {
          super.visitVarInsn(Opcodes.ALOAD, 0);
          super.visitLdcInsn("required this");
          super.visitMethodInsn(Opcodes.INVOKESTATIC, "java/util/Objects", "requireNonNull",
            "(Ljava/lang/Object;Ljava/lang/String;)Ljava/lang/Object", false);
        }
      }
      return super.visitTypeAnnotation(typeRef, typePath, desc, visible);
    }
  }

  public NotNullClassVisitor(int api, ClassWriter writer) {
    super(api, writer);
  }

  @Override
  public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
    System.out.printf("access:%s, name:%s, desc:%s, signature:%s \r\n", access, name, desc, signature);
    return new MyMethodVisitor(Opcodes.ASM5, this.cv.visitMethod(access, name, desc, signature, exceptions), Modifier.isStatic(access));
  }
}
