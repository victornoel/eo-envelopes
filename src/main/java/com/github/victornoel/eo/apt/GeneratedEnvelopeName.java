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

import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
import org.cactoos.Scalar;

public final class GeneratedEnvelopeName implements Scalar<String> {

    private final TypeElement source;
    private final String suffix;

    public GeneratedEnvelopeName(final TypeElement source) {
        this(source, "Envelope");
    }

    public GeneratedEnvelopeName(final TypeElement source,
        final String suffix) {
        this.source = source;
        this.suffix = suffix;
    }

    @Override
    public String value() {
        String name = this.source.getSimpleName() + this.suffix;
        Element parent = this.source.getEnclosingElement();
        while (parent.getKind() != ElementKind.PACKAGE) {
            name = parent.getSimpleName() + name;
            parent = parent.getEnclosingElement();
        }
        return name;
    }
}
