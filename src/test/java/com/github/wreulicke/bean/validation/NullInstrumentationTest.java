package com.github.wreulicke.bean.validation;


import static org.assertj.core.api.Assertions.assertThat;

import javax.validation.constraints.NotNull;

import org.junit.Test;

import javassist.ClassPool;
import javassist.CtClass;
import javassist.bytecode.SignatureAttribute;
import javassist.bytecode.SignatureAttribute.MethodSignature;
import javassist.bytecode.SignatureAttribute.ObjectType;
import javassist.bytecode.SignatureAttribute.Type;
import javassist.bytecode.SignatureAttribute.TypeParameter;
import mockit.Mock;
import mockit.MockUp;

public class NullInstrumentationTest {
  @SuppressWarnings("unchecked")
  @Test
  public void test() throws Exception {
    new MockUp<Object>(Class.forName("javassist.CtClassType")) {
      @Mock
      void checkModify() {}

      @Mock
      public boolean isModified() {
        return true;
      }
    };
    NotNullInstrumentation instrumentation = new NotNullInstrumentation(Instruments.mock(Example.class));
    byte[] result = instrumentation.transform(getClass().getClassLoader(), Instruments.forInstruments(Example.class), null, null, ByteCodes
      .getByteCode(Example.class));
    ClassPool pool = ClassPool.getDefault();
    CtClass ctClass = pool.get(Example.class.getName());
    MethodSignature methodSignature = new MethodSignature(new TypeParameter[0], new Type[] {
      new SignatureAttribute.ClassType("java.lang.String")
    }, (Type) null, new ObjectType[0]);
    ctClass.getMethod("test", methodSignature.encode())
      .insertBefore("java.util.Objects.requireNonNull($1,\"notNullString\");");

    assertThat(result).isEqualTo(ctClass.toBytecode());

  }

  public static class Example {
    public static void test(@NotNull String notNullString) {}
  }
}
