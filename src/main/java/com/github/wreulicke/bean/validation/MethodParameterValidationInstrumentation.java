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

import javassist.CannotCompileException;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtMethod;

public class MethodParameterValidationInstrumentation implements ClassFileTransformer {
  private ClassPool pool;

  public MethodParameterValidationInstrumentation() {
    this(ClassPool.getDefault());
  }

  public MethodParameterValidationInstrumentation(ClassPool pool) {
    this.pool = pool;
  }

  @Override
  public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain,
    byte[] classfileBuffer) throws IllegalClassFormatException {
    try (ByteArrayInputStream stream = new ByteArrayInputStream(classfileBuffer)) {
      CtClass clazz = pool.makeClass(stream);
      for (CtMethod method : clazz.getDeclaredMethods()) {
        if (!Modifier.isStatic(method.getModifiers()) && !Modifier.isNative(method.getModifiers()) && !Modifier.isAbstract(method.getModifiers()))
          method.insertBefore(
            "javax.validation.executable.ExecutableValidator validator=javax.validation.Validation.buildDefaultValidatorFactory().getValidator().forExecutables();" +
              "java.lang.reflect.Method method=$class.getMethod(\"" + method.getName() + "\", $sig);" +
              "com.github.wreulicke.bean.validation.Constraints.throwIfNeeded(validator.validateParameters(this, method, $args, new Class[0]));");

      }
      return clazz.toBytecode();
    } catch (IOException | CannotCompileException e) {
      e.printStackTrace();
      throw new RuntimeException(e);
    }
  }

}
