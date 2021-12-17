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
  * With `5.0.0-beta.2`, the new fluent API is largely complete and covers all PureTemplate features.
  * Going forward, no major changes are planned.
* Documentation
  * No "Getting started" guide yet, sorry.
  * All main API classes have Javadoc comments. The generated documentation is available at [javadoc.io](https://javadoc.io/doc/org.puretemplate/puretemplate).
* Maturity
  * Based on StringTemplate 4.3.1, but includes [antlr/stringtemplate4@9a43949](https://github.com/antlr/stringtemplate4/commit/9a439491acc5b17d191316c9b3a99ab7bd340477), which incorporates several changes made later.
* Available on Maven Central
  * Maven
    ```xml
    <dependency>
        <groupId>org.puretemplate</groupId>
        <artifactId>puretemplate</artifactId>
        <version>5.0.0-beta.2</version>
    </dependency>
    ```
  * Gradle
    ```groovy
    implementation 'org.puretemplate:puretemplate:5.0.0-beta.2'
    ```

Feedback is, of course, welcome! Simply [create an issue](https://github.com/puretemplate/puretemplate/issues/new).

## Comparison with StringTemplate
It is planned to extend this comparison. The following should be considered a "sneak peek".

### Bugfixes
* "Strict" rendering: by default, String attribute renderers are not applied to template text ([antlr/stringtemplate4#279](https://github.com/antlr/stringtemplate4/pull/279))
* Groups loaded from file system or classpath only load templates from the same source.
  * In StringTemplate, a group loaded from the classpath may fall back to loading templates from the file system.
* Classpath imports are handled consistently with the behavior of file system imports: `import "/foo"` is an absolute path, `import "foo"` is a relative path.
  * In StringTemplate, the latter case was treated as absolute on the classpath, but relative on the file system.

### Additions & Improvements
* Fluent API for loading groups/templates and for rendering.
  * StringTemplate implements different sources via separate classes and uses a multitude of overloads for different rendering options and targets. 
* Built for Java 11
* Locale-sensitive operations during rendering default to the neutral [`Locale.ROOT`](https://docs.oracle.com/en/java/javase/11/docs/api/java.base/java/util/Locale.html#ROOT). The default locale is only used when requested explicitly via [`useSystemDefaultLocale()`](https://javadoc.io/doc/org.puretemplate/puretemplate/latest/org/puretemplate/Context.html#useSystemDefaultLocale()).
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
* Includes an automatic module name for applications running on the module path ([antlr/stringtemplate4#216](https://github.com/antlr/stringtemplate4/issues/216))
* [`NumberRenderer`](https://javadoc.io/doc/org.puretemplate/puretemplate/latest/org/puretemplate/model/NumberRenderer.html) now implements `AttributeRenderer<Number>` instead of `AttributeRenderer<Object>` so that a compile-time error occurs when registering it for arbitrary classes. 

### Removals
The following StringTemplate features were removed. However, feel free to [create an issue](https://github.com/puretemplate/puretemplate/issues/new) asking to bring back one of them or enable other projects to provide corresponding plugins.

* User interface (`STViz`)
* Ability to unload templates / groups
* Static [`format()`](https://javadoc.io/doc/org.antlr/ST4/latest/org/stringtemplate/v4/ST.html#format-java.lang.String-java.lang.Object...-) method for anonymous template with positional arguments
* Support for StringTemplate v3 style template directories (`STRawGroupDir`)
