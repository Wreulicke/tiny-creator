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

import static org.objectweb.asm.Opcodes.INVOKEINTERFACE;
import static org.objectweb.asm.Opcodes.INVOKESTATIC;

import java.lang.reflect.Modifier;
import java.util.List;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.LdcInsnNode;
import org.objectweb.asm.tree.LocalVariableNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.TypeInsnNode;
import org.objectweb.asm.tree.VarInsnNode;

public class ASMMethodParameterValidationInjector implements Injector {

  @Override
  public void inject(ClassNode classNode) {
    @SuppressWarnings("unchecked")
    List<MethodNode> methods = classNode.methods;
    methods.forEach((m) -> inject(m, classNode));
  }

  private void inject(MethodNode node, ClassNode clazzNode) {
    if (Modifier.isStatic(node.access) || Modifier.isAbstract(node.access) || Modifier.isAbstract(node.access) || "<init>".equals(node.name) ||
      "<clinit>".equals(node.name)) {
      return;
    }

    InsnList list = new InsnList();
    int index = node.localVariables.size();
    list.add(new MethodInsnNode(INVOKESTATIC, "javax/validation/Validation", "buildDefaultValidatorFactory", "()Ljavax/validation/ValidatorFactory;",
      false));
    list.add(new MethodInsnNode(INVOKEINTERFACE, "javax/validation/ValidatorFactory", "getValidator", "()Ljavax/validation/Validator;", true));
    list.add(new MethodInsnNode(INVOKEINTERFACE, "javax/validation/Validator", "forExecutables",
      "()Ljavax/validation/executable/ExecutableValidator;", true));

    list.add(new VarInsnNode(Opcodes.ASTORE, index));
    list.add(new VarInsnNode(Opcodes.ALOAD, index));
    list.add(new VarInsnNode(Opcodes.ALOAD, 0));

    list.add(new LdcInsnNode(Type.getType("L" + clazzNode.name + ";")));
    list.add(new LdcInsnNode(node.name));

    @SuppressWarnings("unchecked")
    List<LocalVariableNode> variables = node.localVariables;
    Type[] args = Type.getArgumentTypes(node.desc);

    list.add(new LdcInsnNode(args.length));
    list.add(new TypeInsnNode(Opcodes.ANEWARRAY, "java/lang/Class"));

    for (int i = 0; i < args.length; i++) {
      int paramIndex = 1 + i;
      list.add(new InsnNode(Opcodes.DUP));
      list.add(new LdcInsnNode(i));
      list.add(new LdcInsnNode(Type.getType(variables.get(paramIndex).desc)));
      list.add(new InsnNode(Opcodes.AASTORE));
    }

    list.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL, "java/lang/Class", "getMethod",
      "(Ljava/lang/String;[Ljava/lang/Class;)Ljava/lang/reflect/Method;", false));

    list.add(new LdcInsnNode(args.length));
    list.add(new TypeInsnNode(Opcodes.ANEWARRAY, "java/lang/Object"));
    for (int i = 0; i < args.length; i++) {
      int paramIndex = i + 1;
      list.add(new InsnNode(Opcodes.DUP));
      list.add(new LdcInsnNode(i));
      list.add(new VarInsnNode(Opcodes.ALOAD, paramIndex));
      list.add(new InsnNode(Opcodes.AASTORE));
    }
    list.add(new InsnNode(Opcodes.ICONST_0));
    list.add(new TypeInsnNode(Opcodes.ANEWARRAY, "java/lang/Class"));
    list.add(new MethodInsnNode(INVOKEINTERFACE, "javax/validation/executable/ExecutableValidator", "validateParameters",
      "(Ljava/lang/Object;Ljava/lang/reflect/Method;[Ljava/lang/Object;[Ljava/lang/Class;)Ljava/util/Set;", true));
    list.add(new MethodInsnNode(INVOKESTATIC, "com/github/wreulicke/bean/validation/Constraints", "throwIfNeeded", "(Ljava/util/Set;)V", false));
    node.instructions.insert(list);
  }

}
