## Custom Source Roots

By default, CodeNarc Maven Plugin assumes `src/main/groovy` to be the compile sources root, and `src/test/groovy` to be the test sources root. 
If you need to include additional sources in the analysis, or use a nonstandard project layout, you can use the `<sources>` and `<testSources>` properties:

```xml
<plugin>
    <groupId>com.github.crizzis</groupId>
    <artifactId>codenarc-maven-plugin</artifactId>
    <executions>
        <execution>
            <goals>
                <goal>verify</goal>
            </goals>
        </execution>
    </executions>
    <configuration>
        <sources>
            <fileSet>
                <directory>src/main/groovy</directory>
            </fileSet>
            <fileSet>
                <directory>src/main/additional</directory>
            </fileSet>
        </sources>
        <testSources>
            <fileSet>
                <directory>src/test-sources/groovy</directory>
                <excludes>
                    <exclude>**/*ExcludedTest.groovy</exclude>
                </excludes>
            </fileSet>
        </testSources>
        <includeTests>true</includeTests>
    </configuration>
</plugin>
``` 
Note that while the element type of `<sources>` and `<testSources>` is a [`FileSet`](https://maven.apache.org/shared/file-management/fileset.html), only the `<directory>`, `<includes>`, and `<excludes>` have any effect. 

The `<sources>` and `<testSources>` properties combine with `<includes>` and `<excludes>`. In other words, the following configurations: 

```xml
<sources>
    <fileSet>
        <directory>src/main/additional</directory>
    </fileSet>
    <exludes>
        <exclude>**/*.gr</exclude>    
    </exludes>
</sources>
```
and: 
```xml
<sources>
    <fileSet>
        <directory>src/main/additional</directory>
        <exludes>
            <exclude>**/*.gr</exclude>    
        </exludes>
    </fileSet>
</sources>
```
are equivalent. Also note that if you do not override the `<includes>` property, the default `**/*.groovy` is still in effect, meaning that it will apply to all custom filesets **in addition** to fileset-specific rules.  