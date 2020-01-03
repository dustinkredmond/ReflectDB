package org.gserve.reflectdb.model;
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

/**
 * Data structure used internally by ReflectDB to represent a database column.
 * @since  12/24/2019 08:41
 * @author Dustin K. Redmond
 */
@SuppressWarnings("unused")
public class ReflectDBColumn {

    private String columnName;
    private String columnType;
    private boolean notNull;
    private boolean primaryKey;

    public ReflectDBColumn() { super(); }
    ReflectDBColumn(String columnName, String columnType, boolean notNull, boolean primaryKey) {
        this.columnName = columnName;
        this.columnType = columnType;
        this.notNull = notNull;
        this.primaryKey = primaryKey;
    }

    public String getColumnName() { return this.columnName; }
    public void setColumnName(String columnName) { this.columnName = columnName; }

    public String getColumnType() { return this.columnType; }
    public void setColumnType(String columnType) { this.columnType = columnType; }

    public boolean getNotNull() { return this.notNull; }
    public void setNotNull(boolean notNull) { this.notNull = notNull; }

    public boolean isPrimaryKey() { return this.primaryKey; }
    public void setPrimaryKey(boolean isPrimaryKey) { this.primaryKey = isPrimaryKey; }
}
