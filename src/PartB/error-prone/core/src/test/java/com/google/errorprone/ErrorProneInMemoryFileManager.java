/*
 * Copyright 2014 Google Inc. All Rights Reserved.
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

import static java.nio.charset.StandardCharsets.UTF_8;

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.common.jimfs.Configuration;
import com.google.common.jimfs.Jimfs;

import com.sun.tools.javac.file.JavacFileManager;
import com.sun.tools.javac.util.Context;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;

import javax.tools.JavaFileObject;

/**
 * An in-memory file manager for testing that uses {@link JavacFileManager} and {@link Jimfs}.
 */
public class ErrorProneInMemoryFileManager extends JavacFileManager {
  private final FileSystem fileSystem;
  private final Optional<Class<?>> clazz;

  /**
   * Constructs an ErrorProneInMemoryFileManager instance.
   *
   * <p>Instances constructed with this constructor may not use the {@link #forResource(String)}
   * method to create a JavaFileObject from a source file on disk.  If you wish to use that method,
   * use the {@link #ErrorProneInMemoryFileManager(Class)} constructor instead.
   */
  public ErrorProneInMemoryFileManager() {
    this(Optional.<Class<?>>absent());
  }

  /**
   * Constructs an ErrorProneInMemoryFileManager instance, given a class that can be used to lookup
   * file resources, such as test inputs to compile.
   *
   * @param clazz the class to use to locate file resources
   */
  public ErrorProneInMemoryFileManager(Class<?> clazz) {
    this(Optional.<Class<?>>of(clazz));
  }

  private ErrorProneInMemoryFileManager(Optional<Class<?>> clazz) {
    super(new Context(), false, UTF_8);
    this.fileSystem = Jimfs.newFileSystem(Configuration.unix());
    this.clazz = clazz;
  }

  /**
   * Loads resources of the provided class into {@link JavaFileObject}s.
   */
  public List<JavaFileObject> forResources(Class<?> clazz, String... fileNames) {
    ImmutableList.Builder<JavaFileObject> result = ImmutableList.builder();
    for (String fileName : fileNames) {
      result.add(forResource(clazz, fileName));
    }
    return result.build();
  }

  /**
   * Loads a resource of the provided class into a {@link JavaFileObject}.
   */
  public JavaFileObject forResource(Class<?> clazz, String fileName) {
    Path path = fileSystem.getPath("/", clazz.getPackage().getName().replace('.', '/'), fileName);
    try {
      Files.createDirectories(path.getParent());
      InputStream inputStream = null;
      try {
        inputStream = clazz.getResourceAsStream(fileName);
        if (inputStream == null) {
          inputStream = clazz.getResourceAsStream("testdata/" + fileName);
        }
        Files.copy(inputStream, path);
      } finally {
        inputStream.close();
      }
    } catch (IOException e) {
      throw new AssertionError(e);
    }
    return Iterables.getOnlyElement(getJavaFileObjects(path));
  }

  /**
   * Loads a resource of the class passed into the constructor into a {@link JavaFileObject}.
   */
  public JavaFileObject forResource(String fileName) {
    Preconditions.checkState(
        clazz.isPresent(), "clazz must be set if you want to add a source from a resource file");
    return forResource(clazz.get(), fileName);
  }

  /**
   * Creates a {@link JavaFileObject} with the given name and content.
   */
  public JavaFileObject forSourceLines(String fileName, String... lines) {
    if (!fileName.startsWith("/")) {
      fileName = "/" + fileName;
    }
    Path path = fileSystem.getPath(fileName);
    try {
      Files.createDirectories(path.getParent());
      Files.write(path, Arrays.asList(lines), UTF_8);
    } catch (IOException e) {
      throw new AssertionError(e);
    }
    return Iterables.getOnlyElement(getJavaFileObjects(path));
  }
}
