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

import com.squareup.javapoet.NameAllocator;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.TypeParameterElement;

/**
 * Allocate new names.
 *
 * @since 1.0.1
 */
final class AllocatedNames implements Names {
    /**
     * The name allocator.
     */
    private final NameAllocator allocator;

    /**
     * Ctor.
     * @param allocator The allocator
     * @param source The source element
     * @param methods Local methods
     */
    @SuppressWarnings("PMD.ConstructorOnlyInitializesOrCallOtherConstructors")
    AllocatedNames(
        final NameAllocator allocator,
        final TypeElement source,
        final Iterable<ExecutableElement> methods
    ) {
        this.allocator = allocator;
        for (final TypeParameterElement type : source.getTypeParameters()) {
            this.allocator.newName(type.getSimpleName().toString());
        }
        for (final ExecutableElement executable : methods) {
            for (final TypeParameterElement type : executable.getTypeParameters()) {
                this.allocator.newName(type.getSimpleName().toString());
            }
        }
    }

    @Override
    public String make() {
        return this.make("W");
    }

    @Override
    public String make(final String preferred) {
        return this.allocator.newName(preferred);
    }

}
