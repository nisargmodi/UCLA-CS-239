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

package com.google.errorprone.bugpatterns;

import static com.google.errorprone.BugPattern.Category.JDK;
import static com.google.errorprone.BugPattern.MaturityLevel.EXPERIMENTAL;
import static com.google.errorprone.BugPattern.SeverityLevel.WARNING;

import com.google.errorprone.BugPattern;
import com.google.errorprone.VisitorState;
import com.google.errorprone.annotations.Var;
import com.google.errorprone.bugpatterns.BugChecker.VariableTreeMatcher;
import com.google.errorprone.fixes.Fix;
import com.google.errorprone.fixes.SuggestedFix;
import com.google.errorprone.matchers.Description;
import com.google.errorprone.util.ASTHelpers;

import com.sun.source.tree.VariableTree;
import com.sun.tools.javac.code.Flags;
import com.sun.tools.javac.code.Source;
import com.sun.tools.javac.code.Symbol;

import java.util.Collections;
import java.util.EnumSet;

import javax.lang.model.element.Modifier;

/** @author cushon@google.com (Liam Miller-Cushon) */
@BugPattern(
  name = "Var",
  summary = "Non-constant variable missing @Var annotation",
  category = JDK,
  severity = WARNING,
  maturity = EXPERIMENTAL
)
public class VarChecker extends BugChecker implements VariableTreeMatcher {

  private static final String UNNECESSARY_FINAL = "Unnecessary 'final' modifier.";

  @Override
  public Description matchVariable(VariableTree tree, VisitorState state) {
    Symbol sym = ASTHelpers.getSymbol(tree);
    if (sym == null) {
      return Description.NO_MATCH;
    }
    if (ASTHelpers.hasAnnotation(sym, Var.class)) {
      return Description.NO_MATCH;
    }
    switch (sym.getKind()) {
      case PARAMETER:
      case LOCAL_VARIABLE:
        return handleLocalOrParam(tree, state, sym);
      case FIELD:
        return handleField(tree, sym);
      default:
        return Description.NO_MATCH;
    }
  }

  private Description handleField(VariableTree tree, Symbol sym) {
    if ((sym.flags() & Flags.FINAL) == Flags.FINAL) {
      return Description.NO_MATCH;
    }
    return describeMatch(tree, addVarAnnotation(tree));
  }

  private Description handleLocalOrParam(VariableTree tree, VisitorState state, Symbol sym) {
    if (sym.getModifiers().contains(Modifier.FINAL)) {
      if (Source.instance(state.context).allowEffectivelyFinalInInnerClasses()) {
        // In Java 8, the final modifier is never necessary on locals/parameters because
        // effectively final variables can be used anywhere a final variable is required.
        return buildDescription(tree)
            .setMessage(UNNECESSARY_FINAL)
            .addFix(SuggestedFix.removeModifier(tree, Modifier.FINAL, state))
            .build();
      }
      return Description.NO_MATCH;
    }
    if (!Collections.disjoint(
        sym.owner.getModifiers(), EnumSet.of(Modifier.ABSTRACT, Modifier.NATIVE))) {
      // flow information isn't collected for body-less methods
      return Description.NO_MATCH;
    }
    if ((sym.flags() & (Flags.EFFECTIVELY_FINAL | Flags.FINAL)) != 0) {
      return Description.NO_MATCH;
    }
    return describeMatch(tree, addVarAnnotation(tree));
  }

  private static Fix addVarAnnotation(VariableTree tree) {
    return SuggestedFix.builder().prefixWith(tree, "@Var ").addImport(Var.class.getName()).build();
  }
}
