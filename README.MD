# CodeNarc Maven Plugin

A reporting plugin for CodeNarc that doesn't suck. 

### Basic Usage

As a Maven report:

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

As part of the `install` lifecycle: 

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

See the [docs](https://crizzis.github.io/codenarc-maven-plugin/) for more info. 