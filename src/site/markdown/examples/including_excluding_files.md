## Including/Excluding Files

The default behavior of the plugin is to include all source files matching the pattern `**/*.groovy`. Also, test sources are excluded by default. 

If you want to include test sources in the analysis, use: 

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
                <includeTests>true</includeTests>
            </configuration>
        </plugin>
    </plugins>
</build>
```
Similarly, `<includeMain>false</includeMain>` can be used to disable the default compile source inclusion. 

To include/exclude specific files, use the `<includes>`/`<excludes>` property with a collection of Ant-style patterns: 

```xml
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
        <includes>
            <include>**/*Included.groovy</include>
        </includes>
        <excludes>
            <exclude>**/*NotReallyIncluded.groovy</exclude>
        </excludes>
    </configuration>
</plugin>
```
The above configuration will cause files ending with `Included.groovy` (but **not** `NotReallyIncluded.groovy`) to be the only files included in the analysis. 

If both `<includes>` and `<excludes>` are present, `<excludes>` takes precedence over `<includes>` for a particular source file. 