package com.github.wreulicke;

import static org.assertj.core.api.Assertions.assertThat;

import java.beans.ConstructorProperties;
import java.util.HashMap;

import org.junit.Test;

import lombok.Value;

public class TinyCreatorTest {
  @Test
  public void testCreate() {
    TinyCreator creator = new TinyCreator();
    ValueObject object = creator.create(ValueObject.class, new HashMap<String, Object>() {
      {
        put("number", 2);
        put("string", "str");
      }
    });
    assertThat(object.getNumber()).isEqualTo(2);
    assertThat(object.getString()).isEqualTo("str");
  }
  
  @Test
  public void testCreate2() {
    TinyCreator creator = new TinyCreator();
    ValueObject2 object = creator.create(ValueObject2.class, new HashMap<String, Object>() {
      {
        put("number", 2);
        put("string", "str");
      }
    });
    assertThat(object.number).isEqualTo(2);
    assertThat(object.string).isEqualTo("str");
  }
  
  @Test
  public void testCreate3() {
    TinyCreator creator = new TinyCreator();
    ValueObject2 object = creator.create(ValueObject2.class, new HashMap<String, Object>() {
      {
        put("number", 2);
        put("string", "str");
      }
    });
    assertThat(object.number).isEqualTo(2);
    assertThat(object.string).isEqualTo("str");
  }

  @Value
  public static class ValueObject {
    public Integer number;
    public String string;
  }

  public static class ValueObject2 {
    public Integer number;
    public String string;

    @ConstructorProperties({
      "number",
      "string"
    })
    public ValueObject2(Integer number, String string) {
      this.number = number;
      this.string = string;
    }
  }

}
