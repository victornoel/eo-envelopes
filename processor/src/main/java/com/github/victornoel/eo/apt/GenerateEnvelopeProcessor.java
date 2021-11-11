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

import com.github.victornoel.eo.GenerateEnvelope;
import com.google.auto.service.AutoService;
import com.squareup.javapoet.JavaFile;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Collections;
import java.util.Set;
import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic;
import javax.tools.Diagnostic.Kind;

/**
 * A processor to generate envelopes from {@link GenerateEnvelope}.
 *
 * @since 1.0.0
 */
@AutoService(javax.annotation.processing.Processor.class)
public final class GenerateEnvelopeProcessor extends AbstractProcessor {

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        return Collections.singleton(GenerateEnvelope.class.getName());
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latestSupported();
    }

    @Override
    public boolean process(
        final Set<? extends TypeElement> annotations,
        final RoundEnvironment env
    ) {
        env.getElementsAnnotatedWith(GenerateEnvelope.class)
            .forEach(this::process);
        return true;
    }

    /**
     * Process one element annotated with {@link GenerateEnvelope}.
     *
     * @param element The annotated element
     */
    @SuppressWarnings("PMD.AvoidCatchingGenericException")
    private void process(final Element element) {
        if (element.getKind() == ElementKind.INTERFACE) {
            try {
                this.generate((TypeElement) element);
                // @checkstyle IllegalCatchCheck (1 line)
            } catch (final Exception exception) {
                final StringWriter writer = new StringWriter();
                exception.printStackTrace(new PrintWriter(writer));
                processingEnv.getMessager().printMessage(
                    Kind.ERROR,
                    String.format("FATAL ERROR: %s", writer),
                    element
                );
            }
        } else {
            this.processingEnv.getMessager().printMessage(
                Diagnostic.Kind.ERROR,
                "@GenerateEnvelope is only for interfaces",
                element
            );
        }
    }

    /**
     * Generate an envelope for an interface annotated with
     * {@link GenerateEnvelope}.
     *
     * @param itf The annotated interface
     * @throws Exception If fails
     */
    private void generate(final TypeElement itf) throws Exception {
        final JavaFile file = JavaFile
            .builder(
                this.processingEnv.getElementUtils()
                    .getPackageOf(itf)
                    .getQualifiedName()
                    .toString(),
                new GeneratedEnvelopeTypeSpec(
                    itf,
                    this.processingEnv
                ).typeSpec()
            )
            .build();
        // @checkstyle MethodBodyCommentsCheck (1 line)
        // mitigation for https://bugs.eclipse.org/bugs/show_bug.cgi?id=367599
        file.toJavaFileObject().delete();
        file.writeTo(this.processingEnv.getFiler());
    }
}
