## Separate Steps for Analysis and Failing the Build

The typical use of the `codenarc:verify` goal is to perform CodeNarc analysis (producing a CodeNarc XML report) and fail the build if it does not meet the configured quality criteria. All of this happens within a single execution of the goal. 

For certain customized build, though, it might make sense to perform the analysis in one build phase, and then act upon the result in another. An example scenario for such a build is when you need to *both* generate the HTML report *and* fail a build with substandard code quality 
(if *codenarc:codenarc* is configured to fail the build under certain conditions - either as a standalone goal or as part of reporting - then a failing build will prevent the HTML report from being generated). 

In order to achieve such a goal, the `<ignoreExistingReport>` flag can be used. By default, the flag is set to `true`, meaning that `codenarc:verify` will always perform a CodeNarc analysis from scratch. Setting the flag to `false` will cause `codenarc:verify` to use previously generated `CodeNarc.xml`, if exists. 

For example, to perform CodeNarc analysis during the `process-resources` phase but not fail the build just yet, you can use the following configuration: 

```xml
<build>
    <plugins>
        ...
        <plugin>
            <groupId>com.github.crizzis</groupId>
            <artifactId>codenarc-maven-plugin</artifactId>
            <version>0.1</version>
            <executions>
                <execution>
                    <id>perform-codenarc-analysis</id>
                    <phase>process-resources</phase>
                    <goals>
                        <goal>verify</goal>
                    </goals>
                </execution>
            </executions>
        </plugin>
    </plugins>
</build>
```

You can then define another execution of the `codenarc:verify` goal to consume the report generated in the previous execution (and fail the build if the specified criteria are not met), in another lifecycle phase (e.g. `verify`)

```xml
<execution>
    <id>analyze-results</id>
    <phase>verify</phase>
    <goals>
        <goal>verify</goal>
    </goals>
    <configuration>
        <ignoreExistingReport>false</ignoreExistingReport>
        <maxPriority1Violations>2</maxPriority1Violations>
        <maxPriority2Violations>3</maxPriority2Violations>
        <maxPriority3Violations>1</maxPriority3Violations>
    </configuration>
</execution>
```

**Warning**: this is an advanced use case scenario. Please take special care when using the above approach and make sure you understand the implications it may have on your build. 
In particular, failing to execute the `clean` goal may, in some instances, result in stale analysis results getting consumed by the *codenarc:verify* goal. 