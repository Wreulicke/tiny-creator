package com.github.wreulicke.bean.validation;


import static com.github.wreulicke.bean.validation.ByteCodes.getByteCode;
import static com.github.wreulicke.bean.validation.Instruments.forInstruments;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import javassist.ClassPool;
import mockit.Mock;
import mockit.MockUp;

public class NullInstrumentationTest {

  @BeforeClass
  public static void setup() {

  }

  @AfterClass
  public static void tearDown() {

  }

  @SuppressWarnings({
    "unchecked",
    "rawtypes"
  })
  @Test
  public void test() throws Exception {
    new MockUp(Class.forName("javassist.CtClassType")) {
      @Mock
      public boolean isFrozen() {
        return false;
      }

      @Mock
      void checkModify() {}

      @Mock
      public boolean isModified() {
        return true;
      }
    };
    NotNullInstrumentation inst = new NotNullInstrumentation(ClassPool.getDefault());
    byte[] result = inst.transform(getClass().getClassLoader(), forInstruments(Example.class), null, null, getByteCode(Example.class));

    // Path root = Paths.get(".")
    // .toRealPath()
    // .toAbsolutePath();
    // Path temp = Files.createTempDirectory(root, "temp");
    // Decompiler decompiler = new FernflowerDecompiler();
    // Path path = Files.createTempFile(temp, "Example", ".class");
    // ByteCodes.dumpByteCode(path, result);
    //
    // DecompilationResult decompilationResult = decompiler.decompileClassFile(root, path, temp);
    // Optional<String> first = decompilationResult.getDecompiledFiles()
    // .values()
    // .stream()
    // .findFirst();
    // assertThat(first).matches(Optional::isPresent);
    //
    // Files.walk(temp)
    // .map(Path::toFile)
    // .forEach(File::deleteOnExit);



  }
}
