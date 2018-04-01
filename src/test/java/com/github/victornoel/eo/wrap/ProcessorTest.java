/**
 * EO-Wraps
 * Copyright (C) 2018  Victor Noël
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
package com.github.victornoel.eo.wrap;

import com.google.testing.compile.Compilation;
import com.google.testing.compile.CompilationSubject;
import com.google.testing.compile.Compiler;
import com.google.testing.compile.JavaFileObjects;
import org.assertj.core.api.Assertions;
import org.junit.Test;

public final class ProcessorTest {

    @Test
    public void nothing() {
        final Compilation compilation = Compiler.javac()
            .withProcessors(new Processor())
            .compile(
                JavaFileObjects.forSourceLines(
                    "AnInterface",
                    "public interface AnInterface {}"));
        CompilationSubject.assertThat(compilation).succeededWithoutWarnings();
        Assertions.assertThat(compilation.generatedSourceFiles()).isEmpty();
    }

    @Test
    public void noClass() {
        final Compilation compilation = Compiler.javac()
            .withProcessors(new Processor())
            .compile(
                JavaFileObjects.forSourceLines(
                    "AClass",
                    "import com.github.victornoel.eo.GenerateWrap;",
                    "@GenerateWrap",
                    "public class AClass {}"));
        CompilationSubject.assertThat(compilation).failed();
        CompilationSubject.assertThat(compilation)
            .hadErrorContaining("only for interfaces");
    }

    @Test
    public void nameModifiersAndField() {
        final Compilation compilation = Compiler.javac()
            .withProcessors(new Processor())
            .compile(
                JavaFileObjects.forSourceLines(
                    "AnInterface",
                    "import com.github.victornoel.eo.GenerateWrap;",
                    "@GenerateWrap",
                    "public interface AnInterface {}"));
        CompilationSubject.assertThat(compilation).succeededWithoutWarnings();
        CompilationSubject.assertThat(compilation)
            .generatedSourceFile("AnInterfaceWrap")
            .hasSourceEquivalentTo(
                JavaFileObjects.forSourceLines(
                    "AnInterfaceWrap",
                    "public abstract class AnInterfaceWrap implements AnInterface {",
                    "  protected final AnInterface wrapped;",
                    "  public AnInterfaceWrap(AnInterface wrapped) {",
                    "    this.wrapped = wrapped;",
                    "  }",
                    "}"));
    }

    @Test
    public void packagePreserved() {
        final Compilation compilation = Compiler.javac()
            .withProcessors(new Processor())
            .compile(
                JavaFileObjects.forSourceLines(
                    "a.complex.pkg.AnInterface",
                    "package a.complex.pkg;",
                    "import com.github.victornoel.eo.GenerateWrap;",
                    "@GenerateWrap",
                    "public interface AnInterface {}"));
        CompilationSubject.assertThat(compilation).succeededWithoutWarnings();
        CompilationSubject.assertThat(compilation)
            .generatedSourceFile("a.complex.pkg.AnInterfaceWrap")
            .containsElementsIn(
                JavaFileObjects.forSourceLines(
                    "a.complex.package.AnInterfaceWrap",
                    "package a.complex.pkg;",
                    "public abstract class AnInterfaceWrap implements AnInterface {",
                    "}"));
    }

    @Test
    public void innerInterfaceNamePreserved() {
        final Compilation compilation = Compiler.javac()
            .withProcessors(new Processor())
            .compile(
                JavaFileObjects.forSourceLines(
                    "AClass",
                    "package a.pkg;",
                    "import com.github.victornoel.eo.GenerateWrap;",
                    "public class AClass {",
                    "  @GenerateWrap",
                    "  public interface AnInnerInterface {}",
                    "}"));
        CompilationSubject.assertThat(compilation)
            .succeededWithoutWarnings();
        CompilationSubject.assertThat(compilation)
            .generatedSourceFile("a.pkg.AClassAnInnerInterfaceWrap")
            .hasSourceEquivalentTo(
                JavaFileObjects.forSourceLines(
                    "a.pkg.AClassAnInnerInterfaceWrap",
                    "package a.pkg",
                    "public abstract class AClassAnInnerInterfaceWrap implements AClass.AnInnerInterface {",
                    "  protected final AClass.AnInnerInterface wrapped;",
                    "  public AClassAnInnerInterfaceWrap(AClass.AnInnerInterface wrapped) {",
                    "    this.wrapped = wrapped;",
                    "  }",
                    "}"));
    }

    @Test
    public void overridesVoidMethod() {
        final Compilation compilation = Compiler.javac()
            .withProcessors(new Processor())
            .compile(
                JavaFileObjects.forSourceLines(
                    "AnInterface",
                    "import com.github.victornoel.eo.GenerateWrap;",
                    "@GenerateWrap",
                    "public interface AnInterface {",
                    "  void test();",
                    "}"));
        CompilationSubject.assertThat(compilation).succeededWithoutWarnings();
        CompilationSubject.assertThat(compilation)
            .generatedSourceFile("AnInterfaceWrap")
            .hasSourceEquivalentTo(
                JavaFileObjects.forSourceLines(
                    "AnInterfaceWrap",
                    "import java.lang.Override;",
                    "public abstract class AnInterfaceWrap implements AnInterface {",
                    "  protected final AnInterface wrapped;",
                    "  public AnInterfaceWrap(AnInterface wrapped) {",
                    "    this.wrapped = wrapped;",
                    "  }",
                    "  @Override",
                    "  public final void test() {",
                    "    wrapped.test();",
                    "  }",
                    "}"));
    }

    @Test
    public void overridesMethodWithException() {
        final Compilation compilation = Compiler.javac()
            .withProcessors(new Processor())
            .compile(
                JavaFileObjects.forSourceLines(
                    "AnInterface",
                    "import com.github.victornoel.eo.GenerateWrap;",
                    "@GenerateWrap",
                    "public interface AnInterface {",
                    "  void test() throws Exception;",
                    "}"));
        CompilationSubject.assertThat(compilation).succeededWithoutWarnings();
        CompilationSubject.assertThat(compilation)
            .generatedSourceFile("AnInterfaceWrap")
            .hasSourceEquivalentTo(
                JavaFileObjects.forSourceLines(
                    "AnInterfaceWrap",
                    "import java.lang.Exception;",
                    "import java.lang.Override;",
                    "public abstract class AnInterfaceWrap implements AnInterface {",
                    "  protected final AnInterface wrapped;",
                    "  public AnInterfaceWrap(AnInterface wrapped) {",
                    "    this.wrapped = wrapped;",
                    "  }",
                    "  @Override",
                    "  public final void test() throws Exception {",
                    "    wrapped.test();",
                    "  }",
                    "}"));
    }

    @Test
    public void overridesMethodWithReturnType() {
        final Compilation compilation = Compiler.javac()
            .withProcessors(new Processor())
            .compile(
                JavaFileObjects.forSourceLines(
                    "AnInterface",
                    "import com.github.victornoel.eo.GenerateWrap;",
                    "@GenerateWrap",
                    "public interface AnInterface {",
                    "  String test();",
                    "}"));
        CompilationSubject.assertThat(compilation).succeededWithoutWarnings();
        CompilationSubject.assertThat(compilation)
            .generatedSourceFile("AnInterfaceWrap")
            .hasSourceEquivalentTo(
                JavaFileObjects.forSourceLines(
                    "AnInterfaceWrap",
                    "import java.lang.Override;",
                    "import java.lang.String;",
                    "public abstract class AnInterfaceWrap implements AnInterface {",
                    "  protected final AnInterface wrapped;",
                    "  public AnInterfaceWrap(AnInterface wrapped) {",
                    "    this.wrapped = wrapped;",
                    "  }",
                    "  @Override",
                    "  public final String test() {",
                    "    return wrapped.test();",
                    "  }",
                    "}"));
    }

    @Test
    public void overridesMethodWithPrimitiveReturnType() {
        final Compilation compilation = Compiler.javac()
            .withProcessors(new Processor())
            .compile(
                JavaFileObjects.forSourceLines(
                    "AnInterface",
                    "import com.github.victornoel.eo.GenerateWrap;",
                    "@GenerateWrap",
                    "public interface AnInterface {",
                    "  int test();",
                    "}"));
        CompilationSubject.assertThat(compilation).succeededWithoutWarnings();
        CompilationSubject.assertThat(compilation)
            .generatedSourceFile("AnInterfaceWrap")
            .hasSourceEquivalentTo(
                JavaFileObjects.forSourceLines(
                    "AnInterfaceWrap",
                    "import java.lang.Override;",
                    "public abstract class AnInterfaceWrap implements AnInterface {",
                    "  protected final AnInterface wrapped;",
                    "  public AnInterfaceWrap(AnInterface wrapped) {",
                    "    this.wrapped = wrapped;",
                    "  }",
                    "  @Override",
                    "  public final int test() {",
                    "    return wrapped.test();",
                    "  }",
                    "}"));
    }

    @Test
    public void overridesMethodWithParameters() {
        final Compilation compilation = Compiler.javac()
            .withProcessors(new Processor())
            .compile(
                JavaFileObjects.forSourceLines(
                    "AnInterface",
                    "import com.github.victornoel.eo.GenerateWrap;",
                    "@GenerateWrap",
                    "public interface AnInterface {",
                    "  void test(String a, int b);",
                    "}"));
        CompilationSubject.assertThat(compilation).succeededWithoutWarnings();
        CompilationSubject.assertThat(compilation)
            .generatedSourceFile("AnInterfaceWrap")
            .hasSourceEquivalentTo(
                JavaFileObjects.forSourceLines(
                    "AnInterfaceWrap",
                    "import java.lang.Override;",
                    "import java.lang.String;",
                    "public abstract class AnInterfaceWrap implements AnInterface {",
                    "  protected final AnInterface wrapped;",
                    "  public AnInterfaceWrap(AnInterface wrapped) {",
                    "    this.wrapped = wrapped;",
                    "  }",
                    "  @Override",
                    "  public final void test(String a, int b) {",
                    "    wrapped.test(a, b);",
                    "  }",
                    "}"));
    }
}
