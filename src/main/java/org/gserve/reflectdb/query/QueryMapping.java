package org.gserve.reflectdb.query;
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

import org.gserve.reflectdb.annotations.ReflectDBField;

import java.io.InputStream;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.net.URL;
import java.sql.*;
import java.util.HashMap;
import java.util.Map;

/**
 * @since 12/24/2019 12:37
 * @author Dustin K. Redmond
 */
@SuppressWarnings("unused")
class QueryMapping {

    <T> Map<String, String> getFieldColumnMap(Class<T> cl) {
        HashMap<String, String> map = new HashMap<>();
        for (Field declaredField : cl.getDeclaredFields()) {
            map.put(declaredField.getName(), declaredField.getAnnotation(ReflectDBField.class).fieldName());
        }
        return map;
    }

    /**
     * Takes in an object, calls the appropriate {@code PreparedStatement} setter
     * based on the objects type.
     * @param data Object to set in {@code PreparedStatement}
     * @param ps {@code PreparedStatement} object
     * @param i {@code} Index to set.
     * @throws SQLException If the parameter index is incorrect.
     */
    public void mapObjectToPreparedStatement(Object data, PreparedStatement ps, int i) throws SQLException, ClassCastException {
        if (data instanceof String) {
            ps.setString(i, String.valueOf(data));
        } else if (data instanceof Long) {
            ps.setLong(i, (long) data);
        } else if (data instanceof Integer) {
            ps.setInt(i, (int) data);
        } else if (data instanceof Double) {
            ps.setDouble(i, (double) data);
        } else if (data instanceof java.sql.Date) {
            ps.setDate(i, (java.sql.Date) data);
        } else if (data instanceof Boolean) {
            ps.setBoolean(i, (boolean) data);
        } else if (data instanceof BigDecimal) {
            ps.setBigDecimal(i, (BigDecimal) data);
        } else if (data instanceof InputStream) {
            ps.setBinaryStream(i, (InputStream) data);
        } else if (data instanceof Array) {
            ps.setArray(i, (Array) data);
        } else if (data instanceof Blob) {
            ps.setBlob(i, (Blob) data);
        } else if (data instanceof Byte) {
            ps.setByte(i, (byte) data);
        } else if (data instanceof byte[]) {
            ps.setBytes(i, (byte[]) data);
        } else if (data instanceof Float) {
            ps.setFloat(i, (float) data);
        } else if (data instanceof Time) {
            ps.setTime(i, (Time) data);
        } else if (data instanceof Timestamp) {
            ps.setTimestamp(i, (Timestamp) data);
        } else if (data instanceof Clob) {
            ps.setClob(i, (Clob) data);
        } else if (data instanceof URL) {
            ps.setURL(i, (URL) data);
        } else {
            ps.setObject(i, data);
        }
    }
}
