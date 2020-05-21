## CodeNarc Maven Plugin

The CodeNarc Maven Plugin provides a way to incorporate the [CodeNarc](https://codenarc.github.io/CodeNarc/) static Groovy analysis tool into your Maven build. 
It can be used either as a reporting or a regular plugin, with optional HTML report generation. See a [sample generated report](./sample/codenarc.html) here. 

The current version of the plugin uses [CodeNarc 1.5](https://mvnrepository.com/artifact/org.codenarc/CodeNarc/1.5). 

Feel free to report bugs and submit feature requests to [GitHub](https://github.com/crizzis/codenarc-maven-plugin/issues). 

### Goals Overview

The CodeNarc Maven Plugin has two goals:

* [codenarc:verify](verify-mojo.html) - performs CodeNarc analysis, and optionally fails the build if specified quality criteria are not met
* [codenarc:codenarc](codenarc-mojo.html) - creates a CodeNarc report (implies the execution of the `codenarc:verify` goal)

### Usage

Detailed usage instructions can be found on the [usage page](usage.html). Also, make sure to check out the [FAQ](faq.html). 
More complex use cases and sample reports are shown in the *Examples* section. A couple of working [example projects](https://github.com/crizzis/codenarc-maven-plugin/tree/master/codenarc-maven-plugin/src/test/resources/projects) can also be found in the source code repository. 

### Examples

* [Custom Rule Sets](./examples/custom_rule_sets.html)
* [Including/Excluding Files](./examples/including_excluding_files.html)
* [Custom Source Roots](./examples/custom_source_roots.html)
* [Separate Steps for Analysis and Failing the Build](./examples/separate_steps_for_analysis_and_failing_the_build.html)