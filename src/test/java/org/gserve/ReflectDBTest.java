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

import org.gserve.reflectdb.ReflectDB;
import org.gserve.reflectdb.ReflectDBConfig;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Junit tests using local SQLite database.
 * @since 12/24/2019 08:11
 * @author Dustin K. Redmond
 */
@SuppressWarnings({"unused"})
public class ReflectDBTest {

    private static final ReflectDB db = ReflectDB.initialize(
            new ReflectDBConfig("jdbc:sqlite:TEST_DATABASE.db",
                    "TEST_DB",
                    "",
                    "",
                    3306));

    @BeforeAll
    public static void testA() {
        db.addModelClass(DBTestTable.class);

        // To mitigate bug in SQLite see ReflectDBConfig's addModelClass() method
        if (!db.getConfig().isSqlite()) {
            db.addModelClass(TableWithDateField.class);
        }

        // Since we'll be doing inserts with ID, need to drop.
        db.dropTable(DBTestTable.class);

    }

    @Test
    public void testB() {
        String ddl = db.getTableCreateDDL();
        assert (ddl != null && !ddl.isEmpty());
        System.out.println(ddl);
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
            fail(e);
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
    public void testG() {
        try {
            db.createTablesIfNotExists();
            assert db.insert(new DBTestTable(1, "Dustin", 26));
        } catch (SQLException e) {
            fail(e);
        }
    }

    @Test
    public void testH() {
        DBTestTable test = db.fetchSingle("SELECT * FROM TEST_TABLE WHERE ID = 1", DBTestTable.class);
        assert(db.delete(test));
    }

    @Test
    public void testI() {
        try {
            db.insert(new DBTestTable(1, "Dustin", 27));
        } catch (SQLException e) {
            fail(e);
        }
        DBTestTable test = db.fetchSingle("SELECT * FROM TEST_TABLE WHERE ID = 1", DBTestTable.class);
        test.setAge(26);
        assert(db.save(test));
        DBTestTable test2 = db.fetchSingle("SELECT * FROM TEST_TABLE WHERE ID = 1", DBTestTable.class);
        assertEquals(test2.getAge(), 26);
    }

    @Test
    public void testJ() {
        // test fetching object that doesn't exist
        DBTestTable test = db.fetchSingle("SELECT * FROM TEST_TABLE WHERE ID = 500", DBTestTable.class);
        assertNull(test);
        List<DBTestTable> tests;
        try {
            tests = db.fetch("SELECT * FROM TEST_TABLE WHERE ID = 500", DBTestTable.class);
            assertEquals(0, tests.size());
        } catch (SQLException e) {
            fail(e);
        }
    }

    @Test
    public void testK() {
        try {
            db.insert(new DBTestTable(250, "TestPerson", 25));
            assertEquals("TestPerson", db.findById(250, DBTestTable.class).getName());
        } catch (SQLException e) {
            fail(e);
        }
    }

    @Test
    public void testL() {
        try {
            DBTestTable test = db.findById(1024, DBTestTable.class);
            assertNull(db.findById(1024, DBTestTable.class));
        } catch (SQLException e) {
            fail(e);
        }

    }

    @Test
    public void testM() {
        if (db.getConfig().isSqlite()) {
            // Test not meant for SQLite databases.
            return;
        }
        java.sql.Date createDate = java.sql.Date.valueOf("2019-01-05");
        TableWithDateField test = new TableWithDateField(1, createDate);
        db.save(test);
        try {
            assertEquals(db.findById(1, TableWithDateField.class).getCreated(), createDate);
        } catch (SQLException e) {
            fail(e);
        }
    }

    @AfterAll
    static void tearDown() {
        // You should test with both SQLite as well as MySQL/MariaDB
        // SQLite makes it very apparent if all connection objects haven't
        // been closed, as TEST_DATABASE.db will hang around.
        if (db.getConfig().isSqlite()) {
            try {
                assertTrue(Files.deleteIfExists(Paths.get("TEST_DATABASE.db")),
                        "Unable to remove TEST_DATABASE, java.sql.Connection " +
                                "objects have not been closed.");
            } catch (IOException e) {
                fail(String.format("Failed with (%s), this may mean that a java.sql.Connection" +
                        "was not properly closed. TEST_DATABASE not removed.\n", e.getMessage()));
            }
        } else {
            // NOTE If anymore tables are used during testing, drop them here.
            db.dropTable(DBTestTable.class);
        }

    }
}
