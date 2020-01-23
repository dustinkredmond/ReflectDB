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

import org.gserve.reflectdb.ReflectDB;
import org.gserve.reflectdb.annotations.ReflectDBField;
import org.gserve.reflectdb.annotations.ReflectDBTable;
import org.gserve.reflectdb.exception.ReflectDBException;

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

    public <T> T fetchSingle(String sql, Class<T> modelClass) throws SQLException {
        final String query = sql + " LIMIT 1";
        try (Connection conn = DB.getNativeConnection(); PreparedStatement ps = conn.prepareStatement(query)) {
            ResultSet rs = ps.executeQuery();
            if (rs.getType() == rs.TYPE_FORWARD_ONLY && rs.isBeforeFirst()) {
                // Must advance ResultSet if it's before first row.
                if (rs.isClosed() || !rs.next()) {
                    return null;
                }
            }
            if (rs.isClosed()) {
                return null;
            }
            if (rs.isBeforeFirst()) {
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
        } catch (SQLException e) {
            throw e;
        } catch (NoSuchMethodException e) {
            throw new ReflectDBException(String.format("Model Class: %s must declare a no-argument constructor." +
                    " E.g. public MyClassName() { super(); }", modelClass.getName()));
        } catch (Exception e) {
            throw new UnsupportedOperationException(e);
        }
    }


    public <T> T findById(long id, Class<T> modelClass) throws SQLException {
        String tableName = modelClass.getDeclaredAnnotation(ReflectDBTable.class).tableName();
        String primaryKey = "";
        for (Field f: modelClass.getDeclaredFields()) {
            ReflectDBField rField = f.getAnnotation(ReflectDBField.class);
            if (rField.primaryKey()) {
                primaryKey = rField.fieldName();
            }
        }
        if (primaryKey.isEmpty() || primaryKey.trim().isEmpty()) {
            throw new ReflectDBException("Table must specify a primary key.");
        }
        final String sql = "SELECT * FROM " + tableName + " WHERE " + primaryKey + " = " + id;
        try (Connection conn = DB.getNativeConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ResultSet rs = ps.executeQuery();
            if (rs.isClosed() || rs.isBeforeFirst()){
                if (!rs.next()) {
                    return null;
                }
            }
            T obj = modelClass.getConstructor().newInstance();
            for (Map.Entry<String, String> entry : MAPPING.getFieldColumnMap(modelClass).entrySet()) {
                Field f = obj.getClass().getDeclaredField(entry.getKey());
                f.setAccessible(true);
                f.set(obj, rs.getObject(entry.getValue()));
            }
            return obj;
        } catch (SQLException e) {
            if (!"Current position is before the first row".equals(e.getMessage())) {
                throw e;
            }
            return null;
        } catch (NoSuchMethodException e) {
            throw new ReflectDBException(String.format("Model Class: %s must declare a no-argument constructor." +
                    " E.g. public MyClassName() { super(); }", modelClass.getName()));
        } catch (Exception e) {
            throw new UnsupportedOperationException(e);
        }
    }

    public <T> List<T> fetch(String sql, Class<T> modelClass) throws SQLException {
        List<T> objList = new ArrayList<>();
        try (Connection conn = DB.getNativeConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                T obj = modelClass.getConstructor().newInstance();
                for (Map.Entry<String, String> entry : MAPPING.getFieldColumnMap(modelClass).entrySet()) {
                    Field f = obj.getClass().getDeclaredField(entry.getKey());
                    f.setAccessible(true);
                    f.set(obj, rs.getObject(entry.getValue()));
                }
                objList.add(obj);
            }
            return objList;
        } catch (SQLException e) {
            throw e;
        } catch (NoSuchMethodException e) {
            throw new ReflectDBException(String.format("Model class: %s must declare a no-argument constructor." +
                    "E.g. public MyClassName() { super(); }", modelClass.getName()));
        } catch (Exception e) {
            throw new UnsupportedOperationException(e);
        }
    }

    /**
     * Insert and save a new object in the database.
     * @param obj Object to save in the database. (class must be annotated)
     * @return True if and only if the object was inserted.
     * @throws SQLException if the INSERT SQL causes an exception.
     */
    public boolean insert(Object obj) throws SQLException {

        StringBuilder query = new StringBuilder("INSERT INTO ");
        query.append(obj.getClass().getAnnotation(ReflectDBTable.class).tableName())
                .append(" (");
        StringJoiner columnNames = new StringJoiner(", ");
        StringJoiner values = new StringJoiner(", ");
        for (Map.Entry<String,String> entry: MAPPING.getFieldColumnMap(obj.getClass()).entrySet()) {
            try {
                Field f = obj.getClass().getDeclaredField(entry.getKey());
                f.setAccessible(true);
                // If user defines a primary key in their object, then let it persist
                // otherwise don't default to default value of 0
                if (!isPrimaryKey(f) || isPrimaryKey(f) && f.getDouble(obj) > 0) {
                    columnNames.add(entry.getValue());
                    if (isNumericType(f)) {
                        values.add(f.get(obj).toString());
                    } else if (BOOLEAN.equalsIgnoreCase(f.getAnnotation(ReflectDBField.class).fieldType())) {
                        boolean insert = f.getBoolean(obj);
                        values.add(String.valueOf(insert ? 1:0));
                    } else {
                        if (f.get(obj) != null) {
                            values.add("\"" + f.get(obj).toString() + "\"");
                        } else {
                            values.add("''");
                        }
                    }
                }
            } catch (NoSuchFieldException | IllegalAccessException e) {
                throw new ReflectDBException(e);
            }
        }
        query.append(columnNames.toString()).append(") VALUES (").append(values.toString()).append(");");
        final String sql = query.toString();

        try (Connection conn = DB.getNativeConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            return ps.executeUpdate() > 0;
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
            if (isNumericType(f)) {
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
                if (!isNumericType(field)) {
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
            try (Connection conn = DB.getNativeConnection(); PreparedStatement pstmt = conn.prepareStatement(query.toString())) {
                return pstmt.executeUpdate() > 0;
            }
        }
    }

    /**
     * Checks whether or not a given field type is a numeric
     * SQL data type.
     * @param f Database field, derived from {@code ReflectDBField}
     * @return True if the supplied datatype is a numeric type.
     */
    private boolean isNumericType(Field f) {
        boolean isNumeric = false;
        ReflectDBField dbField = f.getAnnotation(ReflectDBField.class);
        if (dbField != null) {
            for (String s : NUM_TYPE) {
                if (dbField.fieldType().startsWith(s)) {
                    isNumeric = true;
                    break;
                }
            }
        }
        return isNumeric;
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
            try (Connection conn = DB.getNativeConnection(); PreparedStatement pstmt = conn.prepareStatement(sb.toString())) {
                return pstmt.executeUpdate() > 0;
            } catch (Exception e) {
                throw new ReflectDBException(String.format("%s.delete yielded SQL: %s, which raised an exception.",
                        getClass().getName(), sb.toString()), e);
            }
        } else {
            throw new ReflectDBException("Attempted to call ReflectDB.delete on a class with" +
                    "no clearly defined primary key.");
        }
    }

    public <T> List<T> fetchAll(Class<T> modelClass) throws SQLException {
        String tableName = modelClass.getAnnotation(ReflectDBTable.class).tableName();
        if (tableName.isEmpty()) {
            throw new ReflectDBException("Cannot fetch objects for a class without a table name.");
        }
        tableName = "SELECT * FROM " + tableName;
        return fetch(tableName, modelClass);
    }

    public <T> List<T> fetchAll(Class<T> modelClass, int limit) throws SQLException {
        String tableName = modelClass.getAnnotation(ReflectDBTable.class).tableName();
        if (tableName.isEmpty()) {
            throw new ReflectDBException("Cannot fetch objects for a class without a table name.");
        }
        tableName = "SELECT * FROM " + tableName + " LIMIT " + limit;
        return fetch(tableName, modelClass);
    }

    private boolean isPrimaryKey(Field f) {
        ReflectDBField dbField = f.getAnnotation(ReflectDBField.class);
        return dbField != null && dbField.primaryKey();
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
    private static final String BOOLEAN = "BOOLEAN";
}
