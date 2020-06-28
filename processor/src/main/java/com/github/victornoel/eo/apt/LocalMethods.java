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

import com.google.auto.common.MoreElements;
import java.util.Iterator;
import java.util.Set;
import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;

/**
 * Local and inherited methods of the type.
 *
 * @since 1.0.1
 */
final class LocalMethods implements Iterable<ExecutableElement> {
    /**
     * The source interface.
     */
    private final TypeElement source;

    /**
     * The processing environment.
     */
    private final ProcessingEnvironment procenv;

    /**
     * Ctor.
     *
     * @param source The source type.
     * @param procenv The environment.
     */
    LocalMethods(
        final TypeElement source,
        final ProcessingEnvironment procenv
    ) {
        this.source = source;
        this.procenv = procenv;
    }

    @Override
    public Iterator<ExecutableElement> iterator() {
        return this.localAndInherited().iterator();
    }

    /**
     * Local and inherited methods.
     *
     * @return Set of methods.
     */
    private Set<ExecutableElement> localAndInherited() {
        return MoreElements.getLocalAndInheritedMethods(
            this.source,
            this.procenv.getTypeUtils(),
            this.procenv.getElementUtils()
        );
    }
}
