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
import com.google.auto.common.GeneratedAnnotationSpecs;
import com.google.auto.common.MoreElements;
import com.google.auto.common.MoreTypes;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.CodeBlock.Builder;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;
import com.squareup.javapoet.TypeVariableName;
import java.util.Collection;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeKind;

/**
 * The generated code of a generated envelope.
 *
 * @since 1.0.0
 */
public final class GeneratedEnvelopeTypeSpec {

    /**
     * The source interface.
     */
    private final TypeElement source;

    /**
     * The name for the generated envelope.
     */
    private final String name;

    /**
     * The processing environment.
     */
    private ProcessingEnvironment procenv;

    /**
     * Ctor.
     *
     * @param source The source interface
     * @param procenv The processing environment
     */
    public GeneratedEnvelopeTypeSpec(final TypeElement source,
        final ProcessingEnvironment procenv) {
        this(source, new GeneratedEnvelopeName(source).get(), procenv);
    }

    /**
     * Ctor.
     *
     * @param source The source interface
     * @param name The name for the generated envelope
     * @param procenv The processing environment
     */
    public GeneratedEnvelopeTypeSpec(final TypeElement source,
        final String name, final ProcessingEnvironment procenv) {
        this.source = source;
        this.name = name;
        this.procenv = procenv;
    }

    /**
     * Generate the code for the generated envelope.
     *
     * @return The generated code
     * @throws Exception If fails
     */
    public TypeSpec typeSpec() throws Exception {
        final GenerateEnvelope annotation = this.source.getAnnotation(GenerateEnvelope.class);
        final TypeName spr = TypeName.get(this.source.asType());
        final TypeVariableName type = TypeVariableName.get("W", spr);
        final TypeName prm;
        if (annotation.generic()) {
            prm = type;
        } else {
            prm = spr;
        }
        final String wrapped = "wrapped";
        final FieldSpec field = FieldSpec
            .builder(prm, wrapped, Modifier.PROTECTED, Modifier.FINAL)
            .build();
        final ParameterSpec parameter = ParameterSpec
            .builder(prm, wrapped)
            .build();
        final TypeSpec.Builder builder = TypeSpec.classBuilder(this.name)
            .addOriginatingElement(this.source)
            .addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT)
            .addSuperinterface(spr)
            .addTypeVariables(
                this.source.getTypeParameters()
                    .stream()
                    .map(TypeVariableName::get)
                    .collect(Collectors.toList())
            )
            .addField(field)
            .addMethod(MethodSpec.constructorBuilder()
                .addModifiers(Modifier.PUBLIC)
                .addParameter(parameter)
                .addStatement("this.$N = $N", field, parameter)
                .build()
            )
            .addMethods(new DelegatingMethods(field).get());
        if (annotation.generic()) {
            builder.addTypeVariable(type);
        }
        GeneratedAnnotationSpecs.generatedAnnotationSpec(
            this.procenv.getElementUtils(),
            this.procenv.getSourceVersion(),
            GenerateEnvelopeProcessor.class
        ).ifPresent(builder::addAnnotation);
        return builder.build();
    }

    /**
     * Generated delegating methods.
     *
     * @since 1.0.0
     */
    private final class DelegatingMethods
        implements Supplier<Iterable<MethodSpec>> {

        /**
         * The methods to delegate.
         */
        private final Collection<ExecutableElement> sources;

        /**
         * The field to delegate to.
         */
        private final FieldSpec wrapped;

        /**
         * Ctor.
         *
         * @param wrapped The field to delegate to
         */
        DelegatingMethods(final FieldSpec wrapped) {
            this(
                MoreElements.getLocalAndInheritedMethods(
                    GeneratedEnvelopeTypeSpec.this.source,
                    GeneratedEnvelopeTypeSpec.this.procenv.getTypeUtils(),
                    GeneratedEnvelopeTypeSpec.this.procenv.getElementUtils()
                ),
                wrapped
            );
        }

        /**
         * Ctor.
         *
         * @param sources The methods to delegate
         * @param wrapped The field to delegate to
         */
        DelegatingMethods(final Collection<ExecutableElement> sources,
            final FieldSpec wrapped) {
            this.sources = sources;
            this.wrapped = wrapped;
        }

        @Override
        public Iterable<MethodSpec> get() {
            return this.sources.stream()
                .map(m -> new DelegatingMethod(m, this.wrapped).get())
                .collect(Collectors.toList());
        }
    }

    /**
     * One generated delegating method.
     *
     * @since 1.0.0
     */
    private final class DelegatingMethod implements Supplier<MethodSpec> {

        /**
         * The method to delegate.
         */
        private final ExecutableElement method;

        /**
         * The field to delegate to.
         */
        private final FieldSpec wrapped;

        /**
         * Ctor.
         *
         * @param method The method to delegate
         * @param wrapped The field to delegate to
         */
        DelegatingMethod(final ExecutableElement method,
            final FieldSpec wrapped) {
            this.method = method;
            this.wrapped = wrapped;
        }

        @Override
        public MethodSpec get() {
            return MethodSpec
                .overriding(
                    this.method,
                    MoreTypes.asDeclared(
                        GeneratedEnvelopeTypeSpec.this.source.asType()
                    ),
                    GeneratedEnvelopeTypeSpec.this.procenv.getTypeUtils()
                )
                .addModifiers(Modifier.FINAL)
                .addStatement(this.delegation())
                .build();
        }

        /**
         * The actual delegation to the field.
         *
         * @return The delegation code
         */
        private CodeBlock delegation() {
            Builder statement = CodeBlock.builder();
            if (this.method.getReturnType().getKind() != TypeKind.VOID) {
                statement = statement.add("return ");
            }
            return statement
                .add("$N.$N", this.wrapped, this.method.getSimpleName())
                .add("(")
                .add(this.method
                    .getParameters()
                    .stream()
                    .map(ps -> CodeBlock.of("$N", ParameterSpec.get(ps)))
                    .collect(CodeBlock.joining(","))
                )
                .add(")")
                .build();
        }
    }
}
