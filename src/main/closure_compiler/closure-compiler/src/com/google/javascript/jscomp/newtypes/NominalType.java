/*
 * Copyright 2013 The Closure Compiler Authors.
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

package com.google.javascript.jscomp.newtypes;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.common.collect.Multimap;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Pattern;

/**
 * @author blickly@google.com (Ben Lickly)
 * @author dimvar@google.com (Dimitris Vardoulakis)
 */
public final class NominalType {
  // In the case of a generic type (rawType.typeParameters non-empty) either:
  // a) typeMap is empty, this is an uninstantiated generic type (Foo<T>), or
  // b) typeMap's keys exactly correspond to the type parameters of rawType;
  //    this represents a completely instantiated generic type (Foo<number>).
  private final ImmutableMap<String, JSType> typeMap;
  private final RawNominalType rawType;
  private static final Pattern NUMERIC_PATTERN = Pattern.compile("\\d+");

  NominalType(ImmutableMap<String, JSType> typeMap, RawNominalType rawType) {
    Preconditions.checkState(typeMap.isEmpty()
        || typeMap.keySet().containsAll(rawType.getTypeParameters())
        && rawType.getTypeParameters().containsAll(typeMap.keySet()));
    this.typeMap = typeMap;
    this.rawType = rawType;
  }

  // This should only be called during GlobalTypeInfo.
  public RawNominalType getRawNominalType() {
    Preconditions.checkState(!this.rawType.isFinalized());
    return this.rawType;
  }

  public JSType getInstanceAsJSType() {
    return (rawType.isGeneric() && !typeMap.isEmpty())
        ? JSType.fromObjectType(ObjectType.fromNominalType(this))
        : rawType.getInstanceAsJSType();
  }

  ObjectKind getObjectKind() {
    return this.rawType.getObjectKind();
  }

  Map<String, JSType> getTypeMap() {
    return typeMap;
  }

  boolean isClassy() {
    return !isFunction() && !isBuiltinObject();
  }

  boolean isFunction() {
    return "Function".equals(rawType.name);
  }

  public boolean isBuiltinObject() {
    return "Object".equals(rawType.name);
  }

  public boolean isStruct() {
    return rawType.isStruct();
  }

  public boolean isDict() {
    return rawType.isDict();
  }

  public boolean isGeneric() {
    return rawType.isGeneric();
  }

  public boolean isUninstantiatedGenericType() {
    return rawType.isGeneric() && typeMap.isEmpty();
  }

  NominalType instantiateGenerics(List<JSType> types) {
    ImmutableList<String> typeParams = this.rawType.getTypeParameters();
    Preconditions.checkState(types.size() == typeParams.size());
    Map<String, JSType> typeMap = new LinkedHashMap<>();
    for (int i = 0; i < typeParams.size(); i++) {
      typeMap.put(typeParams.get(i), types.get(i));
    }
    return instantiateGenerics(typeMap);
  }

  NominalType instantiateGenerics(Map<String, JSType> newTypeMap) {
    if (newTypeMap.isEmpty()) {
      return this;
    }
    if (!this.rawType.isGeneric()) {
      return this.rawType.getAsNominalType();
    }
    ImmutableMap.Builder<String, JSType> builder = ImmutableMap.builder();
    ImmutableMap<String, JSType> resultMap;
    if (!typeMap.isEmpty()) {
      for (String oldKey : typeMap.keySet()) {
        builder.put(oldKey, typeMap.get(oldKey).substituteGenerics(newTypeMap));
      }
      resultMap = builder.build();
    } else {
      ImmutableList<String> typeParams = this.rawType.getTypeParameters();
      for (String newKey : typeParams) {
        if (newTypeMap.containsKey(newKey)) {
          builder.put(newKey, newTypeMap.get(newKey));
        }
      }
      resultMap = builder.build();
      if (resultMap.isEmpty()) {
        return this;
      }
      // This works around a bug in FunctionType, because we can't know where
      // FunctionType#receiverType is coming from.
      // If the condition is true, receiverType comes from a method declaration,
      // and we should not create a new type here.
      if (resultMap.size() < typeParams.size()) {
        return this;
      }
    }
    return new NominalType(resultMap, this.rawType);
  }

  public String getName() {
    return rawType.name;
  }

  // Only used for keys in GlobalTypeInfo
  public RawNominalType getId() {
    return rawType;
  }

  public boolean isClass() {
    return rawType.isClass();
  }

  public boolean isInterface() {
    return rawType.isInterface();
  }

  public boolean isFinalized() {
    return this.rawType.isFinalized();
  }

  boolean hasAncestorClass(RawNominalType ancestor) {
    return this.rawType.hasAncestorClass(ancestor);
  }

  boolean hasAncestorInterface(RawNominalType ancestor) {
    return this.rawType.hasAncestorInterface(ancestor);
  }

  public ImmutableSet<String> getAllPropsOfInterface() {
    return this.rawType.getAllPropsOfInterface();
  }

  public ImmutableSet<String> getAllPropsOfClass() {
    return this.rawType.getAllPropsOfClass();
  }

  public NominalType getInstantiatedSuperclass() {
    Preconditions.checkState(rawType.isFinalized());
    if (this.rawType.getSuperClass() == null) {
      return null;
    }
    return this.rawType.getSuperClass().instantiateGenerics(typeMap);
  }

  public JSType getPrototype() {
    Preconditions.checkState(rawType.isFinalized());
    return rawType.getCtorPropDeclaredType("prototype")
        .substituteGenerics(typeMap);
  }

  public ImmutableSet<NominalType> getInstantiatedInterfaces() {
    Preconditions.checkState(rawType.isFinalized());
    ImmutableSet.Builder<NominalType> result = ImmutableSet.builder();
    for (NominalType interf : this.rawType.getInterfaces()) {
      result.add(interf.instantiateGenerics(typeMap));
    }
    return result.build();
  }

  Property getProp(String pname) {
    if (this.rawType.name.equals("Array")
        && NUMERIC_PATTERN.matcher(pname).matches()) {
      if (typeMap.isEmpty()) {
        return Property.make(JSType.UNKNOWN, null);
      }
      Preconditions.checkState(typeMap.size() == 1);
      JSType elmType = Iterables.getOnlyElement(typeMap.values());
      return Property.make(elmType, null);
    }
    Property p = this.rawType.getProp(pname);
    return p == null ? null : p.substituteGenerics(typeMap);
  }

  public JSType getPropDeclaredType(String pname) {
    JSType type = rawType.getInstancePropDeclaredType(pname);
    if (type == null) {
      return null;
    }
    return type.substituteGenerics(typeMap);
  }

  public boolean hasConstantProp(String pname) {
    Property p = this.rawType.getProp(pname);
    return p != null && p.isConstant();
  }

  JSType getConstructorObject(FunctionType ctorFn) {
    return this.rawType.getConstructorObject(ctorFn);
  }

  boolean isSubtypeOf(NominalType other) {
    RawNominalType thisRaw = this.rawType;
    if (thisRaw == other.rawType) {
      return areTypeMapsCompatible(other);
    }
    if (other.isInterface()) {
      // If thisRaw is not finalized, thisRaw.interfaces may be null.
      for (NominalType i : thisRaw.getInterfaces()) {
        if (i.instantiateGenerics(this.typeMap).isSubtypeOf(other)) {
          return true;
        }
      }
    }
    // Note that other can still be an interface here (implemented by a superclass)
    return isClass() && thisRaw.getSuperClass() != null
        && thisRaw.getSuperClass().instantiateGenerics(this.typeMap).isSubtypeOf(other);
  }

  private boolean areTypeMapsCompatible(NominalType other) {
    Preconditions.checkState(rawType.equals(other.rawType));
    if (this.typeMap.isEmpty()) {
      return other.instantiationIsUnknownOrIdentity();
    }
    if (other.typeMap.isEmpty()) {
      return instantiationIsUnknownOrIdentity();
    }
    for (String typeVar : rawType.getTypeParameters()) {
      Preconditions.checkState(this.typeMap.containsKey(typeVar),
          "Type variable %s not in the domain: %s",
          typeVar, this.typeMap.keySet());
      Preconditions.checkState(other.typeMap.containsKey(typeVar),
          "Other (%s) doesn't contain mapping (%s->%s) from this (%s)",
          other, typeVar, this.typeMap.get(typeVar), this);
      JSType thisType = this.typeMap.get(typeVar);
      JSType otherType = other.typeMap.get(typeVar);
      if (allowCovariantGenerics(this)) {
        if (!thisType.isSubtypeOf(otherType)) {
          return false;
        }
      } else if (!thisType.equals(otherType)
          && JSType.unifyUnknowns(thisType, otherType) == null) {
        return false;
      }
    }
    return true;
  }

  /**
   * Unify the two types symmetrically, given that we have already instantiated
   * the type variables of interest in {@code nt1} and {@code nt2}, treating
   * JSType.UNKNOWN as a "hole" to be filled.
   * @return The unified type, or null if unification fails
   */
  static NominalType unifyUnknowns(NominalType nt1, NominalType nt2) {
    if (!nt1.rawType.equals(nt2.rawType)) {
      return null;
    }
    Map<String, JSType> m1 = nt1.typeMap;
    Map<String, JSType> m2 = nt2.typeMap;
    if (m1.isEmpty() && m2.isEmpty()) {
      return nt1;
    } else if (m1.isEmpty() || m2.isEmpty()) {
      return null;
    }
    ImmutableMap.Builder<String, JSType> builder = ImmutableMap.builder();
    for (Map.Entry<String, JSType> entry : m1.entrySet()) {
      String typeVar = entry.getKey();
      JSType t1 = entry.getValue();
      JSType t2 = m2.get(typeVar);
      if (t1.isUnknown()) {
        builder.put(typeVar, t2);
      } else if (t2.isUnknown()) {
        builder.put(typeVar, t1);
      } else {
        JSType newType = JSType.unifyUnknowns(t1, t2);
        if (newType == null) {
          return null;
        }
        builder.put(typeVar, newType);
      }
    }
    return new NominalType(builder.build(), nt1.rawType);
  }

  private static boolean allowCovariantGenerics(NominalType nt) {
    // TODO(dimvar): Add Object here when we handle parameterized Object.
    return nt.rawType.name.equals("Array");
  }

  private boolean instantiationIsUnknownOrIdentity() {
    if (this.typeMap.isEmpty()) {
      return true;
    }
    for (String typeVar : this.rawType.getTypeParameters()) {
      Preconditions.checkState(this.typeMap.containsKey(typeVar),
          "Type variable %s not in the domain: %s",
          typeVar, this.typeMap.keySet());
      JSType t = this.typeMap.get(typeVar);
      if (!t.isUnknown() && !t.equals(JSType.fromTypeVar(typeVar))) {
        return false;
      }
    }
    return true;
  }

  // A special-case of join
  static NominalType pickSuperclass(NominalType c1, NominalType c2) {
    if (c1 == null || c2 == null) {
      return null;
    }
    if (c1.isSubtypeOf(c2)) {
      return c2;
    }
    return c2.isSubtypeOf(c1) ? c1 : null;
  }

  // A special-case of meet
  static NominalType pickSubclass(NominalType c1, NominalType c2) {
    if (c1 == null) {
      return c2;
    }
    if (c2 == null) {
      return c1;
    }
    if (c1.isSubtypeOf(c2)) {
      return c1;
    }
    return c2.isSubtypeOf(c1) ? c2 : null;
  }

  boolean unifyWithSubtype(NominalType other, List<String> typeParameters,
      Multimap<String, JSType> typeMultimap) {
    other = other.findMatchingAncestorWith(this);
    if (other == null) {
      return false;
    }
    if (!isGeneric()) {
      // Non-generic nominal types don't contribute to the unification.
      return true;
    }
    // Most of the time, both nominal types are already instantiated when
    // unifyWith is called. Rarely, when we call a polymorphic function from the
    // body of a method of a polymorphic class, then other.typeMap is empty.
    // For now, don't do anything fancy in that case.
    Preconditions.checkState(!typeMap.isEmpty());
    if (other.typeMap.isEmpty()) {
      return true;
    }
    boolean hasUnified = true;
    for (String typeParam : this.rawType.getTypeParameters()) {
      JSType fromOtherMap = other.typeMap.get(typeParam);
      Preconditions.checkNotNull(fromOtherMap,
          "Type variable %s not found in map %s", typeParam, other.typeMap);
      hasUnified = hasUnified && this.typeMap.get(typeParam)
          .unifyWithSubtype(fromOtherMap, typeParameters, typeMultimap);
    }
    return hasUnified && (allowCovariantGenerics(this)
        || isInvariantWith(typeMultimap, other));
  }

  // Returns a type with the same raw type as other, but possibly different type maps.
  private NominalType findMatchingAncestorWith(NominalType other) {
    RawNominalType thisRaw = this.rawType;
    if (thisRaw == other.rawType) {
      return this;
    }
    if (other.isInterface()) {
      for (NominalType i : thisRaw.getInterfaces()) {
        NominalType nt = i.instantiateGenerics(this.typeMap).findMatchingAncestorWith(other);
        if (nt != null) {
          return nt;
        }
      }
    }
    // Note that other can still be an interface here (implemented by a superclass)
    if (isClass() && thisRaw.getSuperClass() != null) {
      return thisRaw.getSuperClass().instantiateGenerics(this.typeMap)
        .findMatchingAncestorWith(other);
    }
    return null;
  }

  private boolean isInvariantWith(Multimap<String, JSType> typeMultimap, NominalType other) {
    Preconditions.checkState(isGeneric());
    Preconditions.checkState(this.rawType == other.rawType);
    Map<String, JSType> newTypeMap = new LinkedHashMap<>();
    for (String typeVar : typeMultimap.keySet()) {
      Collection<JSType> c = typeMultimap.get(typeVar);
      if (c.size() != 1) {
        return false;
      }
      newTypeMap.put(typeVar, Preconditions.checkNotNull(Iterables.getOnlyElement(c)));
    }
    NominalType instantiated = instantiateGenerics(newTypeMap);
    Map<String, JSType> otherMap = other.typeMap;
    // We can't just compare instantiated.typeMap and otherMap for equality,
    // because it doesn't take unknown types into account.
    for (Map.Entry<String, JSType> entry : instantiated.typeMap.entrySet()) {
      JSType t1 = entry.getValue();
      JSType t2 = otherMap.get(entry.getKey());
      if (!t1.isSubtypeOf(t2) || !t2.isSubtypeOf(t1)) {
        return false;
      }
    }
    return true;
  }

  @Override
  public String toString() {
    return appendTo(new StringBuilder()).toString();
  }

  StringBuilder appendTo(StringBuilder builder) {
    if (this.typeMap.isEmpty()) {
      return this.rawType.appendTo(builder);
    }
    builder.append(this.rawType.name);
    ImmutableList<String> typeParams = this.rawType.getTypeParameters();
    Preconditions.checkState(this.typeMap.keySet().containsAll(typeParams));
    boolean firstIteration = true;
    builder.append('<');
    for (String typeParam : typeParams) {
      if (firstIteration) {
        firstIteration = false;
      } else {
        builder.append(',');
      }
      JSType concrete = this.typeMap.get(typeParam);
      Preconditions.checkNotNull(concrete).appendTo(builder);
    }
    builder.append('>');
    return builder;
  }

  @Override
  public int hashCode() {
    return Objects.hash(typeMap, rawType);
  }

  @Override
  public boolean equals(Object other) {
    if (other == null) {
      return false;
    }
    Preconditions.checkState(other instanceof NominalType);
    NominalType o = (NominalType) other;
    return Objects.equals(typeMap, o.typeMap) && rawType.equals(o.rawType);
  }
}
