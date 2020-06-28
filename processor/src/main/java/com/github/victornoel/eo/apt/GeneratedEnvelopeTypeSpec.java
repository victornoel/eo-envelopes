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
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.NameAllocator;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;
import com.squareup.javapoet.TypeVariableName;
import java.util.stream.Collectors;
import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;

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
    private final Names name;

    /**
     * The allocated names.
     */
    private final Names allocated;

    /**
     * The processing environment.
     */
    private final ProcessingEnvironment procenv;

    /**
     * Ctor.
     *
     * @param source The source interface
     * @param procenv The processing environment
     */
    public GeneratedEnvelopeTypeSpec(
        final TypeElement source,
        final ProcessingEnvironment procenv
    ) {
        this(
            source,
            new GeneratedEnvelopeName(source),
            procenv,
            new AllocatedNames(
                new NameAllocator(),
                source,
                new LocalMethods(source, procenv)
            )
        );
    }

    /**
     * Ctor.
     *
     * @param source The source interface
     * @param name The name for the generated envelope
     * @param procenv The processing environment
     * @param allocated The allocator of names
     * @checkstyle ParameterNumberCheck (10 lines)
     */
    public GeneratedEnvelopeTypeSpec(
        final TypeElement source,
        final Names name,
        final ProcessingEnvironment procenv,
        final Names allocated
    ) {
        this.source = source;
        this.name = name;
        this.procenv = procenv;
        this.allocated = allocated;
    }

    /**
     * Generate the code for the generated envelope.
     *
     * @return The generated code
     * @throws Exception If fails
     */
    public TypeSpec typeSpec() throws Exception {
        final GenerateEnvelope annotation = this.source.getAnnotation(
            GenerateEnvelope.class
        );
        final TypeName spr = TypeName.get(this.source.asType());
        final TypeVariableName type = TypeVariableName.get(
            this.allocated.make(),
            spr
        );
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
        final TypeSpec.Builder builder = TypeSpec.classBuilder(this.name.make())
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
            .addMethods(new DelegatingMethods(field, this.procenv, this.source));
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
}
