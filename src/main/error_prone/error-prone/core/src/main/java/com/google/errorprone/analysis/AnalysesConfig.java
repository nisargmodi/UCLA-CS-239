/*
 * Copyright 2015 Google Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package com.google.errorprone.analysis;

import com.google.auto.value.AutoValue;
import com.google.errorprone.ErrorProneOptions;
import com.google.errorprone.InvalidCommandLineOptionException;

import java.util.Set;

/**
 * Config for all kinds of analyses.
 * 
 * @author Louis Wasserman
 */
@AutoValue
public abstract class AnalysesConfig {
  public static AnalysesConfig create(ErrorProneOptions options) {
    return new AutoValue_AnalysesConfig(options);
  }
  
  AnalysesConfig() {}
  
  abstract ErrorProneOptions errorProneOptions();
  
  void validate(TopLevelAnalysis analysis) throws InvalidCommandLineOptionException {
    if (!errorProneOptions().ignoreUnknownChecks()) {
      Set<String> knownAnalyses = analysis.knownAnalysisNames();
      for (String configured : errorProneOptions().getSeverityMap().keySet()) {
        if (!knownAnalyses.contains(configured)) {
          throw new InvalidCommandLineOptionException(configured + " is not a valid checker name");
        }
      }
    }
  }
}
