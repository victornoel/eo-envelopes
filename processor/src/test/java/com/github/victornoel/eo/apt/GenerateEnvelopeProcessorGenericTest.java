/*
 * EO-Envelopes
 * Copyright (C) 2018 Victor Noël
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package com.github.victornoel.eo.apt;

import com.google.testing.compile.Compilation;
import com.google.testing.compile.CompilationSubject;
import com.google.testing.compile.Compiler;
import com.google.testing.compile.JavaFileObjects;
import org.junit.Test;

/**
 * Tests for {@link GenerateEnvelopeProcessor}.
 *
 * @since 0.0.1
 */
@SuppressWarnings("PMD.AvoidDuplicateLiterals")
public final class GenerateEnvelopeProcessorGenericTest {

    @Test
    public void markerInterfaceWithGenericEnvelope() {
        final Compilation compilation = Compiler.javac()
            .withProcessors(new GenerateEnvelopeProcessor())
            .compile(
                JavaFileObjects.forSourceLines(
                    "Foo",
                    "import com.github.victornoel.eo.GenerateEnvelope;",
                    "@GenerateEnvelope(generic = true)",
                    "public interface Foo {}"
                )
            );
        CompilationSubject.assertThat(compilation).succeededWithoutWarnings();
        CompilationSubject.assertThat(compilation)
            .generatedSourceFile("FooEnvelope")
            .hasSourceEquivalentTo(
                JavaFileObjects.forSourceLines(
                    "FooEnvelope",
                    "import javax.annotation.Generated;",
                    // @checkstyle LineLengthCheck (1 line)
                    "@Generated(\"com.github.victornoel.eo.apt.GenerateEnvelopeProcessor\")",
                    // @checkstyle LineLengthCheck (1 line)
                    "public abstract class FooEnvelope<W extends Foo> implements Foo {",
                    "  protected final W wrapped;",
                    "  public FooEnvelope(W wrapped) {",
                    "    this.wrapped = wrapped;",
                    "  }",
                    "}"
                )
            );
    }

    @Test
    public void samInterfaceWithGenericEnvelope() {
        final Compilation compilation = Compiler.javac()
            .withProcessors(new GenerateEnvelopeProcessor())
            .compile(
                JavaFileObjects.forSourceLines(
                    "Foo",
                    "import com.github.victornoel.eo.GenerateEnvelope;",
                    "@GenerateEnvelope(generic = true)",
                    "public interface Foo { Foo itself(); }"
                )
            );
        CompilationSubject.assertThat(compilation).succeededWithoutWarnings();
        CompilationSubject.assertThat(compilation)
            .generatedSourceFile("FooEnvelope")
            .hasSourceEquivalentTo(
                JavaFileObjects.forSourceLines(
                    "FooEnvelope",
                    "import java.lang.Override;",
                    "import javax.annotation.Generated;",
                    // @checkstyle LineLengthCheck (1 line)
                    "@Generated(\"com.github.victornoel.eo.apt.GenerateEnvelopeProcessor\")",
                    "public abstract class FooEnvelope<W extends Foo> implements Foo {",
                    "  protected final W wrapped;",
                    "  public FooEnvelope(W wrapped) {",
                    "    this.wrapped = wrapped;",
                    "  }",
                    "  @Override",
                    "  public final Foo itself() {",
                    "   return wrapped.itself();",
                    "  }",
                    "}"
                )
            );
    }

    @Test
    public void samInterfaceWithGenericEnvelopeAndGenericParameter() {
        final Compilation compilation = Compiler.javac()
            .withProcessors(new GenerateEnvelopeProcessor())
            .compile(
                JavaFileObjects.forSourceLines(
                    "Foo",
                    "import com.github.victornoel.eo.GenerateEnvelope;",
                    "import java.util.function.Supplier;",
                    "@GenerateEnvelope(generic = true)",
                    "public interface Foo<X> extends Supplier<X> {};"
                )
            );
        CompilationSubject.assertThat(compilation).succeededWithoutWarnings();
        CompilationSubject.assertThat(compilation)
            .generatedSourceFile("FooEnvelope")
            .hasSourceEquivalentTo(
                JavaFileObjects.forSourceLines(
                    "FooEnvelope",
                    "import java.lang.Override;",
                    "import javax.annotation.Generated;",
                    // @checkstyle LineLengthCheck (1 line)
                    "@Generated(\"com.github.victornoel.eo.apt.GenerateEnvelopeProcessor\")",
                    "public abstract class FooEnvelope<X, W extends Foo<X>> implements Foo<X> {",
                    "  protected final W wrapped;",
                    "  public FooEnvelope(W wrapped) {",
                    "    this.wrapped = wrapped;",
                    "  }",
                    "  @Override",
                    "  public final X get() {",
                    "   return wrapped.get();",
                    "  }",
                    "}"
                )
            );
    }
}
