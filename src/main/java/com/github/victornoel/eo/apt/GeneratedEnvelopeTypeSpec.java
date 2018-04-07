/**
 * EO-Envelopes
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

package com.github.victornoel.eo.apt;

import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.CodeBlock.Builder;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;
import com.squareup.javapoet.TypeVariableName;
import java.util.stream.Collectors;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeKind;
import javax.lang.model.util.ElementFilter;
import org.cactoos.Scalar;
import org.cactoos.iterable.IterableEnvelope;
import org.cactoos.iterable.Mapped;

public final class GeneratedEnvelopeTypeSpec implements Scalar<TypeSpec> {

    private final TypeElement source;
    private final Scalar<String> name;

    public GeneratedEnvelopeTypeSpec(final TypeElement source) {
        this(source, new GeneratedEnvelopeName(source));
    }

    public GeneratedEnvelopeTypeSpec(final TypeElement source,
        final Scalar<String> name) {
        this.source = source;
        this.name = name;
    }

    @Override
    public TypeSpec value() throws Exception {
        final TypeName type = TypeName.get(this.source.asType());
        final FieldSpec field = FieldSpec
            .builder(type, "wrapped", Modifier.PROTECTED, Modifier.FINAL)
            .build();
        final ParameterSpec parameter = ParameterSpec
            .builder(type, "wrapped")
            .build();
        return TypeSpec.classBuilder(this.name.value())
            .addOriginatingElement(this.source)
            .addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT)
            .addSuperinterface(type)
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
            .addMethods(new DelegatingMethods(this.source, field))
            .build();
    }

    private static final class DelegatingMethods
        extends IterableEnvelope<MethodSpec> {

        DelegatingMethods(final TypeElement element, final FieldSpec wrapped) {
            this(
                ElementFilter.methodsIn(element.getEnclosedElements()),
                wrapped);
        }

        DelegatingMethods(
            final Iterable<ExecutableElement> sources,
            final FieldSpec wrapped) {
            super(() -> new Mapped<>(
                m -> new DelegatingMethod(m, wrapped).value(),
                sources));
        }
    }

    private static final class DelegatingMethod implements Scalar<MethodSpec> {

        private final ExecutableElement method;
        private final FieldSpec wrapped;

        DelegatingMethod(final ExecutableElement method,
            final FieldSpec wrapped) {
            this.method = method;
            this.wrapped = wrapped;
        }

        @Override
        public MethodSpec value() {
            return MethodSpec
                .overriding(this.method)
                .addModifiers(Modifier.FINAL)
                .addStatement(this.delegation())
                .build();
        }

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
