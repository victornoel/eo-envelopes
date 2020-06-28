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

/**
 * Produce names.
 *
 * @since 1.0.1
 */
public interface Names {
    /**
     * Make a new name.
     * @return A new unique name
     */
    String make();

    /**
     * Make a new name, with a preference.
     * @param preferred A preferred name
     * @return A preferred name if not occupied, or new unique name
     */
    String make(String preferred);
}
