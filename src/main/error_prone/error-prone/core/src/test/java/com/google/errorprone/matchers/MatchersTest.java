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

package com.google.errorprone.matchers;

import static com.google.errorprone.BugPattern.Category.ONE_OFF;
import static com.google.errorprone.BugPattern.MaturityLevel.MATURE;
import static com.google.errorprone.BugPattern.SeverityLevel.ERROR;
import static com.google.errorprone.matchers.Matchers.inLoop;
import static com.google.errorprone.matchers.Matchers.isSubtypeOf;
import static com.google.errorprone.matchers.Matchers.methodReturns;
import static com.google.errorprone.suppliers.Suppliers.typeFromString;

import com.google.errorprone.BugPattern;
import com.google.errorprone.CompilationTestHelper;
import com.google.errorprone.MatcherChecker;
import com.google.errorprone.VisitorState;
import com.google.errorprone.bugpatterns.BugChecker;
import com.google.errorprone.bugpatterns.BugChecker.MethodTreeMatcher;
import com.google.errorprone.scanner.ErrorProneScanner;
import com.google.errorprone.scanner.ScannerSupplier;

import com.sun.source.tree.MethodTree;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/**
 * Unit tests for {@link Matchers}.
 */
@RunWith(JUnit4.class)
public class MatchersTest {

  private CompilationTestHelper compilationHelper;

  @Before
  public void setUp() {
    compilationHelper = CompilationTestHelper.newInstance(InLoopChecker.class, getClass());
  }

  @Test
  public void inLoopShouldMatchInWhileLoop() {
    compilationHelper
        .addSourceLines("Test.java",
            "public class Test {",
            "  public void doIt() {",
            "    while (true) {",
            "      // BUG: Diagnostic contains:",
            "      System.out.println();",
            "    }",
            "  }",
            "}")
        .doTest();
  }

  @Test
  public void inLoopShouldMatchInDoLoop() {
    compilationHelper
        .addSourceLines("Test.java",
            "public class Test {",
            "  public void doIt() {",
            "    do {",
            "      // BUG: Diagnostic contains:",
            "      System.out.println();",
            "    } while (true);",
            "  }",
            "}")
        .doTest();
  }

  @Test
  public void inLoopShouldMatchInForLoop() {
    compilationHelper
        .addSourceLines("Test.java",
            "public class Test {",
            "  public void doIt() {",
            "    for (; true; ) {",
            "      // BUG: Diagnostic contains:",
            "      System.out.println();",
            "    }",
            "  }",
            "}")
        .doTest();
  }

  @Test
  public void inLoopShouldMatchInEnhancedForLoop() {
    compilationHelper
        .addSourceLines("Test.java",
            "import java.util.List;",
            "public class Test {",
            "  public void doIt(List<String> strings) {",
            "    for (String s : strings) {",
            "      // BUG: Diagnostic contains:",
            "      System.out.println();",
            "    }",
            "  }",
            "}")
        .doTest();
  }

  @Test
  public void inLoopShouldNotMatchInInitializerWithoutLoop() {
    compilationHelper
        .addSourceLines("Test.java",
            "import java.util.List;",
            "public class Test {",
            "  {",
            "    System.out.println();",
            "  }",
            "}")
        .doTest();
  }

  @Test
  public void inLoopShouldMatchInInitializerInLoop() {
    compilationHelper
        .addSourceLines("Test.java",
            "import java.util.List;",
            "public class Test {",
            "  {",
            "    int count = 0;",
            "    while (count < 10) {",
            "      // BUG: Diagnostic contains:",
            "      System.out.println();",
            "    }",
            "  }",
            "}")
        .doTest();
  }

  @Test
  public void inLoopShouldNotMatchInAnonymousInnerClassDefinedInLoop() {
    compilationHelper
        .addSourceLines("Test.java",
            "import java.util.*;",
            "public class Test {",
            "  public void sort(List<List<String>> stringLists) {",
            "    for (List<String> stringList : stringLists) {",
            "      Collections.sort(stringList, new Comparator<String>() {",
            "          {",
            "            System.out.println();",
            "          }",
            "          public int compare(String s1, String s2) {",
            "            return 0;",
            "          }",
            "          public boolean equals(Object obj) {",
            "            return false;",
            "          }",
            "      });",
            "    }",
            "  }",
            "}")
        .doTest();
  }

  @Test
  public void methodWithClassAndName() {
    Matcher<MethodTree> matcher =
        Matchers.methodWithClassAndName("com.google.errorprone.foo.bar.Test", "myMethod");
    CompilationTestHelper.newInstance(methodTreeCheckerSupplier(matcher), getClass())
        .addSourceLines(
            "com/google/errorprone/foo/bar/Test.java",
            "package com.google.errorprone.foo.bar;",
            "public class Test {",
            "  // BUG: Diagnostic contains:",
            "  public void myMethod() {}",
            "}")
        .doTest();
  }

  @Test
  public void methodReturnsSubtype() {
    Matcher<MethodTree> matcher = methodReturns(isSubtypeOf("java.util.List"));
    CompilationTestHelper.newInstance(methodTreeCheckerSupplier(matcher), getClass())
        .addSourceLines(
            "test/MethodReturnsSubtypeTest.java",
            "package test;",
            "public class MethodReturnsSubtypeTest {",
            "  // BUG: Diagnostic contains:",
            "  public java.util.ArrayList<String> matches() {",
            "    return null;",
            "  }",
            "  public java.util.HashSet<String> doesntMatch() {",
            "    return null;",
            "  }",
            "}")
        .doTest();
  }

  @Test
  public void methodReturnsType() {
    Matcher<MethodTree> matcher = methodReturns(typeFromString("java.lang.Number"));
    CompilationTestHelper.newInstance(methodTreeCheckerSupplier(matcher), getClass())
        .addSourceLines(
            "test/MethodReturnsSubtypeTest.java",
            "package test;",
            "public class MethodReturnsSubtypeTest {",
            "  public java.lang.Integer doesntMatch() {",
            "    return 0;",
            "  }",
            "  // BUG: Diagnostic contains:",
            "  public java.lang.Number matches() {",
            "    return 0;",
            "  }",
            "}")
        .doTest();
  }

  @Test
  public void methodReturnsNonPrimitiveType() {
    Matcher<MethodTree> matcher = Matchers.methodReturnsNonPrimitiveType();
    CompilationTestHelper.newInstance(methodTreeCheckerSupplier(matcher), getClass())
        .addSourceLines(
            "test/MethodReturnsSubtypeTest.java",
            "package test;",
            "public class MethodReturnsSubtypeTest {",
            "  public int doesntMatch() {",
            "    return 0;",
            "  }",
            "  // BUG: Diagnostic contains:",
            "  public String matches() {",
            "    return \"\";",
            "  }",
            "}")
        .doTest();
  }

  @BugPattern(
      name = "InLoopChecker",
      summary = "Checker that flags the given expression statement if the given matcher matches",
      category = ONE_OFF, maturity = MATURE, severity = ERROR)
  public static class InLoopChecker extends MatcherChecker {
    public InLoopChecker() {
      super("System.out.println();", inLoop());
    }
  }

  private static ScannerSupplier methodTreeCheckerSupplier(Matcher<MethodTree> matcher) {
    return ScannerSupplier.fromScanner(new ErrorProneScanner(new MethodTreeChecker(matcher)));
  }

  @BugPattern(
    name = "MethodTreeChecker",
    summary = "Checker that flags the given method declaration if the given matcher matches",
    category = ONE_OFF,
    maturity = MATURE,
    severity = ERROR
  )
  static class MethodTreeChecker extends BugChecker
      implements MethodTreeMatcher {
    private final Matcher<MethodTree> matcher;

    public MethodTreeChecker(Matcher<MethodTree> matcher) {
      this.matcher = matcher;
    }

    @Override
    public Description matchMethod(MethodTree tree, VisitorState state) {
      return matcher.matches(tree, state) ? describeMatch(tree) : Description.NO_MATCH;
    }
  }
}
