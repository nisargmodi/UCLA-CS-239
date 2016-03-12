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

import static com.google.common.io.Files.readLines;
import static java.nio.charset.StandardCharsets.UTF_8;

import com.beust.jcommander.IStringConverter;
import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;

import java.io.IOException;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Utility main which consumes the same tab-delimited text file and generates GitHub pages for
 * the BugPatterns.
 */
public class DocGenTool {

  @Parameters(separators = "=")
  static class Options {
    @Parameter(names = "-bug_patterns", description = "Path to bugPatterns.txt", required = true)
    private String bugPatterns;

    @Parameter(
      names = "-explanations",
      description = "Path to side-car explanations",
      required = true
    )
    private String explanations;

    @Parameter(names = "-docs_repository", description = "Path to docs repository", required = true)
    private String docsRepository;

    @Parameter(names = "-examplesDir", description = "Path to examples directory", required = true)
    private String examplesDir;

    @Parameter(
      names = "-target",
      description = "Whether to target the internal or external site",
      converter = TargetEnumConverter.class,
      required = true
    )
    private Target target;

    @Parameter(
      names = "-use_pygments_highlighting",
      description = "Use pygments for highlighting",
      arity = 1
    )
    private boolean usePygments = true;
  }

  enum Target {
    INTERNAL,
    EXTERNAL
  }

  public static class TargetEnumConverter implements IStringConverter<Target> {
    @Override
    public Target convert(String arg) {
      return Target.valueOf(arg.toUpperCase());
    }
  }

  public static void main(String[] args) throws IOException {
    Options options = new Options();
    new JCommander(options, args);

    Path bugPatterns = Paths.get(options.bugPatterns);
    if (!Files.exists(bugPatterns)) {
      usage("Cannot find bugPatterns file: " + options.bugPatterns);
    }
    Path explanationDir = Paths.get(options.explanations);
    if (!Files.exists(explanationDir)) {
      usage("Cannot find explanations dir: " + options.explanations);
    }
    Path wikiDir = Paths.get(options.docsRepository);
    Files.createDirectories(wikiDir);
    Path exampleDirBase = Paths.get(options.examplesDir);
    if (!Files.exists(exampleDirBase)) {
      usage("Cannot find example directory: " + options.examplesDir);
    }
    Path bugpatternDir = wikiDir.resolve("bugpattern");
    if (!Files.exists(bugpatternDir)) {
      Files.createDirectories(bugpatternDir);
    }
    Files.createDirectories(wikiDir.resolve("_data"));
    BugPatternFileGenerator generator =
        new BugPatternFileGenerator(
            bugpatternDir,
            exampleDirBase,
            explanationDir,
            options.target == Target.EXTERNAL,
            options.usePygments);
    try (Writer w =
            Files.newBufferedWriter(wikiDir.resolve("bugpatterns.md"), StandardCharsets.UTF_8)) {
      new BugPatternIndexWriter()
          .dump(readLines(bugPatterns.toFile(), UTF_8, generator), w, options.target);
    }
  }

  private static void usage(String err) {
    System.err.println(err);
    System.exit(1);
  }
}
