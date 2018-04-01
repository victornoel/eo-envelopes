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

import com.github.victornoel.eo.GenerateWrap;
import com.google.auto.service.AutoService;
import com.squareup.javapoet.JavaFile;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Collections;
import java.util.Set;
import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic;
import javax.tools.Diagnostic.Kind;

@SupportedSourceVersion(SourceVersion.RELEASE_8)
@AutoService(javax.annotation.processing.Processor.class)
public final class Processor extends AbstractProcessor {

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        return Collections.singleton(GenerateWrap.class.getName());
    }

    @Override
    public boolean process(final Set<? extends TypeElement> annotations,
        final RoundEnvironment env) {
        env.getElementsAnnotatedWith(GenerateWrap.class).forEach(this::process);
        return true;
    }

    private void process(final Element element) {
        if (element.getKind() != ElementKind.INTERFACE) {
            this.processingEnv.getMessager().printMessage(
                Diagnostic.Kind.ERROR,
                "@GenerateWrap is only for interfaces",
                element);
        } else {
            try {
                this.process((TypeElement) element);
                // @checkstyle IllegalCatchCheck (1 line)
            } catch (final Exception exception) {
                final StringWriter writer = new StringWriter();
                exception.printStackTrace(new PrintWriter(writer));
                processingEnv.getMessager().printMessage(
                    Kind.ERROR,
                    String.format("FATAL ERROR: %s", writer),
                    element);
            }
        }
    }

    private void process(final TypeElement itf) throws Exception {
        JavaFile
            .builder(
                this.processingEnv.getElementUtils()
                    .getPackageOf(itf)
                    .getQualifiedName()
                    .toString(),
                new GeneratedTypeSpec(itf).value())
            .build()
            .writeTo(this.processingEnv.getFiler());
    }
}
