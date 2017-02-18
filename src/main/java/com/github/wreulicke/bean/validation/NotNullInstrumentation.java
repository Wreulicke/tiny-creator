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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.lang.reflect.Modifier;
import java.security.ProtectionDomain;

import javax.validation.constraints.NotNull;

import javassist.CannotCompileException;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtMethod;
import javassist.bytecode.CodeAttribute;
import javassist.bytecode.LocalVariableAttribute;
import javassist.bytecode.MethodInfo;

public class NotNullInstrumentation implements ClassFileTransformer {
  public ClassPool pool;

  public NotNullInstrumentation(ClassPool pool) {
    this.pool = pool;
  }

  @Override
  public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain,
    byte[] classfileBuffer) throws IllegalClassFormatException {
    try (ByteArrayInputStream stream = new ByteArrayInputStream(classfileBuffer)) {
      CtClass clazz = pool.makeClass(stream);
      for (CtMethod method : clazz.getDeclaredMethods()) {
        MethodInfo info = method.getMethodInfo();
        CodeAttribute codeAttribute = info.getCodeAttribute();
        LocalVariableAttribute attribute = (LocalVariableAttribute) codeAttribute.getAttribute(LocalVariableAttribute.tag);
        Object[][] annotationArray = method.getParameterAnnotations();
        for (int i = 0; i < annotationArray.length; i++) {
          Object[] annotations = annotationArray[i];
          String name = attribute.variableName(i + (Modifier.isStatic(method.getModifiers()) ? 0 : 1));
          for (Object annotation : annotations) {
            if (annotation instanceof NotNull) {
              method.insertBefore("java.util.Objects.requireNonNull($" + (i + 1) + ",\"" + name + " is required\");");
            }
          }
        }
      }
      return clazz.toBytecode();
    } catch (IOException | ClassNotFoundException | CannotCompileException e) {
      throw new RuntimeException(e);
    }
  }


}
