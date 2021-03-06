package org.gserve.reflectdb;
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

import org.gserve.reflectdb.exception.ReflectDBException;
import org.gserve.reflectdb.model.ReflectDBColumn;
import org.gserve.reflectdb.model.ReflectDBTable;
import org.gserve.reflectdb.query.ReflectDBQuery;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.List;

/**
 * @since  12/24/2019 08:11
 * @author Dustin K. Redmond
 */
@SuppressWarnings("unused")
public class ReflectDB {
    private static ReflectDBConfig config = null;
    private final ReflectDBQuery QUERY = new ReflectDBQuery();

    private ReflectDB() { super(); }

    /**
     * Initialize the ReflectDB library with configuration to be used throughout.
     * @param config {@code ReflectDBConfig} instance with database configuration.
     * @return {@code ReflectDB} instance for working with the database.
     */
    public static ReflectDB initialize(ReflectDBConfig config) {
        ReflectDB.config = config;
        return ReflectDB.getInstance();
    }

    /**
     * Initialize the ReflectDB library with a {@code javax.sql.DataSource} instead
     * of a {@code ReflectDBConfig}
     * @param dataSource {@code javax.sql.DataSource}
     * @return Configured ReflectDB instance
     * @throws SQLException If the DataSource creation throws SQLException
     */
    public static ReflectDB initialize(DataSource dataSource) throws SQLException {
        config = new ReflectDBConfig(dataSource);
        return ReflectDB.getInstance();
    }

    /**
     * Gets an instance of the ReflectDB class which contains API methods.
     * @return a ReflectDB instance if one has been initialized.
     */
    public static ReflectDB getInstance() {
        if (ReflectDB.config == null) {
            throw new UnsupportedOperationException("ReflectDBConfig has not yet been initialized.");
        } else {
            return new ReflectDB();
        }
    }

    /**
     * Can be used to verify the table DDL that ReflectDB uses to prepare
     * the database tables.
     * @return SQL DDL used in creating database tables.
     */
    public String getTableCreateDDL() {
        StringBuilder sb = new StringBuilder();
        ReflectDBTable.findAll().forEach(table -> {
            sb.append("CREATE TABLE IF NOT EXISTS ").append(table.getTableName()).append("(\n");
            List<ReflectDBColumn> columns = table.getColumns();
            for (int i = 0; i < columns.size(); i++) {
                ReflectDBColumn col = columns.get(i);
                String nullable = col.getNotNull() ? "NOT NULL" : "NULL";
                String primaryKey;
                // Yet another SQLite peculiarity AUTO_INCREMENT not needed
                if (!config.isSqlite()) {
                    primaryKey = col.isPrimaryKey() ? " AUTO_INCREMENT PRIMARY KEY " : "";
                } else {
                    primaryKey = col.isPrimaryKey() ? " PRIMARY KEY " : "";
                }
                sb.append("\t").append(col.getColumnName()).append(" ").append(col.getColumnType()).append(" ").append(nullable).append(primaryKey);
                if (i < columns.size() -1) {
                    sb.append(",\n");
                }
            }
            sb.append("\n);\n");
        });
        return sb.toString();
    }

    /**
     * Attempts to create tables in database annotated with ReflectDB annotations.
     * This method uses the DDL from the {@code ReflectDB.getTableCreateDDL()} method,
     * which may be used to preview the table structure.
     * @throws SQLException If a SQLException occurs during table creation.
     */
    public void createTablesIfNotExists() throws SQLException {
        for (String sql : getTableCreateDDL().split(";")) {
            if (sql.isEmpty() || (sql.trim().isEmpty())) {
                // SQLException occurs if attempting to execute empty query
                // throws error code 1065
                continue;
            }
            try (Connection conn = getNativeConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.executeUpdate();
            } catch (SQLException e) {
                // Workaround for bug in SQLite where SQLException is thrown
                // even after successful table creation. -dustin 2019-12-24
                if (!(config.isSqlite() && e.getMessage().startsWith("The prepared statement has been finalized"))) {
                    throw new SQLException(e);
                }
            }
        }
    }

    /**
     * Fetches an object from the database by its unique ID value. This query uses the DB primary key
     * as a search criterion.
     * @param id Unique ID of the object (primary key)
     * @param modelClass Type to fetch.
     * @param <T> Type of the class being returned.
     * @return An object with specified ID, or null if not exists.
     */
    public <T> T findById(long id, Class<T> modelClass) throws SQLException {
        return QUERY.findById(id, modelClass);
    }

    /**
     * Uses full database connection string and credentials if necessary to retrieve a {@code Connection}
     * , otherwise just uses the URL.
     * @return {@code java.sql.Connection}
     * @throws SQLException If a {@code Connection} cannot be established.
     */
    private static Connection getJdbcConnection() throws SQLException {
        if (ReflectDB.config == null) {
            throw new UnsupportedOperationException(
                    "ReflectDB has not yet been initialized with a ReflectDBConfig.");
        }
        if (config.getDataSource() != null) {
           return config.getDataSource().getConnection();
        } else if (config.getDatabaseUsername().isEmpty() || config.getDatabasePassword().isEmpty()) {
            // SQLite doesn't require these, so only need URL
            return DriverManager.getConnection(config.getUrl());
        } else {
            // All other RDBMS, authenticate with url, user, and password
            return DriverManager.getConnection(config.getUrl(), config.getDatabaseUsername(), config.getDatabasePassword());
        }
    }

    /**
     * Convenience method to get a {@code java.sql.Connection} using the {@code DriverManager.getConnection()} method.
     * @return A {@code java.sql.Connection} for the configured database.
     */
    public Connection getNativeConnection() throws SQLException {
        return ReflectDB.getJdbcConnection();
    }

    /**
     * Fetches a single object from a SQL query. Returns null
     * if the query does not find any records.
     * @param sql SQL query
     * @param modelClass Class of the object to return.
     * @param <T> Type of the object being returned.
     * @return Returns a single object fitting the specified query.
     */
    public <T> T fetchSingle(String sql, Class<T> modelClass) {
        try {
            return new ReflectDBQuery().fetchSingle(sql, modelClass);
        } catch (SQLException e) {
            return null;
        }
    }

    /**
     * Returns a {@code List} of objects based on a SQL query and its type.
     * <p>E.g. {@code ReflectDB.fetch("SELECT * FROM PEOPLE", Person.class);} returns
     * {@code Person} objects fitting the query.
     * @param sql SQL query
     * @param modelClass The class of the modeled object.
     * @param <T> The type of the object returned.
     * @return List of objects representing a SQL query.
     */
    public <T> List<T> fetch(String sql, Class<T> modelClass) throws SQLException {
        return QUERY.fetch(sql, modelClass);
    }

    /**
     * Inserts a new object into the ReflectDB table.
     * @param obj A newly created object to be inserted.
     * @return True if the INSERT appears to succeed.
     */
    public boolean insert(Object obj) throws SQLException {
        return QUERY.insert(obj);
    }

    /**
     * Attempts to save the current object (UPDATE) it in the database.
     * <p>Internally, ReflectDB uses the primary key to look up objects,
     * therefore the object to be saved should not have a primary key that
     * deviates from the original.
     * <p>If you would like to update an object's primary key, it is recommended
     * that you do so using a native SQL query.
     * @param obj Object whose properties should be updated.
     * @return Returns true if the update appears successful.
     */
    public boolean save(Object obj) {
        try {
            return new ReflectDBQuery().save(obj);
        } catch (Exception e) {
            throw new ReflectDBException(e);
        }
    }

    /**
     *  Attempts to delete an object from the database.
     * @param obj Object to remove from the database, must have primary key.
     * @return Returns true if the object was removed successfully.
     */
    public boolean delete(Object obj) {
        return new ReflectDBQuery().delete(obj);
    }

    /**
     * Drops a table if it exists.
     * <p><b>This drops the actual database table, use caution.
     * @param modelClass The ReflectDB annotated database table to drop.
     * @param <T> The ReflectDB type.
     */
    public <T> void dropTable(Class<T> modelClass) {
        org.gserve.reflectdb.annotations.ReflectDBTable table = modelClass.getAnnotation(org.gserve.reflectdb.annotations.ReflectDBTable.class);
        if (table == null || table.tableName().isEmpty()) {
            throw new ReflectDBException("Unable to issue DROP command on a class that " +
                    "does not contain ReflectDBTable annotation or that does not specify " +
                    "table name.");
        } else {
            final String sql = String.format("DROP TABLE IF EXISTS %s;", table.tableName());
            try (Connection conn = this.getNativeConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.executeUpdate();
            } catch (SQLException e) {
                throw new ReflectDBException(String.format("SQL Exception occurred when attempting" +
                        " to drop table: %s\t|%s", table.tableName(), e.getMessage()));
            }
        }
    }

    /**
     * Designates a class annotated with {@code ReflectDBTable} to be used by ReflectDB.
     * @param modelClass Class annotated with {@code ReflectDBTable}
     */
    public void addModelClass(Class<?> modelClass) {
        this.getConfig().addModelClass(modelClass);
    }

    /**
     * Designates a list of classes annotated with {@code ReflectDBTable} to be used by ReflectDB.
     * @param classes Classes annotated with {@code ReflectDBTable}
     */
    public void addModelClasses(Class<?>... classes) {
        for (Class<?> aClass : classes) {
            this.getConfig().addModelClass(aClass);
        }
    }

    /**
     * Returns the {@code ReflectDBConfig} currently in use.
     * @return The currently set {@code ReflectDBConfig}
     */
    public ReflectDBConfig getConfig() {
        return ReflectDB.config;
    }

    /**
     * Returns a list of all entities in a ReflectDB table.
     * @param modelClass The ReflectDB table to query.
     * @param <T> The ReflectDB type.
     * @return List of all objects in a table.
     */
    public <T> List<T> fetchAll(Class<T> modelClass) throws SQLException {
        return QUERY.fetchAll(modelClass);
    }

    /**
     * Returns a list of entities in a ReflectDB table with a specified limit.
     * @param modelClass The ReflectDB table to query.
     * @param limit The limit of records to fetch.
     * @param <T> The ReflectDB type.
     * @return List of objects in the table.
     */
    public <T> List<T> fetch(Class<T> modelClass, int limit) throws SQLException {
        return QUERY.fetchAll(modelClass, limit);
    }
}
