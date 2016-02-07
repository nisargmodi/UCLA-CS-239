/*
 * Copyright 2015 Google Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.errorprone;

import static com.google.errorprone.BugPattern.Category.JDK;
import static com.google.errorprone.BugPattern.MaturityLevel.EXPERIMENTAL;
import static com.google.errorprone.BugPattern.SeverityLevel.NOT_A_PROBLEM;

import com.google.errorprone.BugCheckerRefactoringTestHelper.TestMode;
import com.google.errorprone.bugpatterns.BugChecker;
import com.google.errorprone.bugpatterns.BugChecker.ReturnTreeMatcher;
import com.google.errorprone.fixes.SuggestedFix;
import com.google.errorprone.matchers.Description;

import com.sun.source.tree.ReturnTree;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.io.IOException;

/**
 * Tests for {@link BugCheckerRefactoringTestHelper}.
 */
@RunWith(JUnit4.class)
public class BugCheckerRefactoringTestHelperTest {

  private BugCheckerRefactoringTestHelper helper;

  @Before
  public void setUp() {
    helper = BugCheckerRefactoringTestHelper.newInstance(new ReturnNullRefactoring(), getClass());
  }

  @Test
  public void testNoMatch() throws IOException {
    helper
        .addInputLines("in/Test.java", "public class Test {}")
        .addOutputLines("out/Test.java", "public class Test {}")
        .doTest();
  }

  @Test
  public void testReplace() throws IOException {
    helper
        .addInputLines(
            "in/Test.java",
            "public class Test {",
            "  public Object foo() {",
            "    Integer i = 1 + 2;",
            "    return i;",
            "  }",
            "}")
        .addOutputLines(
            "out/Test.java",
            "public class Test {",
            "  public Object foo() {",
            "    Integer i = 1 + 2;",
            "  return null;",
            "  }",
            "}")
        .doTest();
  }

  @Test(expected = AssertionError.class)
  public void testReplaceFail() throws IOException {
    helper
        .addInputLines(
            "in/Test.java",
            "public class Test {",
            "  public Object foo() {",
            "    Integer i = 1 + 2;",
            "    return i;",
            "  }",
            "}")
        .addOutputLines(
            "out/Test.java",
            "public class Test {",
            "  public Object foo() {",
            "    Integer i = 1 + 2;",
            "    return i;",
            "  }",
            "}")
        .doTest();
  }



  @Test
  public void testReplaceTextMatch() throws IOException {
    helper
        .addInputLines(
            "in/Test.java",
            "public class Test {",
            "  public Object foo() {",
            "    Integer i = 1 + 2;",
            "    return i;",
            "  }",
            "}")
        .addOutputLines(
            "out/Test.java",
            "public class Test {",
            "  public Object foo() {",
            "    Integer i = 1 + 2;",
            "    return null;",
            "  }",
            "}")
        .doTest(TestMode.TEXT_MATCH);
  }

  @Test(expected = AssertionError.class)
  public void testReplaceTextMatchFail() throws IOException {
    helper
        .addInputLines(
            "in/Test.java",
            "public class Test {",
            "  public Object foo() {",
            "    Integer i = 1 + 2;",
            "    return i;",
            "  }",
            "}")
        .addOutputLines("out/Test.java",
            "public class Test {",
            "  public Object foo() {",
            "    Integer i = 1 + 2;",
            "  return null;",
            "  }",
            "}")
        .doTest(TestMode.TEXT_MATCH);
  }


  /**
   * Mock {@link BugChecker} for testing only.
   */
  @BugPattern(
      name = "ReturnNullRefactoring",
      summary = "Mock refactoring that replaces all returns with 'return null;' statement.",
      explanation = "For test purposes only.",
      category = JDK,
      severity = NOT_A_PROBLEM,
      maturity = EXPERIMENTAL)
  public static class ReturnNullRefactoring extends BugChecker implements ReturnTreeMatcher {
    @Override
    public Description matchReturn(ReturnTree tree, VisitorState state) {
      return describeMatch(tree, SuggestedFix.replace(tree, "return null;"));
    }
  }
}
