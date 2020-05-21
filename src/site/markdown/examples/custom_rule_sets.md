## Custom Rule Sets

By default, CodeNarc Maven Plugin uses the [*Basic*](https://codenarc.github.io/CodeNarc/codenarc-rules-basic.html), [*Exceptions*](https://codenarc.github.io/CodeNarc/codenarc-rules-exceptions.html), and [*Imports*](https://codenarc.github.io/CodeNarc/codenarc-rules-imports.html) rule sets in the analysis. 
However, this default setting can be overriden by specifying both standard CodeNarc rule sets as well as user-defined ones. 

To override the default rule sets, you can use the `<defaultRulesets>` configuration parameter. 
You can also use `<additionalRuleSets>` to specify user-defined rule sets. The two options can be combined, as shown in the following example: 

```xml
<plugin>
    <groupId>com.github.crizzis</groupId>
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
        <defaultRuleSets>
            <defaultRuleSet>rulesets/formatting.xml</defaultRuleSet>
            <defaultRuleSet>rulesets/comments.xml</defaultRuleSet>
            <defaultRuleSet>rulesets/groovyism.xml</defaultRuleSet>
        </defaultRuleSets>
        <additionalRuleSets>
            <additionalRuleSet>custom.xml</additionalRuleSet>
        </additionalRuleSets>
    </configuration>
</plugin>
```
The above configuration will cause the plugin to use both the standard `formatting.xml`, `comments.xml`, and `groovyism.xml` (**instead** of the default setting) and the user-defined `custom.xml` rule set file (defined in the project root directory). 

A detailed reference of the standard CodeNarc rule sets you can use can be found on the [CodeNarc website](https://codenarc.github.io/CodeNarc/codenarc-rule-index.html). It also shows [how to create a rule set](https://codenarc.github.io/CodeNarc/codenarc-creating-ruleset.html). 