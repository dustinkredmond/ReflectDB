package org.gserve;
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
import org.gserve.annotations.ReflectDBTable;

/**
 * Dummy table to use when testing.
 * Created: 12/24/2019 08:22
 * Author: Dustin K. Redmond
 */
@ReflectDBTable(tableName = "TEST_TABLE")
@SuppressWarnings({"unused","WeakerAccess"})
public class DBTestTable {

    @ReflectDBField(fieldName = "id", fieldType = "INTEGER", notNull = true, primaryKey = true)
    private int id;
    @ReflectDBField(fieldName = "name")
    private String name;
    @ReflectDBField(fieldName = "age", fieldType = "INTEGER")
    private int age;

    public DBTestTable() { super(); }
    public DBTestTable(int id, String name, int age) { this.id = id; this.name = name; this.age = age; }

    public int getId() { return this.id; }
    public void setId(int id) { this.id = id; }

    public String getName() { return this.name; }
    public void setName(String name) { this.name = name; }

    public int getAge() { return this.age; }
    public void setAge(int age) { this.age = age; }
}
