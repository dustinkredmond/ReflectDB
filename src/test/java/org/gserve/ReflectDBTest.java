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

import org.junit.Test;
import static org.junit.Assert.assertEquals;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Junit tests using local SQLite database.
 * Created: 12/24/2019 08:11
 * Author: Dustin K. Redmond
 */
@SuppressWarnings({"SqlResolve", "unused", "SqlNoDataSourceInspection"})
public class ReflectDBTest {

    private static final ReflectDB db = ReflectDB.initialize(
            new ReflectDBConfig("jdbc:sqlite:Database.db",
                    "TEST_DB",
                    "",
                    "",
                    3306,
                    "org.gserve"));

    @Test
    public void testA() {
        assert (db.getConfig() != null);
        // Since we'll be doing inserts with ID, need to drop.
        db.dropTable(DBTestTable.class);
    }

    @Test
    public void testB() {
        String ddl = db.getTableCreateDDL();
        assert (ddl != null && !ddl.isEmpty());
    }

    @Test
    public void testC() {
        try {
            db.createTablesIfNotExists();
        } catch (Exception e) {
            e.printStackTrace();
            assert (false);
        }
    }

    @Test
    public void testD() {
        final String sql = "INSERT INTO TEST_TABLE (id, name, age) VALUES (1,'John','18')";
        try (Connection conn = db.getNativeConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            db.createTablesIfNotExists();
            assert (ps.executeUpdate() > 0);
        } catch (SQLException e) {
            e.printStackTrace();
            assert(false);
        }
    }

    @Test
    public void testE() {
        final String sql = "SELECT * FROM TEST_TABLE WHERE id = 1";
        try (Connection conn = db.getNativeConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            db.createTablesIfNotExists();
            ResultSet rs = ps.executeQuery();
            rs.next();
            assertEquals(rs.getString("name").toUpperCase(), "JOHN");
        } catch (SQLException e) {
            assert false;
        }
    }

    @Test
    public void testF() {
        final String sql = "DELETE FROM TEST_TABLE WHERE id = 1";
        try (Connection conn = db.getNativeConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            db.createTablesIfNotExists();
            assertEquals(ps.executeUpdate(), 1);
        } catch (SQLException e) {
            e.printStackTrace();
            assert false;
        }
    }

    @Test
    public void testG() throws SQLException {
        db.createTablesIfNotExists();
        assert db.insert(new DBTestTable(1, "Dustin", 26));
    }

    @Test
    public void testH() {
        DBTestTable test = db.fetchSingle("SELECT * FROM TEST_TABLE WHERE ID = 1", DBTestTable.class);
        assert(db.delete(test));
    }

    @Test
    public void testI() {
        db.insert(new DBTestTable(1, "Dustin", 27));
        DBTestTable test = db.fetchSingle("SELECT * FROM TEST_TABLE WHERE ID = 1", DBTestTable.class);
        test.setAge(26);
        assert(db.save(test));
        DBTestTable test2 = db.fetchSingle("SELECT * FROM TEST_TABLE WHERE ID = 1", DBTestTable.class);
        assertEquals(test2.getAge(), 26);
    }

}
