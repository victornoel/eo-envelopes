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
import org.assertj.core.api.Assertions;
import org.junit.Test;

/**
 * Tests for {@link GenerateEnvelopeProcessor}.
 *
 * @since 0.0.1
 * @checkstyle JavadocMethodCheck (500 lines)
 */
@SuppressWarnings({"PMD.TooManyMethods", "PMD.AvoidDuplicateLiterals"})
public final class GenerateEnvelopeProcessorTest {

    @Test
    public void nothing() {
        final Compilation compilation = Compiler.javac()
            .withProcessors(new GenerateEnvelopeProcessor())
            .compile(
                JavaFileObjects.forSourceLines(
                    "AnInterface",
                    "public interface AnInterface {}"
                )
            );
        CompilationSubject.assertThat(compilation).succeededWithoutWarnings();
        Assertions.assertThat(compilation.generatedSourceFiles()).isEmpty();
    }

    @Test
    public void noClass() {
        final Compilation compilation = Compiler.javac()
            .withProcessors(new GenerateEnvelopeProcessor())
            .compile(
                JavaFileObjects.forSourceLines(
                    "AClass",
                    "import com.github.victornoel.eo.GenerateEnvelope;",
                    "@GenerateEnvelope",
                    "public class AClass {}"
                )
            );
        CompilationSubject.assertThat(compilation).failed();
        CompilationSubject.assertThat(compilation)
            .hadErrorContaining("only for interfaces");
    }

    @Test
    public void nameModifiersAndField() {
        final Compilation compilation = Compiler.javac()
            .withProcessors(new GenerateEnvelopeProcessor())
            .compile(
                JavaFileObjects.forSourceLines(
                    "AnInterface",
                    "import com.github.victornoel.eo.GenerateEnvelope;",
                    "@GenerateEnvelope",
                    "public interface AnInterface {}"
                )
            );
        CompilationSubject.assertThat(compilation).succeededWithoutWarnings();
        CompilationSubject.assertThat(compilation)
            .generatedSourceFile("AnInterfaceEnvelope")
            .hasSourceEquivalentTo(
                JavaFileObjects.forSourceLines(
                    "AnInterfaceEnvelope",
                    // @checkstyle LineLengthCheck (1 line)
                    "public abstract class AnInterfaceEnvelope implements AnInterface {",
                    "  protected final AnInterface wrapped;",
                    "  public AnInterfaceEnvelope(AnInterface wrapped) {",
                    "    this.wrapped = wrapped;",
                    "  }",
                    "}"
                )
            );
    }

    @Test
    public void packagePreserved() {
        final Compilation compilation = Compiler.javac()
            .withProcessors(new GenerateEnvelopeProcessor())
            .compile(
                JavaFileObjects.forSourceLines(
                    "a.complex.pkg.AnInterface",
                    "package a.complex.pkg;",
                    "import com.github.victornoel.eo.GenerateEnvelope;",
                    "@GenerateEnvelope",
                    "public interface AnInterface {}"
                )
            );
        CompilationSubject.assertThat(compilation).succeededWithoutWarnings();
        CompilationSubject.assertThat(compilation)
            .generatedSourceFile("a.complex.pkg.AnInterfaceEnvelope")
            .containsElementsIn(
                JavaFileObjects.forSourceLines(
                    "a.complex.package.AnInterfaceEnvelope",
                    "package a.complex.pkg;",
                    // @checkstyle LineLengthCheck (1 line)
                    "public abstract class AnInterfaceEnvelope implements AnInterface {",
                    "}"
                )
            );
    }

    @Test
    public void innerInterfaceNamePreserved() {
        final Compilation compilation = Compiler.javac()
            .withProcessors(new GenerateEnvelopeProcessor())
            .compile(
                JavaFileObjects.forSourceLines(
                    "AClass",
                    "package a.pkg;",
                    "import com.github.victornoel.eo.GenerateEnvelope;",
                    "public class AClass {",
                    "  @GenerateEnvelope",
                    "  public interface AnInnerInterface {}",
                    "}"
                )
            );
        CompilationSubject.assertThat(compilation)
            .succeededWithoutWarnings();
        CompilationSubject.assertThat(compilation)
            .generatedSourceFile("a.pkg.AClassAnInnerInterfaceEnvelope")
            .hasSourceEquivalentTo(
                JavaFileObjects.forSourceLines(
                    "a.pkg.AClassAnInnerInterfaceEnvelope",
                    "package a.pkg",
                    // @checkstyle LineLengthCheck (1 line)
                    "public abstract class AClassAnInnerInterfaceEnvelope implements AClass.AnInnerInterface {",
                    "  protected final AClass.AnInnerInterface wrapped;",
                    // @checkstyle LineLengthCheck (1 line)
                    "  public AClassAnInnerInterfaceEnvelope(AClass.AnInnerInterface wrapped) {",
                    "    this.wrapped = wrapped;",
                    "  }",
                    "}"
                )
            );
    }

    @Test
    public void overridesVoidMethod() {
        final Compilation compilation = Compiler.javac()
            .withProcessors(new GenerateEnvelopeProcessor())
            .compile(
                JavaFileObjects.forSourceLines(
                    "AnInterface",
                    "import com.github.victornoel.eo.GenerateEnvelope;",
                    "@GenerateEnvelope",
                    "public interface AnInterface {",
                    "  void test();",
                    "}"
                )
            );
        CompilationSubject.assertThat(compilation).succeededWithoutWarnings();
        CompilationSubject.assertThat(compilation)
            .generatedSourceFile("AnInterfaceEnvelope")
            .hasSourceEquivalentTo(
                JavaFileObjects.forSourceLines(
                    "AnInterfaceEnvelope",
                    "import java.lang.Override;",
                    // @checkstyle LineLengthCheck (1 line)
                    "public abstract class AnInterfaceEnvelope implements AnInterface {",
                    "  protected final AnInterface wrapped;",
                    "  public AnInterfaceEnvelope(AnInterface wrapped) {",
                    "    this.wrapped = wrapped;",
                    "  }",
                    "  @Override",
                    "  public final void test() {",
                    "    wrapped.test();",
                    "  }",
                    "}"
                )
            );
    }

    @Test
    public void overridesMethodWithException() {
        final Compilation compilation = Compiler.javac()
            .withProcessors(new GenerateEnvelopeProcessor())
            .compile(
                JavaFileObjects.forSourceLines(
                    "AnInterface",
                    "import com.github.victornoel.eo.GenerateEnvelope;",
                    "@GenerateEnvelope",
                    "public interface AnInterface {",
                    "  void test() throws Exception;",
                    "}"
                )
            );
        CompilationSubject.assertThat(compilation).succeededWithoutWarnings();
        CompilationSubject.assertThat(compilation)
            .generatedSourceFile("AnInterfaceEnvelope")
            .hasSourceEquivalentTo(
                JavaFileObjects.forSourceLines(
                    "AnInterfaceEnvelope",
                    "import java.lang.Exception;",
                    "import java.lang.Override;",
                    // @checkstyle LineLengthCheck (1 line)
                    "public abstract class AnInterfaceEnvelope implements AnInterface {",
                    "  protected final AnInterface wrapped;",
                    "  public AnInterfaceEnvelope(AnInterface wrapped) {",
                    "    this.wrapped = wrapped;",
                    "  }",
                    "  @Override",
                    "  public final void test() throws Exception {",
                    "    wrapped.test();",
                    "  }",
                    "}"
                )
            );
    }

    @Test
    public void overridesMethodWithReturnType() {
        final Compilation compilation = Compiler.javac()
            .withProcessors(new GenerateEnvelopeProcessor())
            .compile(
                JavaFileObjects.forSourceLines(
                    "AnInterface",
                    "import com.github.victornoel.eo.GenerateEnvelope;",
                    "@GenerateEnvelope",
                    "public interface AnInterface {",
                    "  String test();",
                    "}"
                )
            );
        CompilationSubject.assertThat(compilation).succeededWithoutWarnings();
        CompilationSubject.assertThat(compilation)
            .generatedSourceFile("AnInterfaceEnvelope")
            .hasSourceEquivalentTo(
                JavaFileObjects.forSourceLines(
                    "AnInterfaceEnvelope",
                    "import java.lang.Override;",
                    "import java.lang.String;",
                    // @checkstyle LineLengthCheck (1 line)
                    "public abstract class AnInterfaceEnvelope implements AnInterface {",
                    "  protected final AnInterface wrapped;",
                    "  public AnInterfaceEnvelope(AnInterface wrapped) {",
                    "    this.wrapped = wrapped;",
                    "  }",
                    "  @Override",
                    "  public final String test() {",
                    "    return wrapped.test();",
                    "  }",
                    "}"
                )
            );
    }

    @Test
    public void overridesMethodWithPrimitiveReturnType() {
        final Compilation compilation = Compiler.javac()
            .withProcessors(new GenerateEnvelopeProcessor())
            .compile(
                JavaFileObjects.forSourceLines(
                    "AnInterface",
                    "import com.github.victornoel.eo.GenerateEnvelope;",
                    "@GenerateEnvelope",
                    "public interface AnInterface {",
                    "  int test();",
                    "}"
                )
            );
        CompilationSubject.assertThat(compilation).succeededWithoutWarnings();
        CompilationSubject.assertThat(compilation)
            .generatedSourceFile("AnInterfaceEnvelope")
            .hasSourceEquivalentTo(
                JavaFileObjects.forSourceLines(
                    "AnInterfaceEnvelope",
                    "import java.lang.Override;",
                    // @checkstyle LineLengthCheck (1 line)
                    "public abstract class AnInterfaceEnvelope implements AnInterface {",
                    "  protected final AnInterface wrapped;",
                    "  public AnInterfaceEnvelope(AnInterface wrapped) {",
                    "    this.wrapped = wrapped;",
                    "  }",
                    "  @Override",
                    "  public final int test() {",
                    "    return wrapped.test();",
                    "  }",
                    "}"
                )
            );
    }

    @Test
    public void overridesMethodWithParameters() {
        final Compilation compilation = Compiler.javac()
            .withProcessors(new GenerateEnvelopeProcessor())
            .compile(
                JavaFileObjects.forSourceLines(
                    "AnInterface",
                    "import com.github.victornoel.eo.GenerateEnvelope;",
                    "@GenerateEnvelope",
                    "public interface AnInterface {",
                    "  void test(String a, int b);",
                    "}"
                )
            );
        CompilationSubject.assertThat(compilation).succeededWithoutWarnings();
        CompilationSubject.assertThat(compilation)
            .generatedSourceFile("AnInterfaceEnvelope")
            .hasSourceEquivalentTo(
                JavaFileObjects.forSourceLines(
                    "AnInterfaceEnvelope",
                    "import java.lang.Override;",
                    "import java.lang.String;",
                    // @checkstyle LineLengthCheck (1 line)
                    "public abstract class AnInterfaceEnvelope implements AnInterface {",
                    "  protected final AnInterface wrapped;",
                    "  public AnInterfaceEnvelope(AnInterface wrapped) {",
                    "    this.wrapped = wrapped;",
                    "  }",
                    "  @Override",
                    "  public final void test(String a, int b) {",
                    "    wrapped.test(a, b);",
                    "  }",
                    "}"
                )
            );
    }

    @Test
    public void typeParametersPreserved() {
        final Compilation compilation = Compiler.javac()
            .withProcessors(new GenerateEnvelopeProcessor())
            .compile(
                JavaFileObjects.forSourceLines(
                    "AnInterface",
                    "import com.github.victornoel.eo.GenerateEnvelope;",
                    "@GenerateEnvelope",
                    "public interface AnInterface<A> {}"
                )
            );
        CompilationSubject.assertThat(compilation).succeededWithoutWarnings();
        CompilationSubject.assertThat(compilation)
            .generatedSourceFile("AnInterfaceEnvelope")
            .hasSourceEquivalentTo(
                JavaFileObjects.forSourceLines(
                    "AnInterfaceEnvelope",
                    // @checkstyle LineLengthCheck (1 line)
                    "public abstract class AnInterfaceEnvelope<A> implements AnInterface<A> {",
                    "  protected final AnInterface<A> wrapped;",
                    "  public AnInterfaceEnvelope(AnInterface<A> wrapped) {",
                    "    this.wrapped = wrapped;",
                    "  }",
                    "}"
                )
            );
    }

    @Test
    public void constrainedTypeParametersPreserved() {
        final Compilation compilation = Compiler.javac()
            .withProcessors(new GenerateEnvelopeProcessor())
            .compile(
                JavaFileObjects.forSourceLines(
                    "AnInterface",
                    "import java.util.List;",
                    "import com.github.victornoel.eo.GenerateEnvelope;",
                    "@GenerateEnvelope",
                    "public interface AnInterface<B, A extends List<B>> {}"
                )
            );
        CompilationSubject.assertThat(compilation).succeededWithoutWarnings();
        CompilationSubject.assertThat(compilation)
            .generatedSourceFile("AnInterfaceEnvelope")
            .containsElementsIn(
                JavaFileObjects.forSourceLines(
                    "AnInterfaceEnvelope",
                    "import java.util.List;",
                    // @checkstyle LineLengthCheck (1 line)
                    "public abstract class AnInterfaceEnvelope<B, A extends List<B>> implements AnInterface<B, A> {",
                    "}"
                )
            );
    }
}
