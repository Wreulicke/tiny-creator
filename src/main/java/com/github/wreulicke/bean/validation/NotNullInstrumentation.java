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
import javassist.bytecode.ByteArray;
import javassist.bytecode.CodeAttribute;
import javassist.bytecode.ConstPool;
import javassist.bytecode.LocalVariableAttribute;
import javassist.bytecode.MethodInfo;
import javassist.bytecode.TypeAnnotationsAttribute;
import javassist.bytecode.annotation.Annotation;

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
        System.out.println("xxxxxxxxxxxxxxxxx:" + method.getName());
        MethodInfo info = method.getMethodInfo();
        CodeAttribute codeAttribute = info.getCodeAttribute();
        TypeAnnotationsAttribute attr = (TypeAnnotationsAttribute) info.getAttribute("RuntimeVisibleTypeAnnotations");
        LocalVariableAttribute attribute = (LocalVariableAttribute) codeAttribute.getAttribute(LocalVariableAttribute.tag);
        Object[][] annotationArray = method.getParameterAnnotations();
        attr.numAnnotations();
        try {
          Walker walker = new Walker(attr.get(), attr.getConstPool());
          walker.annotationArray();
        } catch (Exception e) {
          e.printStackTrace();
        }
        for (int i = 0; i < annotationArray.length; i++) {
          Object[] annotations = annotationArray[i];
          String name = attribute.variableName(i + (Modifier.isStatic(method.getModifiers()) ? 0 : 1));
          System.out.println(name + " found");
          for (Object annotation : annotations) {
            if (annotation instanceof NotNull) {
              method.insertBefore("System.out.println(" + name + ");");
              method.insertBefore("java.util.Objects.requireNonNull($" + (i + 1) + ",\"" + name + " is required\");");
            }
          }
        }
        System.out.println("xxxxxxxxxxxxxxxxx");
      }
      return clazz.toBytecode();
    } catch (IOException | ClassNotFoundException | CannotCompileException e) {
      e.printStackTrace();
      throw new RuntimeException(e);
    }
  }

  static class Walker {
    byte[] info;
    SubWalker sub;
    ConstPool pool;

    public Walker(byte[] info, ConstPool pool) {
      this.info = info;
      sub = new SubWalker(info);
      this.pool = pool;
    }

    Walker(byte[] attrInfo) {
      info = attrInfo;

    }

    final void parameters() throws Exception {
      int numParam = info[0] & 0xff;
      parameters(numParam, 1);
    }

    void parameters(int numParam, int pos) throws Exception {
      for (int i = 0; i < numParam; ++i)
        pos = annotationArray(pos);
    }

    final void annotationArray() throws Exception {
      annotationArray(0);
    }

    final int annotationArray(int pos) throws Exception {
      int num = ByteArray.readU16bit(info, pos);
      return annotationArray(pos + 2, num);
    }


    int annotationArray(int pos, int num) throws Exception {
      for (int i = 0; i < num; i++) {
        int targetType = info[pos] & 0xff;
        pos = sub.targetInfo(pos + 1, targetType);
        pos = sub.typePath(pos);
        System.out.println(targetType);
        pos = annotation(pos);
      }

      return pos;
    }

    final int annotation(int pos) throws Exception {
      int type = ByteArray.readU16bit(info, pos);
      int numPairs = ByteArray.readU16bit(info, pos + 2);
      return annotation(pos + 4, type, numPairs);
    }

    int annotation(int pos, int type, int numPairs) throws Exception {
      System.out.println(new Annotation(type, pool));
      for (int j = 0; j < numPairs; ++j)
        pos = memberValuePair(pos);

      return pos;
    }

    /**
     * {@code element_value_paris}
     */
    final int memberValuePair(int pos) throws Exception {
      int nameIndex = ByteArray.readU16bit(info, pos);
      return memberValuePair(pos + 2, nameIndex);
    }

    /**
     * {@code element_value_paris[]}
     */
    int memberValuePair(int pos, int nameIndex) throws Exception {
      return memberValue(pos);
    }

    /**
     * {@code element_value}
     */
    final int memberValue(int pos) throws Exception {
      int tag = info[pos] & 0xff;
      if (tag == 'e') {
        int typeNameIndex = ByteArray.readU16bit(info, pos + 1);
        int constNameIndex = ByteArray.readU16bit(info, pos + 3);
        enumMemberValue(pos, typeNameIndex, constNameIndex);
        return pos + 5;
      }
      else if (tag == 'c') {
        int index = ByteArray.readU16bit(info, pos + 1);
        classMemberValue(pos, index);
        return pos + 3;
      }
      else if (tag == '@')
        return annotationMemberValue(pos + 1);
      else if (tag == '[') {
        int num = ByteArray.readU16bit(info, pos + 1);
        return arrayMemberValue(pos + 3, num);
      }
      else { // primitive types or String.
        int index = ByteArray.readU16bit(info, pos + 1);
        constValueMember(tag, index);
        return pos + 3;
      }
    }

    /**
     * {@code const_value_index}
     */
    void constValueMember(int tag, int index) throws Exception {}

    /**
     * {@code enum_const_value}
     */
    void enumMemberValue(int pos, int typeNameIndex, int constNameIndex) throws Exception {}

    /**
     * {@code class_info_index}
     */
    void classMemberValue(int pos, int index) throws Exception {}

    /**
     * {@code annotation_value}
     */
    int annotationMemberValue(int pos) throws Exception {
      return annotation(pos);
    }

    /**
     * {@code array_value}
     */
    int arrayMemberValue(int pos, int num) throws Exception {
      for (int i = 0; i < num; ++i) {
        pos = memberValue(pos);
      }

      return pos;
    }
  }
  static class SubWalker {
    byte[] info;

    SubWalker(byte[] attrInfo) {
      info = attrInfo;
    }

    final int targetInfo(int pos, int type) throws Exception {
      switch (type) {
        case 0x00:
        case 0x01: {
          int index = info[pos] & 0xff;
          typeParameterTarget(pos, type, index);
          return pos + 1;
        }
        case 0x10: {
          int index = ByteArray.readU16bit(info, pos);
          supertypeTarget(pos, index);
          return pos + 2;
        }
        case 0x11:
        case 0x12: {
          int param = info[pos] & 0xff;
          int bound = info[pos + 1] & 0xff;
          typeParameterBoundTarget(pos, type, param, bound);
          return pos + 2;
        }
        case 0x13:
        case 0x14:
        case 0x15:
          emptyTarget(pos, type);
          return pos;
        case 0x16: {
          int index = info[pos] & 0xff;
          formalParameterTarget(pos, index);
          return pos + 1;
        }
        case 0x17: {
          int index = ByteArray.readU16bit(info, pos);
          throwsTarget(pos, index);
          return pos + 2;
        }
        case 0x40:
        case 0x41: {
          int len = ByteArray.readU16bit(info, pos);
          return localvarTarget(pos + 2, type, len);
        }
        case 0x42: {
          int index = ByteArray.readU16bit(info, pos);
          catchTarget(pos, index);
          return pos + 2;
        }
        case 0x43:
        case 0x44:
        case 0x45:
        case 0x46: {
          int offset = ByteArray.readU16bit(info, pos);
          offsetTarget(pos, type, offset);
          return pos + 2;
        }
        case 0x47:
        case 0x48:
        case 0x49:
        case 0x4a:
        case 0x4b: {
          int offset = ByteArray.readU16bit(info, pos);
          int index = info[pos + 2] & 0xff;
          typeArgumentTarget(pos, type, offset, index);
          return pos + 3;
        }
        default:
          throw new RuntimeException("invalid target type: " + type);
      }
    }

    void typeParameterTarget(int pos, int targetType, int typeParameterIndex) throws Exception {}

    void supertypeTarget(int pos, int superTypeIndex) throws Exception {}

    void typeParameterBoundTarget(int pos, int targetType, int typeParameterIndex, int boundIndex) throws Exception {}

    void emptyTarget(int pos, int targetType) throws Exception {}

    void formalParameterTarget(int pos, int formalParameterIndex) throws Exception {}

    void throwsTarget(int pos, int throwsTypeIndex) throws Exception {}

    int localvarTarget(int pos, int targetType, int tableLength) throws Exception {
      for (int i = 0; i < tableLength; i++) {
        int start = ByteArray.readU16bit(info, pos);
        int length = ByteArray.readU16bit(info, pos + 2);
        int index = ByteArray.readU16bit(info, pos + 4);
        localvarTarget(pos, targetType, start, length, index);
        pos += 6;
      }

      return pos;
    }

    void localvarTarget(int pos, int targetType, int startPc, int length, int index) throws Exception {}

    void catchTarget(int pos, int exceptionTableIndex) throws Exception {}

    void offsetTarget(int pos, int targetType, int offset) throws Exception {}

    void typeArgumentTarget(int pos, int targetType, int offset, int typeArgumentIndex) throws Exception {}

    final int typePath(int pos) throws Exception {
      int len = info[pos++] & 0xff;
      return typePath(pos, len);
    }

    int typePath(int pos, int pathLength) throws Exception {
      for (int i = 0; i < pathLength; i++) {
        int kind = info[pos] & 0xff;
        int index = info[pos + 1] & 0xff;
        typePath(pos, kind, index);
        pos += 2;
      }

      return pos;
    }

    void typePath(int pos, int typePathKind, int typeArgumentIndex) throws Exception {}
  }

}
