[![Build Status](https://travis-ci.org/victornoel/eo-envelopes.png?branch=master)](https://travis-ci.org/victornoel/eo-envelopes)
[![Maven Central](https://img.shields.io/maven-central/v/com.github.victornoel.eo/eo-envelopes.svg)](http://search.maven.org/#search%7Cga%7C1%7Cg%3A%22com.github.victornoel.eo%22%20AND%20a%3A%22eo-envelopes%22)

# Elegant Objects Envelopes

Distributed under the GPL 3.0.

Note that the output of the processor is not covered by this license and can be used without any restrictions.

## Why

See http://www.yegor256.com/2017/01/31/decorating-envelopes.html for an explanation of the requirements.

## How

For the latest version, see the Maven Central badge at the top, SNAPSHOTs are deployed to OSS Sonatype.

Add the following dependency:

```xml
<dependency>
  <groupId>com.github.victornoel.eo</groupId>
  <artifactId>eo-envelopes-annotations</artifactId>
  <version><!-- Version --></version>
  <scope>provided</scope>
</dependency>
```

And the processor:

```xml
<build>
  <pluginManagement>
    <plugins>
      <plugin>
        <artifactId>maven-compiler-plugin</artifactId>
        <configuration>
          <annotationProcessorPaths>
            <annotationProcessorPath>
              <groupId>com.github.victornoel.eo</groupId>
              <artifactId>eo-envelopes</artifactId>
              <version><!-- Version --></version>
            </annotationProcessorPath>
          </annotationProcessorPaths>
        </configuration>
      </plugin>
    </plugins>
  </pluginManagement>
</build>
```

Enable Annotation Processing in your IDE to take advantage of it during development.

Simply annotate your interfaces with `@GenerateEnvelope` and an envelope class will be generated for it.
You can now extend it to write a decorating envelope without managing the delegation yourself.

## What

```java
package test;

import com.github.victornoel.eo.GenerateEnvelope;

@GenerateEnvelope
public interface Test {
    String lol(int ab);
    void lol(Double a) throws Exception;
}
```

Gives you:

```java
package test;

import java.lang.Double;
import java.lang.Exception;
import java.lang.Override;
import java.lang.String;

public abstract class TestEnvelope implements Test {
  protected final Test wrapped;

  public TestEnvelope(Test wrapped) {
    this.wrapped = wrapped;
  }

  @Override
  public final String lol(int ab) {
    return wrapped.lol(ab);
  }

  @Override
  public final void lol(Double a) throws Exception {
    wrapped.lol(a);
  }
}
```

Inner interfaces are also supported:

```java
package test;

import com.github.victornoel.eo.GenerateEnvelope;

public class AClass {

    @GenerateEnvelope
    public interface Test {
        
        String lol(int ab);
        
        void lol(Double a) throws Exception;
    }
}
```

Gives you

```java
package test;

import java.lang.Double;
import java.lang.Exception;
import java.lang.Override;
import java.lang.String;
import javax.annotation.Generated;

@Generated("com.github.victornoel.eo.apt.GenerateEnvelopeProcessor")
public abstract class AClassTestEnvelope implements AClass.Test {
  protected final AClass.Test wrapped;

  public AClassTestEnvelope(AClass.Test wrapped) {
    this.wrapped = wrapped;
  }

  @Override
  public final String lol(int ab) {
    return wrapped.lol(ab);
  }

  @Override
  public final void lol(Double a) throws Exception {
    wrapped.lol(a);
  }
}
```
