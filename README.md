[![Maven Central](https://maven-badges.herokuapp.com/maven-central/org.puretemplate/puretemplate/badge.svg)](https://maven-badges.herokuapp.com/maven-central/org.puretemplate/puretemplate)

[![javadoc](https://javadoc.io/badge2/org.puretemplate/puretemplate/javadoc.svg)](https://javadoc.io/doc/org.puretemplate/puretemplate)

[![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=org.puretemplate%3Apuretemplate&metric=alert_status)](https://sonarcloud.io/summary/overall?id=org.puretemplate%3Apuretemplate)

# PureTemplate
This fork of [StringTemplate](https://github.com/antlr/stringtemplate4) has the following goals:

* Provide a more modern API
* Fix bugs and make improvements in a way that StringTemplate could not do without sacrificing backwards compatibility
* Rigidly encapsulate implementation details to reduce constraints on future evolution such as adding thread safety
* Modernize the original Java 6 code base to take advantage of and offer support for newer Java language versions
* Enforce strict model-view separation just like StringTemplate does
* Keep the StringTemplate language unchanged

## Current status
* API
  * With `5.0.0-beta.1`, the new fluent API is largely complete and covers all PureTemplate features.
  * Going forward, no major changes are planned.
* Documentation
  * No "Getting started" guide yet, sorry.
  * [Javadoc](https://javadoc.io/doc/org.puretemplate/puretemplate): coverage both for classes inherited from StringTemplate and new classes still needs to be improved.
* Maturity
  * Based on StringTemplate 4.3.1, but includes [antlr/stringtemplate4@9a43949](https://github.com/antlr/stringtemplate4/commit/9a439491acc5b17d191316c9b3a99ab7bd340477), which incorporates several changes made later.
* Available on Maven Central
  * Maven
    ```xml
    <dependency>
        <groupId>org.puretemplate</groupId>
        <artifactId>puretemplate</artifactId>
        <version>5.0.0-beta.1</version>
    </dependency>
    ```
  * Gradle
    ```groovy
    implementation 'org.puretemplate:puretemplate:5.0.0-beta.1'
    ```

Feedback is, of course, welcome! Simply [create an issue](https://github.com/puretemplate/puretemplate/issues/new).

## Comparison with StringTemplate
It is planned to extend this comparison. The following should be considered a "sneak peek".

### Bugfixes
* "Strict" rendering: by default, String attribute renderers are not applied to template text ([antlr/stringtemplate4#279](https://github.com/antlr/stringtemplate4/pull/279))

### Additions & Improvements
* Fluent API for loading groups/templates and for rendering
* Built for Java 11
* Replaced unusual aggregates API with one that should feel more Java-like:
    ```java
    // PureTemplate
    context.add("items",
      Aggregate.build()
          .properties("firstName", "lastName", "id")
          .withValues("Tom", "Burns", 34));

    // StringTemplate
    st.addAggr("items.{firstName, lastName, id}", "Tom", "Burns", 34);
    ```

### Removals
The following StringTemplate features were removed. However, feel free to [create an issue](https://github.com/puretemplate/puretemplate/issues/new) asking to bring back one of them or enable other projects to provide corresponding plugins.

* User interface (`STViz`)
* Ability to unload templates / groups
* Static [`format()`](https://javadoc.io/doc/org.antlr/ST4/latest/org/stringtemplate/v4/ST.html#format-java.lang.String-java.lang.Object...-) method for anonymous template with positional arguments
* Support for StringTemplate v3 style template directories (`STRawGroupDir`)
