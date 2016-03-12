/*
 * Copyright 2016 Google Inc. All Rights Reserved.
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

package com.google.errorprone.fixes;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

import com.google.common.base.Verify;
import com.google.common.collect.ImmutableMap;
import com.google.errorprone.BugPattern;
import com.google.errorprone.BugPattern.Category;
import com.google.errorprone.BugPattern.MaturityLevel;
import com.google.errorprone.BugPattern.SeverityLevel;
import com.google.errorprone.CompilationTestHelper;
import com.google.errorprone.VisitorState;
import com.google.errorprone.bugpatterns.BugChecker;
import com.google.errorprone.bugpatterns.BugChecker.ReturnTreeMatcher;
import com.google.errorprone.bugpatterns.BugChecker.VariableTreeMatcher;
import com.google.errorprone.matchers.Description;
import com.google.errorprone.util.ASTHelpers;

import com.sun.source.tree.ClassTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.ReturnTree;
import com.sun.source.tree.VariableTree;

import com.sun.tools.javac.code.Type;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.lang.annotation.Retention;

import javax.lang.model.element.Modifier;

/** @author cushon@google.com (Liam Miller-Cushon) */
@RunWith(JUnit4.class)
public class SuggestedFixTest {

  @Retention(RUNTIME)
  public @interface EditModifiers {
    String value() default "";

    EditKind kind() default EditKind.ADD;

    enum EditKind {
      ADD,
      REMOVE
    }
  }

  @BugPattern(
    name = "EditModifiers",
    category = Category.ONE_OFF,
    summary = "Edits modifiers",
    severity = SeverityLevel.ERROR,
    maturity = MaturityLevel.EXPERIMENTAL
  )
  public static class EditModifiersChecker extends BugChecker implements VariableTreeMatcher {

    static final ImmutableMap<String, Modifier> MODIFIERS_BY_NAME = createModifiersByName();

    private static ImmutableMap<String, Modifier> createModifiersByName() {
      ImmutableMap.Builder<String, Modifier> builder = ImmutableMap.builder();
      for (Modifier mod : Modifier.values()) {
        builder.put(mod.toString(), mod);
      }
      return builder.build();
    }

    @Override
    public Description matchVariable(VariableTree tree, VisitorState state) {
      EditModifiers editModifiers =
          ASTHelpers.getAnnotation(
              ASTHelpers.findEnclosingNode(state.getPath(), ClassTree.class), EditModifiers.class);
      Modifier mod = MODIFIERS_BY_NAME.get(editModifiers.value());
      Verify.verifyNotNull(mod, editModifiers.value());
      Fix fix;
      switch (editModifiers.kind()) {
        case ADD:
          fix = SuggestedFix.addModifier(tree, mod, state);
          break;
        case REMOVE:
          fix = SuggestedFix.removeModifier(tree, mod, state);
          break;
        default:
          throw new AssertionError(editModifiers.kind());
      }
      return describeMatch(tree, fix);
    }
  }

  @Test
  public void addModifiers() {
    CompilationTestHelper.newInstance(EditModifiersChecker.class, getClass())
        .addSourceLines(
            "Test.java",
            String.format("import %s;", EditModifiers.class.getCanonicalName()),
            "import javax.annotation.Nullable;",
            "@EditModifiers(value=\"final\", kind=EditModifiers.EditKind.ADD)",
            "class Test {",
            "  // BUG: Diagnostic contains: final Object one",
            "  Object one;",
            "  // BUG: Diagnostic contains: @Nullable final Object two",
            "  @Nullable Object two;",
            "  // BUG: Diagnostic contains: @Nullable public final Object three",
            "  @Nullable public Object three;",
            "  // BUG: Diagnostic contains: public final Object four",
            "  public Object four;",
            "}")
        .doTest();
  }

  @Test
  public void addModifiersFirst() {
    CompilationTestHelper.newInstance(EditModifiersChecker.class, getClass())
        .addSourceLines(
            "Test.java",
            String.format("import %s;", EditModifiers.class.getCanonicalName()),
            "import javax.annotation.Nullable;",
            "@EditModifiers(value=\"public\", kind=EditModifiers.EditKind.ADD)",
            "class Test {",
            "  // BUG: Diagnostic contains: public static final transient Object one",
            "  static final transient Object one = null;",
            "}")
        .doTest();
  }

  @Test
  public void removeModifiers() {
    CompilationTestHelper.newInstance(EditModifiersChecker.class, getClass())
        .addSourceLines(
            "Test.java",
            String.format("import %s;", EditModifiers.class.getCanonicalName()),
            "import javax.annotation.Nullable;",
            "@EditModifiers(value=\"final\", kind=EditModifiers.EditKind.REMOVE)",
            "class Test {",
            "  // BUG: Diagnostic contains: Object one",
            "  final Object one = null;",
            "  // BUG: Diagnostic contains: @Nullable Object two",
            "  @Nullable final Object two = null;",
            "  // BUG: Diagnostic contains: @Nullable public Object three",
            "  @Nullable public final Object three = null;",
            "  // BUG: Diagnostic contains: public Object four",
            "  public final Object four = null;",
            "}")
        .doTest();
  }

  @Retention(RUNTIME)
  public @interface TypeToCast {
    String value();
  }

  @BugPattern(
    category = Category.ONE_OFF,
    maturity = MaturityLevel.EXPERIMENTAL,
    name = "CastReturn",
    severity = SeverityLevel.ERROR,
    summary = "Adds casts to returned expressions"
  )
  public static class CastReturn extends BugChecker implements ReturnTreeMatcher {

    @Override
    public Description matchReturn(ReturnTree tree, VisitorState state) {
      if (tree.getExpression() == null) {
        return Description.NO_MATCH;
      }
      TypeToCast typeToCast =
          ASTHelpers.getAnnotation(
              ASTHelpers.findEnclosingNode(state.getPath(), MethodTree.class), TypeToCast.class);
      SuggestedFix.Builder fixBuilder = SuggestedFix.builder();
      Type type = state.getTypeFromString(typeToCast.value());
      Verify.verifyNotNull(type, "could not find type: %s", typeToCast.value());
      String qualifiedTargetType = SuggestedFix.qualifyType(state, fixBuilder, type.tsym);
      fixBuilder.prefixWith(tree.getExpression(), String.format("(%s) ", qualifiedTargetType));
      return describeMatch(tree, fixBuilder.build());
    }
  }

  @Test
  public void qualifiedName_Object() {
    CompilationTestHelper.newInstance(CastReturn.class, getClass())
        .addSourceLines(
            "Test.java",
            String.format("import %s;", TypeToCast.class.getCanonicalName()),
            "class Test {",
            "  @TypeToCast(\"java.lang.Object\")",
            "  Object f() {",
            "    // BUG: Diagnostic contains: return (Object) null;",
            "    return null;",
            "  }",
            "}")
        .doTest();
  }

  @Test
  public void qualifiedName_imported() {
    CompilationTestHelper.newInstance(CastReturn.class, getClass())
        .addSourceLines(
            "Test.java",
            "import java.util.Map.Entry;",
            String.format("import %s;", TypeToCast.class.getCanonicalName()),
            "class Test {",
            "  @TypeToCast(\"java.util.Map.Entry\")",
            "  Object f() {",
            "    // BUG: Diagnostic contains: return (Entry) null;",
            "    return null;",
            "  }",
            "}")
        .doTest();
  }

  @Test
  public void qualifiedName_notImported() {
    CompilationTestHelper.newInstance(CastReturn.class, getClass())
        .addSourceLines(
            "Test.java",
            String.format("import %s;", TypeToCast.class.getCanonicalName()),
            "class Test {",
            "  @TypeToCast(\"java.util.Map.Entry\")",
            "  Object f() {",
            "    // BUG: Diagnostic contains: return (Map.Entry) null;",
            "    return null;",
            "  }",
            "}")
        .doTest();
  }
}
