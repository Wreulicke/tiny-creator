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

import java.lang.instrument.ClassDefinition;
import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.Instrumentation;
import java.security.ProtectionDomain;

import org.junit.Test;

import com.ea.agentloader.AgentLoader;

public class NullInstrumentationTest2 {
  private static byte[] before;

  public static class HelloAgent {
    public static void agentmain(String agentArgs, Instrumentation inst) {
      ClassFileTransformer transformer = (loader, className, classBeingRedefined, protectionDomain, classfileBuffer) -> {
        System.out.println("test:" + className);
        if (className.equals("com.github.wreulicke.bean.validation.Example".replaceAll("\\.", "/"))) {
          ASMNotNullInstrumentation instrumentation = new ASMNotNullInstrumentation();
          before = classfileBuffer;
          return instrumentation.transform(loader, className, classBeingRedefined, protectionDomain, classfileBuffer);
        }
        return classfileBuffer;
      };
      inst.addTransformer((loader, className, classBeingRedefined, protectionDomain, classfileBuffer) -> {
        try {
          return transformer.transform(loader, className, classBeingRedefined, protectionDomain, classfileBuffer);
        } finally {
          inst.removeTransformer(transformer);
        }
      });
      try {
        inst.redefineClasses(new ClassDefinition(Example.class, ByteCodes.getByteCode(Example.class)));
      } catch (Throwable e) {
        e.printStackTrace();
      }
    }
  }
  public static class RestoreAgent {
    public static void agentmain(String agentArgs, Instrumentation inst) {
      inst.addTransformer((ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain,
        byte[] classfileBuffer) -> {
        System.out.println("test:" + className);
        if (className.equals("com.github.wreulicke.bean.validation.Example".replaceAll("\\.", "/"))) {
          return ByteCodes.getByteCode(Example.class);
        }
        return classfileBuffer;
      });
      try {
        inst.retransformClasses(Example.class);
        inst.redefineClasses(new ClassDefinition(Example.class, ByteCodes.getByteCode(Example.class)));
      } catch (Throwable e) {
        e.printStackTrace();
      }
    }

  }

  @Test
  public void test() {
    System.out.println("hello agent load");
    AgentLoader.loadAgentClass(HelloAgent.class.getName(), "Hello!");
    try {
      new Example().method(null);
      fail("cannot reach here");
    } catch (NullPointerException e) {
    }
    System.gc();
    AgentLoader.loadAgentClass(RestoreAgent.class.getName(), "test", null, true, true, false);
    new Example().method(null);

  }
}
