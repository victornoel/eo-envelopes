# Elegant Objects Wrap

Distributed under the GPL3 but don't forget the generated code is not covered by the license.

## Why

See http://www.yegor256.com/2017/01/31/decorating-envelopes.html for an explanation of the requirements.

## How

Enable Annotation Processing in your IDE to take advantage of it during development.

Simply annotate your interfaces with `@GenerateWrap` and a `Wrap` class will be generated for it.
You can now extend it to write a decorating envelope without managing the delegation yourself.

## What

```java
package test;

import com.github.victornoel.eo.GenerateWrap;

@GenerateWrap
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

public abstract class TestWrap implements Test {
  protected final Test wrapped;

  public TestWrap(Test wrapped) {
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

import com.github.victornoel.eo.GenerateWrap;

public class AClass {

    @GenerateWrap
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

public abstract class AClassTestWrap implements AClass.Test {
  protected final AClass.Test wrapped;

  public AClassTestWrap(AClass.Test wrapped) {
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

## TODO

- [ ] interfaces extending other interfaces
- [ ] interfaces overriding methods of extended interface
- [ ] generate a generic wrapped field
