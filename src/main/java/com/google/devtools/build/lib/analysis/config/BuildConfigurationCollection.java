// Copyright 2014 The Bazel Authors. All rights reserved.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//    http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.google.devtools.build.lib.analysis.config;

import com.google.common.collect.ImmutableList;
import com.google.devtools.build.lib.concurrent.ThreadSafety.ThreadSafe;
import java.util.HashMap;
import java.util.List;

/**
 * Convenience container for top-level target and host configurations.
 *
 * <p>The target configuration is used for all targets specified on the command line. Multiple
 * target configurations are possible because of settings like {@link
 * com.google.devtools.build.lib.buildtool.BuildRequest.BuildRequestOptions#multiCpus}.
 *
 * <p>The host configuration is used for tools that are executed during the build, e. g, compilers.
 */
@ThreadSafe
public final class BuildConfigurationCollection {
  private final ImmutableList<BuildConfiguration> targetConfigurations;
  private final BuildConfiguration hostConfiguration;

  public BuildConfigurationCollection(List<BuildConfiguration> targetConfigurations,
      BuildConfiguration hostConfiguration)
      throws InvalidConfigurationException {
    this.targetConfigurations = ImmutableList.copyOf(targetConfigurations);
    this.hostConfiguration = hostConfiguration;

    // Except for the host configuration (which may be identical across target configs), the other
    // configurations must all have different cache keys or we will end up with problems.
    HashMap<String, BuildConfiguration> cacheKeyConflictDetector = new HashMap<>();
    for (BuildConfiguration config : targetConfigurations) {
      String cacheKey = config.checksum();
      if (cacheKeyConflictDetector.containsKey(cacheKey)) {
        throw new InvalidConfigurationException("Conflicting configurations: " + config + " & "
            + cacheKeyConflictDetector.get(cacheKey));
      }
      cacheKeyConflictDetector.put(cacheKey, config);
    }
  }

  public ImmutableList<BuildConfiguration> getTargetConfigurations() {
    return targetConfigurations;
  }

  /**
   * Returns the host configuration for this collection.
   *
   * <p>Don't use this method. It's limited in that it assumes a single host configuration for
   * the entire collection. This may not be true in the future and more flexible interfaces based
   * on dynamic configurations will likely supplant this interface anyway. Its main utility is
   * to keep Bazel working while dynamic configuration progress is under way.
   */
  public BuildConfiguration getHostConfiguration() {
    return hostConfiguration;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (!(obj instanceof BuildConfigurationCollection)) {
      return false;
    }
    BuildConfigurationCollection that = (BuildConfigurationCollection) obj;
    return this.targetConfigurations.equals(that.targetConfigurations);
  }

  @Override
  public int hashCode() {
    return targetConfigurations.hashCode();
  }
}
