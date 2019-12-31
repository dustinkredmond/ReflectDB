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

import org.gserve.ReflectDB;
import org.gserve.annotations.ReflectDBField;
import org.gserve.annotations.ReflectDBTable;
import org.gserve.exception.ReflectDBException;

import java.lang.reflect.Field;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;

/**
 * Class for handling generic SQL Queries.
 * @since 12/24/2019 11:55
 * @author Dustin K. Redmond
 */
public class ReflectDBQuery {

    public <T> T fetchSingle(String sql, Class<T> modelClass) {
        try (Connection conn = DB.getNativeConnection(); PreparedStatement ps = conn.prepareStatement(sql+=" LIMIT 1")) {
            ResultSet rs = ps.executeQuery();
            if (rs.getType() == rs.TYPE_FORWARD_ONLY && rs.isBeforeFirst()) {
                // Must advance ResultSet if it's before first row.
                rs.next();
            }
            T obj = modelClass.getConstructor().newInstance();
            for (Map.Entry<String, String> entry : MAPPING.getFieldColumnMap(modelClass).entrySet()) {
                String fieldName = entry.getKey();
                String dbColumn = entry.getValue();
                Field f = obj.getClass().getDeclaredField(fieldName);
                f.setAccessible(true);
                f.set(obj, rs.getObject(dbColumn));
            }
            return obj;
        } catch (Exception e) {
            throw new ReflectDBException(String.format("Exception occurred in query: %s", sql), e);
        }
    }

    public <T> List<T> fetch(String sql, Class<T> modelClass) {
        List<T> objList = new ArrayList<>();
        try (Connection conn = DB.getNativeConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                T obj = modelClass.getConstructor().newInstance();
                for (Map.Entry<String,String> entry: MAPPING.getFieldColumnMap(modelClass).entrySet()) {
                    Field f = obj.getClass().getDeclaredField(entry.getKey());
                    f.setAccessible(true);
                    f.set(obj, rs.getObject(entry.getValue()));
                }
                objList.add(obj);
            }
            return objList;
        } catch (Exception e) {
            throw new ReflectDBException(String.format("Exception occurred in query: %s", sql), e);
        }
    }

    /**
     * Insert and save a new object in the database.
     * @param obj Object to save in the database (class must be annotated)
     * @return True if and only if the object was inserted.
     */
    public boolean insert(Object obj) {

        StringBuilder query = new StringBuilder("INSERT INTO ");
        query.append(obj.getClass().getAnnotation(ReflectDBTable.class).tableName())
                .append(" (");
        StringJoiner columnNames = new StringJoiner(", ");
        StringJoiner values = new StringJoiner(", ");
        for (Map.Entry<String,String> entry: MAPPING.getFieldColumnMap(obj.getClass()).entrySet()) {
            columnNames.add(entry.getValue());

            try {
                Field f = obj.getClass().getDeclaredField(entry.getKey());
                f.setAccessible(true);
                if (NUM_TYPE.contains(f.getAnnotation(ReflectDBField.class).fieldType())) {
                    values.add(f.get(obj).toString());
                } else {
                    values.add("\"" + f.get(obj).toString() + "\"");
                }
            } catch (NoSuchFieldException | IllegalAccessException e) {
                throw new ReflectDBException("Exception occurred in reflectively assigning field values.", e);
            }
        }
        query.append(columnNames.toString()).append(") VALUES (").append(values.toString()).append(");");
        final String sql = query.toString();

        try (Connection conn = DB.getNativeConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new ReflectDBException(String.format("Malformed SQL: %s", sql), e);
        }
    }

    /**
     * Saves the object in the database (performs SQL Update)
     * @param obj Object to update in the database.
     * @return True if the object was updated.
     * @throws Exception if Exception is thrown due to reflection or SQL
     */
    public boolean save(Object obj) throws Exception {
        StringBuilder query = new StringBuilder("UPDATE ");
        query.append(obj.getClass().getAnnotation(ReflectDBTable.class).tableName())
                .append(" SET ");
        StringJoiner values = new StringJoiner(", ");
        for (Map.Entry<String,String> entry: MAPPING.getFieldColumnMap(obj.getClass()).entrySet()) {
            Field f = obj.getClass().getDeclaredField(entry.getKey());
            f.setAccessible(true);
            if (NUM_TYPE.contains(f.getAnnotation(ReflectDBField.class).fieldType())) {
                values.add(entry.getValue() + " = " + f.get(obj).toString());
            } else {
                values.add(entry.getValue() + " = \"" + f.get(obj).toString() + "\"");
            }
        }

        query.append(values);
        int numPrimaryKey = 0;
        for (Field field : obj.getClass().getDeclaredFields()) {
            if (field.getAnnotation(ReflectDBField.class).primaryKey()) {
                numPrimaryKey++;
                if (!NUM_TYPE.contains(field.getAnnotation(ReflectDBField.class).fieldType())) {
                    throw new ReflectDBException(String.format("Field: %s declared as primary key," +
                            " but field is not a numeric type. ReflectDB requires a numeric type.", field.getName()),
                            new Exception());
                } else {
                    field.setAccessible(true);
                    query.append(" WHERE ").append(field.getAnnotation(ReflectDBField.class).fieldName())
                            .append(" = ").append(field.get(obj));
                }
            }
        }
        if (numPrimaryKey != 1) {
            String tableName = obj.getClass().getAnnotation(ReflectDBTable.class).tableName();
            throw new ReflectDBException(
                    String.format("ReflectDB requires one primary key per table, found: %d keys for table: %s",
                            numPrimaryKey, tableName));
        } else {
            return DB.getNativeConnection().prepareStatement(query.toString()).executeUpdate() > 0;
        }
    }

    public boolean delete(Object obj) {
        StringBuilder sb = new StringBuilder("DELETE FROM ");
        sb.append(obj.getClass().getAnnotation(ReflectDBTable.class).tableName());
        sb.append(" WHERE ");
        int numPrimaryKey = 0;
        for (Field field: obj.getClass().getDeclaredFields()) {
            if (field.getAnnotation(ReflectDBField.class).primaryKey()) {
                numPrimaryKey++;
                field.setAccessible(true);
                sb.append(field.getAnnotation(ReflectDBField.class).fieldName());
                try {
                    sb.append(" = ").append(field.get(obj));
                } catch (IllegalAccessException e) {
                    throw new ReflectDBException(e);
                }
            }
        }
        if (numPrimaryKey == 1) {
            try {
                return DB.getNativeConnection().prepareStatement(sb.toString()).executeUpdate() > 0;
            } catch (Exception e) {
                throw new ReflectDBException(String.format("%s.delete yielded SQL: %s, which raised an exception.",
                        getClass().getName(), sb.toString()), e);
            }
        } else {
            throw new ReflectDBException("Attempted to call ReflectDB.delete on a class with" +
                    "no defined primary key.");
        }
    }

    public <T> List<T> fetchAll(Class<T> modelClass) {
        String tableName = modelClass.getAnnotation(ReflectDBTable.class).tableName();
        if (tableName.isEmpty()) {
            throw new ReflectDBException("Cannot fetch objects for a class without a table name.");
        }
        tableName = "SELECT * FROM " + tableName;
        return fetch(tableName, modelClass);
    }

    private static final ReflectDB DB = ReflectDB.getInstance();
    private static final QueryMapping MAPPING = new QueryMapping();
    private static List<String> NUM_TYPE = List.of(
            "SMALLINT",
            "INTEGER",
            "BIGINT",
            "NUMERIC",
            "DECIMAL",
            "FLOAT",
            "REAL",
            "DOUBLE PRECISION",
            "DOUBLE",
            "NUMBER",
            "INT",
            "TINYINT");
}
