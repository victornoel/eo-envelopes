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
 * @checkstyle JavadocMethodCheck (1000 lines)
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
                    "import javax.annotation.Generated;",
                    // @checkstyle LineLengthCheck (1 line)
                    "@Generated(\"com.github.victornoel.eo.apt.GenerateEnvelopeProcessor\")",
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
                    "import javax.annotation.Generated;",
                    // @checkstyle LineLengthCheck (1 line)
                    "@Generated(\"com.github.victornoel.eo.apt.GenerateEnvelopeProcessor\")",
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
                    "import javax.annotation.Generated;",
                    // @checkstyle LineLengthCheck (1 line)
                    "@Generated(\"com.github.victornoel.eo.apt.GenerateEnvelopeProcessor\")",
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
    public void delegatesVoidMethod() {
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
                    "import javax.annotation.Generated;",
                    // @checkstyle LineLengthCheck (1 line)
                    "@Generated(\"com.github.victornoel.eo.apt.GenerateEnvelopeProcessor\")",
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
    public void delegatesMethodWithException() {
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
                    "import javax.annotation.Generated;",
                    // @checkstyle LineLengthCheck (1 line)
                    "@Generated(\"com.github.victornoel.eo.apt.GenerateEnvelopeProcessor\")",
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
    public void delegatesMethodWithReturnType() {
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
                    "import javax.annotation.Generated;",
                    // @checkstyle LineLengthCheck (1 line)
                    "@Generated(\"com.github.victornoel.eo.apt.GenerateEnvelopeProcessor\")",
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
    public void delegatesMethodWithPrimitiveReturnType() {
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
                    "import javax.annotation.Generated;",
                    // @checkstyle LineLengthCheck (1 line)
                    "@Generated(\"com.github.victornoel.eo.apt.GenerateEnvelopeProcessor\")",
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
    public void delegatesMethodWithParameters() {
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
                    "import javax.annotation.Generated;",
                    // @checkstyle LineLengthCheck (1 line)
                    "@Generated(\"com.github.victornoel.eo.apt.GenerateEnvelopeProcessor\")",
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
                    "import javax.annotation.Generated;",
                    // @checkstyle LineLengthCheck (1 line)
                    "@Generated(\"com.github.victornoel.eo.apt.GenerateEnvelopeProcessor\")",
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
                    "import javax.annotation.Generated;",
                    // @checkstyle LineLengthCheck (1 line)
                    "@Generated(\"com.github.victornoel.eo.apt.GenerateEnvelopeProcessor\")",
                    // @checkstyle LineLengthCheck (1 line)
                    "public abstract class AnInterfaceEnvelope<B, A extends List<B>> implements AnInterface<B, A> {",
                    "}"
                )
            );
    }

    @Test
    public void delegatesMethodsOfSuperSuperInterfaces() {
        final Compilation compilation = Compiler.javac()
            .withProcessors(new GenerateEnvelopeProcessor())
            .compile(
                JavaFileObjects.forSourceLines(
                    "ASuperSuperInterface",
                    "public interface ASuperSuperInterface {",
                    "  void test(String a, int b);",
                    "}"
                ),
                JavaFileObjects.forSourceLines(
                    "ASuperInterface",
                    // @checkstyle LineLengthCheck (1 line)
                    "public interface ASuperInterface extends ASuperSuperInterface {",
                    "  int test2(String a, int b) throws Exception;",
                    "}"
                ),
                JavaFileObjects.forSourceLines(
                    "AnInterface",
                    "import com.github.victornoel.eo.GenerateEnvelope;",
                    "import javax.annotation.Generated;",
                    // @checkstyle LineLengthCheck (1 line)
                    "@Generated(\"com.github.victornoel.eo.apt.GenerateEnvelopeProcessor\")",
                    "@GenerateEnvelope",
                    "public interface AnInterface extends ASuperInterface {",
                    "  void test3();",
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
                    "import java.lang.String;",
                    "import javax.annotation.Generated;",
                    // @checkstyle LineLengthCheck (1 line)
                    "@Generated(\"com.github.victornoel.eo.apt.GenerateEnvelopeProcessor\")",
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
                    "  @Override",
                    // @checkstyle LineLengthCheck (1 line)
                    "  public final int test2(String a, int b) throws Exception {",
                    "    return wrapped.test2(a, b);",
                    "  }",
                    "  @Override",
                    "  public final void test3() {",
                    "    wrapped.test3();",
                    "  }",
                    "}"
                )
            );
    }

    @Test
    public void delegatesMethodsOfMultipleSuperInterfaces() {
        final Compilation compilation = Compiler.javac()
            .withProcessors(new GenerateEnvelopeProcessor())
            .compile(
                JavaFileObjects.forSourceLines(
                    "ASuperInterface1",
                    "public interface ASuperInterface1 {",
                    "  void test1(String a, int b);",
                    "}"
                ),
                JavaFileObjects.forSourceLines(
                    "ASuperInterface2",
                    "public interface ASuperInterface2 {",
                    "  void test2(String a, int b);",
                    "}"
                ),
                JavaFileObjects.forSourceLines(
                    "AnInterface",
                    "import com.github.victornoel.eo.GenerateEnvelope;",
                    "@GenerateEnvelope",
                    // @checkstyle LineLengthCheck (1 line)
                    "public interface AnInterface extends ASuperInterface1, ASuperInterface2 {",
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
                    "import java.lang.String;",
                    "import javax.annotation.Generated;",
                    // @checkstyle LineLengthCheck (1 line)
                    "@Generated(\"com.github.victornoel.eo.apt.GenerateEnvelopeProcessor\")",
                    // @checkstyle LineLengthCheck (1 line)
                    "public abstract class AnInterfaceEnvelope implements AnInterface {",
                    "  protected final AnInterface wrapped;",
                    "  public AnInterfaceEnvelope(AnInterface wrapped) {",
                    "    this.wrapped = wrapped;",
                    "  }",
                    "  @Override",
                    "  public final void test1(String a, int b) {",
                    "    wrapped.test1(a, b);",
                    "  }",
                    "  @Override",
                    "  public final void test2(String a, int b) {",
                    "    wrapped.test2(a, b);",
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
    public void delegatesMethodsOfSuperInterfacesOnlyOnce() {
        final Compilation compilation = Compiler.javac()
            .withProcessors(new GenerateEnvelopeProcessor())
            .compile(
                JavaFileObjects.forSourceLines(
                    "ASuperInterface1",
                    "public interface ASuperInterface1 {",
                    "  void test(String a, int b);",
                    "}"
                ),
                JavaFileObjects.forSourceLines(
                    "ASuperInterface2",
                    "public interface ASuperInterface2 {",
                    "  void test(String a, int b);",
                    "}"
                ),
                JavaFileObjects.forSourceLines(
                    "AnInterface",
                    "import com.github.victornoel.eo.GenerateEnvelope;",
                    "@GenerateEnvelope",
                    // @checkstyle LineLengthCheck (1 line)
                    "public interface AnInterface extends ASuperInterface1, ASuperInterface2 {",
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
                    "import java.lang.String;",
                    "import javax.annotation.Generated;",
                    // @checkstyle LineLengthCheck (1 line)
                    "@Generated(\"com.github.victornoel.eo.apt.GenerateEnvelopeProcessor\")",
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
                    "  @Override",
                    "  public final void test() {",
                    "    wrapped.test();",
                    "  }",
                    "}"
                )
            );
    }

    @Test
    public void delegatesMethodsOfOverridedSuperInterfaces() {
        final Compilation compilation = Compiler.javac()
            .withProcessors(new GenerateEnvelopeProcessor())
            .compile(
                JavaFileObjects.forSourceLines(
                    "ASuperInterface",
                    "public interface ASuperInterface {",
                    "  void test1(String a, int b) throws Exception;",
                    "}"
                ),
                JavaFileObjects.forSourceLines(
                    "AnInterface",
                    "import com.github.victornoel.eo.GenerateEnvelope;",
                    "@GenerateEnvelope",
                    "public interface AnInterface extends ASuperInterface {",
                    "  void test();",
                    "  @Override",
                    "  void test1(String a, int b);",
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
                    "import javax.annotation.Generated;",
                    // @checkstyle LineLengthCheck (1 line)
                    "@Generated(\"com.github.victornoel.eo.apt.GenerateEnvelopeProcessor\")",
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
                    "  @Override",
                    "  public final void test1(String a, int b) {",
                    "    wrapped.test1(a, b);",
                    "  }",
                    "}"
                )
            );
    }

    @Test
    public void delegatesMethodsOfSuperInterfacesWithConcreteParameters() {
        final Compilation compilation = Compiler.javac()
            .withProcessors(new GenerateEnvelopeProcessor())
            .compile(
                JavaFileObjects.forSourceLines(
                    "ASuperInterface",
                    "public interface ASuperInterface<A> {",
                    "  void test(A a);",
                    "}"
                ),
                JavaFileObjects.forSourceLines(
                    "AnInterface",
                    "import com.github.victornoel.eo.GenerateEnvelope;",
                    "@GenerateEnvelope",
                    // @checkstyle LineLengthCheck (1 line)
                    "public interface AnInterface<B> extends ASuperInterface<B> {",
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
                    "import javax.annotation.Generated;",
                    // @checkstyle LineLengthCheck (1 line)
                    "@Generated(\"com.github.victornoel.eo.apt.GenerateEnvelopeProcessor\")",
                    // @checkstyle LineLengthCheck (1 line)
                    "public abstract class AnInterfaceEnvelope<B> implements AnInterface<B> {",
                    "  protected final AnInterface<B> wrapped;",
                    "  public AnInterfaceEnvelope(AnInterface<B> wrapped) {",
                    "    this.wrapped = wrapped;",
                    "  }",
                    "  @Override",
                    "  public final void test(B a) {",
                    "    wrapped.test(a);",
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
    public void delegatesMethodsOfInterfacesWithConcreteComplexParameters() {
        final Compilation compilation = Compiler.javac()
            .withProcessors(new GenerateEnvelopeProcessor())
            .compile(
                JavaFileObjects.forSourceLines(
                    "AnInterface",
                    "import com.github.victornoel.eo.GenerateEnvelope;",
                    "import java.lang.Iterable;",
                    "@GenerateEnvelope",
                    // @checkstyle LineLengthCheck (1 line)
                    "public interface AnInterface extends Iterable<String> {",
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
                    "import java.util.Iterator;",
                    "import java.util.Spliterator;",
                    "import java.util.function.Consumer;",
                    "import javax.annotation.Generated;",
                    // @checkstyle LineLengthCheck (1 line)
                    "@Generated(\"com.github.victornoel.eo.apt.GenerateEnvelopeProcessor\")",
                    // @checkstyle LineLengthCheck (1 line)
                    "public abstract class AnInterfaceEnvelope implements AnInterface {",
                    "  protected final AnInterface wrapped;",
                    "  public AnInterfaceEnvelope(AnInterface wrapped) {",
                    "    this.wrapped = wrapped;",
                    "  }",
                    "  @Override",
                    "  public final Iterator<String> iterator() {",
                    "    return wrapped.iterator();",
                    "  }",
                    "  @Override",
                    // @checkstyle LineLengthCheck (1 line)
                    "  public final void forEach(Consumer<? super String> arg0) {",
                    "    wrapped.forEach(arg0);",
                    "  }",
                    "  @Override",
                    "  public final Spliterator<String> spliterator() {",
                    "    return wrapped.spliterator();",
                    "  }",
                    "}"
                )
            );
    }
}
