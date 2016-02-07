/*
 * Copyright 2013 Google Inc. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.errorprone.refaster;

import com.google.common.testing.EqualsTester;
import com.google.common.testing.SerializableTester;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/**
 * Tests for {@link UMethodType}.
 *
 * @author lowasser@google.com (Louis Wasserman)
 */
@RunWith(JUnit4.class)
public class UMethodTypeTest {
  @Test
  public void equality() {
    UType stringTy = UClassType.create("java.lang.String");
    new EqualsTester()
        .addEqualityGroup(UMethodType.create(stringTy, UPrimitiveType.INT))
        .addEqualityGroup(UMethodType.create(stringTy, UPrimitiveType.INT, UPrimitiveType.INT))
        .addEqualityGroup(UMethodType.create(UPrimitiveType.INT, UPrimitiveType.INT))
        .addEqualityGroup(UMethodType.create(stringTy, stringTy))
        .addEqualityGroup(UMethodType.create(stringTy))
        .testEquals();
  }
  
  @Test
  public void serialization() {
    SerializableTester.reserializeAndAssert(
        UMethodType.create(UClassType.create("java.lang.String"), UPrimitiveType.INT));
  }
}
