package org.gserve.query;
/*
 *  Copyright (C) 2019 Dustin K. Redmond
 *
 * This program is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by the Free
 * Software Foundation; either version 2 of the License, or (at your option)
 * any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for
 * more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc., 59
 * Temple Place, Suite 330, Boston, MA 02111-1307 USA
 *
 */

import org.gserve.annotations.ReflectDBField;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

/**
 * @since 12/24/2019 12:37
 * @author Dustin K. Redmond
 */
class QueryMapping {
    <T> Map<String, String> getFieldColumnMap(Class<T> cl) {
        HashMap<String, String> map = new HashMap<>();
        for (Field declaredField : cl.getDeclaredFields()) {
            map.put(declaredField.getName(), declaredField.getAnnotation(ReflectDBField.class).fieldName());
        }
        return map;
    }
}
