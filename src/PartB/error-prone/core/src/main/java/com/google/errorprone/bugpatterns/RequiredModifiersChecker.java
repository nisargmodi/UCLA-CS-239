/*
 * Copyright 2013 Google Inc. All Rights Reserved.
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
import static com.google.errorprone.BugPattern.LinkType.NONE;
import static com.google.errorprone.BugPattern.MaturityLevel.MATURE;
import static com.google.errorprone.BugPattern.SeverityLevel.WARNING;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.common.collect.Sets;
import com.google.errorprone.BugPattern;
import com.google.errorprone.VisitorState;
import com.google.errorprone.annotations.RequiredModifiers;
import com.google.errorprone.bugpatterns.BugChecker.AnnotationTreeMatcher;
import com.google.errorprone.matchers.Description;
import com.google.errorprone.util.ASTHelpers;

import com.sun.source.tree.AnnotationTree;
import com.sun.source.tree.ModifiersTree;
import com.sun.source.tree.Tree;
import com.sun.tools.javac.code.Attribute;
import com.sun.tools.javac.util.Names;

import java.util.List;
import java.util.Set;

import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;

/**
 * @author sgoldfeder@google.com (Steven Goldfeder)
 */
@BugPattern(name = "RequiredModifiers",
    summary = "This annotation is missing required modifiers as specified by its "
    + "@RequiredModifiers annotation",
    explanation = "This annotation is itself annotated with @RequiredModifiers and "
    + "can only be used when the specified modifiers are present. You are attempting to"
    + "use it on an  element that is missing one or more required modifiers.", linkType = NONE,
    category = JDK, severity = WARNING, maturity = MATURE)
public class RequiredModifiersChecker extends BugChecker implements AnnotationTreeMatcher {

  private static final String MESSAGE_TEMPLATE = "%s has specified that it must be used"
      + " together with the following modifiers: %s";

  // TODO(cushon): deprecate and remove
  private static final String GUAVA_ANNOTATION =
      "com.google.common.annotations.RequiredModifiers";

  private static final Function<Attribute.Enum, Modifier> TO_MODIFIER =
      new Function<Attribute.Enum, Modifier>() {
        @Override
        public Modifier apply(Attribute.Enum input) {
          return Modifier.valueOf(input.getValue().name.toString());
        }
      };

  private static Set<Modifier> getRequiredModifiers(AnnotationTree tree, VisitorState state) {
    for (Attribute.Compound c : ASTHelpers.getSymbol(tree).getAnnotationMirrors()) {
      if (((TypeElement) c.getAnnotationType().asElement()).getQualifiedName()
          .contentEquals(GUAVA_ANNOTATION)) {
        @SuppressWarnings("unchecked")
        List<Attribute.Enum> modifiers =
            (List<Attribute.Enum>) c.member(Names.instance(state.context).value).getValue();
        return ImmutableSet.copyOf(Iterables.transform(modifiers, TO_MODIFIER));
      }
    }

    RequiredModifiers annotation = ASTHelpers.getAnnotation(tree, RequiredModifiers.class);
    if (annotation != null) {
      return ImmutableSet.copyOf(annotation.value());
    }

    return ImmutableSet.of();
  }

  @Override
  public Description matchAnnotation(AnnotationTree tree, VisitorState state) {
    Set<Modifier> requiredModifiers = getRequiredModifiers(tree, state);
    if (requiredModifiers.isEmpty()) {
      return Description.NO_MATCH;
    }

    Tree parent = state.getPath().getParentPath().getLeaf();
    if (!(parent instanceof ModifiersTree)) {
      // e.g. An annotated package name
      return Description.NO_MATCH;
    }

    Set<Modifier> missing = Sets.difference(
        requiredModifiers,
        ((ModifiersTree) parent).getFlags());

    if (missing.isEmpty()) {
      return Description.NO_MATCH;
    }

    String annotationName = ASTHelpers.getAnnotationName(tree);
    String nameString = annotationName != null
        ? String.format("The annotation '@%s'", annotationName)
        : "This annotation";
    String customMessage = String.format(MESSAGE_TEMPLATE, nameString, missing.toString());
    return buildDescription(tree)
        .setMessage(customMessage)
        .build();
  }
}
