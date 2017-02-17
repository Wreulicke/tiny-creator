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
package com.github.wreulicke.bean;

import static org.assertj.core.api.Assertions.assertThat;

import java.beans.ConstructorProperties;
import java.util.HashMap;

import org.junit.Test;

import com.github.wreulicke.bean.TinyCreator;

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
