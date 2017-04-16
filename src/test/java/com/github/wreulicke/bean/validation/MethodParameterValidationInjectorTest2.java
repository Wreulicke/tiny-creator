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

import static org.junit.Assert.fail;

import java.io.IOException;
import java.lang.instrument.ClassDefinition;
import java.lang.instrument.Instrumentation;
import java.lang.instrument.UnmodifiableClassException;
import java.security.ProtectionDomain;

import org.junit.Test;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.tree.ClassNode;

import com.ea.agentloader.AgentLoader;
import com.github.wreulicke.bean.validation.Constraints.ConstraintException;

public class MethodParameterValidationInjectorTest2 {
  private static byte[] before;

  public static class InstrumentationAgent {
    public static void agentmain(String agentArgs, Instrumentation inst) {
      inst.addTransformer((ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain,
        byte[] classfileBuffer) -> {
        try {
          if ("com.github.wreulicke.bean.validation.Example".replaceAll("\\.", "/")
            .equals(className)) {
            MethodParameterValidationInjector instrumentation = new MethodParameterValidationInjector();
            before = classfileBuffer;
            ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_FRAMES + ClassWriter.COMPUTE_MAXS);
            ClassReader reader = new ClassReader(classfileBuffer);
            ClassNode node = new ClassNode();
            reader.accept(node, ClassReader.EXPAND_FRAMES);
            instrumentation.inject(node);
            node.accept(writer);
            return writer.toByteArray();
          }
        } catch (Exception e) {
          e.printStackTrace();
        }
        return classfileBuffer;
      });
    }
  }
  public static class RestoreAgent {
    public static void agentmain(String agentArgs, Instrumentation inst) {
      inst.addTransformer((ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain,
        byte[] classfileBuffer) -> {
        if (className.equals("com.github.wreulicke.bean.validation.Example".replaceAll("\\.", "/"))) {
          return before;
        }
        return classfileBuffer;
      });
      try {
        inst.redefineClasses(new ClassDefinition(Example.class, before));
      } catch (UnmodifiableClassException | ClassNotFoundException e) {
        e.printStackTrace();
      }
    }

  }

  @Test
  public void firstCase() throws IOException, NoSuchMethodException, SecurityException {
    try {
      AgentLoader.loadAgentClass(InstrumentationAgent.class.getName(), "test");
      new Example().testMethod(null, null);
      fail("not reach here");
    } catch (ConstraintException e) {
    } finally {
      AgentLoader.loadAgentClass(RestoreAgent.class.getName(), "");
    }
    new Example().testMethod(null, null);
  }
}
