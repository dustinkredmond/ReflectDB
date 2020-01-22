package org.gserve;
/*
 *  Copyright (C) 2020 Dustin K. Redmond
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
import org.gserve.reflectdb.annotations.ReflectDBTable;

import java.sql.Date;

/**
 * @author Dustin K. Redmond
 * @since 01/22/2020 14:55
 */
@ReflectDBTable(tableName = "DATE_TEST")
public class TableWithDateField {
    @ReflectDBField(fieldName = "ID", fieldType = "INTEGER(11)", notNull = true, primaryKey = true)
    private int id;
    @ReflectDBField(fieldName = "CREATED_DATE", fieldType = "DATE", notNull = true)
    private java.sql.Date created;

    public TableWithDateField() { super(); }

    public TableWithDateField(int id, Date created) {
        this.id = id;
        this.created = created;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Date getCreated() {
        return created;
    }

    public void setCreated(Date created) {
        this.created = created;
    }
}
