package com.github.wreulicke.bean.validation;

import static org.objectweb.asm.Opcodes.INVOKEINTERFACE;
import static org.objectweb.asm.Opcodes.INVOKESTATIC;

import java.io.PrintWriter;
import java.util.List;

import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.util.ASMifier;
import org.objectweb.asm.util.TraceClassVisitor;

public class MethodParameterValidationInjector implements Injector {

  @Override
  public void inject(ClassNode classNode) {
    @SuppressWarnings("unchecked")
    List<MethodNode> methods = classNode.methods;
    methods.forEach((m) -> inject(m, classNode));
    classNode.accept(new TraceClassVisitor(null, new ASMifier(), new PrintWriter(System.out)));
  }

  private void inject(MethodNode node, ClassNode clazzNode) {
    InsnList list = new InsnList();
    list.add(new MethodInsnNode(INVOKESTATIC, "javax/validation/Validation", "buildDefaultValidatorFactory", "()Ljavax/validation/ValidatorFactory;",
      false));
    list.add(new MethodInsnNode(INVOKEINTERFACE, "javax/validation/ValidatorFactory", "getValidator", "()Ljavax/validation/Validator;", true));
    list.add(new MethodInsnNode(INVOKEINTERFACE, "javax/validation/Validator", "forExecutables",
      "()Ljavax/validation/executable/ExecutableValidator;", true));
    node.localVariables.size();
  }

}
