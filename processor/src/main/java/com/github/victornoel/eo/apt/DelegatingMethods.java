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

import com.google.auto.common.MoreTypes;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import java.util.Iterator;
import java.util.function.Function;
import java.util.stream.StreamSupport;
import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeKind;

/**
 * Generated delegating methods.
 *
 * @since 1.0.0
 */
final class DelegatingMethods implements Iterable<MethodSpec> {

    /**
     * The local methods.
     */
    private final Iterable<ExecutableElement> sources;

    /**
     * The delegating method generator.
     */
    private final Function<ExecutableElement, MethodSpec> method;

    /**
     * Ctor.
     *
     * @param wrapped The field to delegate to
     * @param prcnv The processing environment
     * @param src The source element
     */
    DelegatingMethods(
        final FieldSpec wrapped, final ProcessingEnvironment prcnv,
        final TypeElement src
    ) {
        this(
            new LocalMethods(src, prcnv),
            new DelegatingMethod(wrapped, src, prcnv)
        );
    }

    /**
     * Ctor.
     * @param sources The methods to delegate
     * @param mthd The delegating method
     */
    DelegatingMethods(
        final Iterable<ExecutableElement> sources,
        final Function<ExecutableElement, MethodSpec> mthd
    ) {
        this.sources = sources;
        this.method = mthd;
    }

    @Override
    public Iterator<MethodSpec> iterator() {
        return StreamSupport.stream(this.sources.spliterator(), false)
            .map(this.method)
            .iterator();
    }

    /**
     * One generated delegating method.
     *
     * @since 1.0.0
     */
    static final class DelegatingMethod implements
        Function<ExecutableElement, MethodSpec> {

        /**
         * The field to delegate to.
         */
        private final FieldSpec wrapped;

        /**
         * The source type.
         */
        private final TypeElement source;

        /**
         * The environment.
         */
        private final ProcessingEnvironment procenv;

        /**
         * Ctor.
         *
         * @param wrapped The field to delegate to
         * @param source The source type
         * @param procenv The processing environment
         */
        DelegatingMethod(
            final FieldSpec wrapped,
            final TypeElement source,
            final ProcessingEnvironment procenv
        ) {
            this.wrapped = wrapped;
            this.source = source;
            this.procenv = procenv;
        }

        @Override
        public MethodSpec apply(final ExecutableElement method) {
            return MethodSpec
                .overriding(
                    method,
                    MoreTypes.asDeclared(
                        this.source.asType()
                    ),
                    this.procenv.getTypeUtils()
                )
                .addModifiers(Modifier.FINAL)
                .addStatement(this.delegation(method))
                .build();
        }

        /**
         * The actual delegation to the field.
         *
         * @param method A method to delegate.
         * @return The delegation code
         */
        private CodeBlock delegation(final ExecutableElement method) {
            CodeBlock.Builder statement = CodeBlock.builder();
            if (TypeKind.VOID != method.getReturnType().getKind()) {
                statement = statement.add("return ");
            }
            return statement
                .add("$N.$N", this.wrapped, method.getSimpleName())
                .add("(")
                .add(method
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
