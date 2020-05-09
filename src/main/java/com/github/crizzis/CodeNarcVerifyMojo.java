package com.github.crizzis;

/*
 * Copyright 2001-2005 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import lombok.Getter;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

import static org.apache.maven.plugins.annotations.LifecyclePhase.VERIFY;
import static org.apache.maven.plugins.annotations.ResolutionScope.TEST;

/**
 * Perform CodeNarc analysis, and optionally fail the build if specified quality criteria are not met
 */
@Getter
@Mojo(name = "verify", defaultPhase = VERIFY, requiresDependencyResolution = TEST)
public class CodeNarcVerifyMojo extends AbstractCodeNarcMojo {

    /**
     * Whether to ignore any pre-existing SpotBugs XML report.
     * Setting this property to {@code true} is intended for a scenario in which generating the XML report
     * and failing the build (if violations have been found) needs to be split into separate executions of this goal
     */
    @Parameter(property = "codenarc.ignoreExistingReport", defaultValue = "true")
    private boolean ignoreExistingReport;

    @Override
    public void execute() {
        // execute CodeNarc verification
    }
}
