## Usage
The plugin can be used either as a quality gate in the build process, a reporting plugin in the `site` lifecycle, or as a standalone HTML report generator. Regardless of whether the plugin is configured to produce an HTML report or not, CodeNarc's standard `CodeNarc.xml` will be generated in the *target* directory by default. 

### Default Settings
By default, the plugin assumes that:
* `src/main/groovy` is the compile source root (by default, compile sources are **included** in the analysis)
* `src/test/groovy` is the test source root (by default, test sources are **excluded** from the analysis)
* Groovy sources follow the `**/*.groovy` naming convention
* The scope of the analysis should be limited to the [*Basic*](https://codenarc.github.io/CodeNarc/codenarc-rules-basic.html), [*Exceptions*](https://codenarc.github.io/CodeNarc/codenarc-rules-exceptions.html), and [*Imports*](https://codenarc.github.io/CodeNarc/codenarc-rules-imports.html) rule sets

### Verifying Code Quality as Part of the Build Process

For CodeNarc Maven Plugin to serve as a quality gate in the build process, add an execution of the `[codenarc:verify](verify-mojo.html)` goal in the `<build>` section of your `pom.xml`: 

```xml
<build>
    <plugins>
        ...
        <plugin>
            <groupId>io.github.crizzis</groupId>
            <artifactId>codenarc-maven-plugin</artifactId>
            <version>0.1</version>
            <executions>
                <execution>
                    <goals>
                        <goal>verify</goal>
                    </goals>
                </execution>
            </executions>
            <configuration>
                <maxPriority1Violations>2</maxPriority1Violations>
                <maxPriority2Violations>3</maxPriority2Violations>
                <maxPriority3Violations>1</maxPriority3Violations>
            </configuration>
        </plugin>
    </plugins>
</build>
```

In the above example, `<maxPriorityXViolations>` represents the maximum allowed number of priority X violations before failing the build. The `verify` goal binds to the `verify` phase by default; use `<phase>` to override that behavior. 

With the above configuration in place, `mvn install` will fail during the `verify` phase if the number of violations for either priority exceeds the number defined in the corresponding configuration property. 

### Generating a Report as Part of the Site Lifecycle

The CodeNarc Plugin can also be used for reporting: simply add the plugin definition to the `<reporting>` section of your `pom.xml`: 

```xml
<reporting>
    <plugins>
        ...
        <plugin>
            <groupId>io.github.crizzis</groupId>
            <artifactId>codenarc-maven-plugin</artifactId>
            <version>0.1</version>
        </plugin>
    </plugins>
</reporting>
```

With the above configuration in place, `mvn site` will result in a `codenarc.html` report being generated in the `target/site` directory. The report will appear in the generated site as *CodeNarc Report*, in the *Project Reports* section. 

Note that, since the reporting goal calls upon the `verify` goal first to perform the analysis, most of the configuration options availble for the `verify` goal will also have an effect here. 

### Using HTML Report Generation as a Standalone Goal

Like with any other reporting plugin, the reporting goal can also be used in standalone mode, rather than as part of the reporting mechanism. To do that, simply reference the [codenarc:codenarc](codenarc-mojo.html) goal directly in your `<build>` section: 

```xml
<build>
    <plugins>
        <plugin>
            <groupId>io.github.crizzis</groupId>
            <artifactId>codenarc-maven-plugin</artifactId>
            <executions>
                <execution>
                    <goals>
                        <goal>codenarc</goal>
                    </goals>
                </execution>
            </executions>
        </plugin>
    </plugins>
</build>
```

With this configuration, `mvn codenarc:codenarc` will cause a `codenarc.html` report to be generated in the `target/site` directory. 