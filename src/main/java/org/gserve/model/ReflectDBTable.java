package org.gserve.model;
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
import org.reflections.Reflections;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

/**
 * Data structure used internally by ReflectDB to represent a database table.
 * @since 12/24/2019 08:14
 * @author Dustin K. Redmond
 */
public class ReflectDBTable {
    private String tableName;
    private List<ReflectDBColumn> columns;

    private ReflectDBTable(String tableName, List<ReflectDBColumn> columns) {
        this.tableName = tableName;
        this.columns = columns;
    }

    public String getTableName() { return this.tableName; }
    public List<ReflectDBColumn> getColumns() { return this.columns; }

    public static List<ReflectDBTable> findAll() {
        List<ReflectDBTable> tables = new ArrayList<>();

        // You found it right here, ReflectDB's only use of the org.reflections:reflections dependency
        Reflections ref = new Reflections(ReflectDB.getInstance().getConfig().getModelPackage());
        for (Class<?> cl : ref.getTypesAnnotatedWith(org.gserve.annotations.ReflectDBTable.class)) {
            org.gserve.annotations.ReflectDBTable reflectDBTable = cl.getAnnotation(org.gserve.annotations.ReflectDBTable.class);
            tables.add(new ReflectDBTable(reflectDBTable.tableName(), getColumnsFromTableBean(cl.getDeclaredFields())));
        }
        return tables;
    }

    private static List<ReflectDBColumn> getColumnsFromTableBean(Field[] fields) {
        List<ReflectDBColumn> columns = new ArrayList<>();
        for (Field field : fields) {
            for (ReflectDBField reflectDBField : field.getDeclaredAnnotationsByType(ReflectDBField.class)) {
                columns.add(new ReflectDBColumn(reflectDBField.fieldName(),
                        reflectDBField.fieldType(),
                        reflectDBField.notNull(),
                        reflectDBField.primaryKey()));
            }
        }
        return columns;
    }
}
