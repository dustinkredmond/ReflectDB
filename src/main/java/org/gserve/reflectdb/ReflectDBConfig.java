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

import java.util.HashSet;

/**
 * Configuration object for the {@code ReflectDB} class. This class provides fields
 * to hold all of the necessary configuration.
 * @since  12/24/2019 08:13
 * @author Dustin K. Redmond
 */
@SuppressWarnings({"unused", "WeakerAccess"})
public class ReflectDBConfig {
    public ReflectDBConfig(String url, String databaseName, String databaseUsername, String databasePassword, int port) {
        this.url = url;
        this.databaseName = databaseName;
        this.databaseUsername = databaseUsername;
        this.databasePassword = databasePassword;
        this.port = port;
    }

    private String url;
    private String databaseName;
    private String databaseUsername;
    private String databasePassword;
    private int port;
    private String modelPackage;
    private HashSet<Class<?>> modelClasses = new HashSet<>();

    public void addModelClass(Class<?> modelClass) {
        modelClasses.add(modelClass);
    }

    public HashSet<Class<?>> getModelClasses() {
        return modelClasses;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getDatabaseName() {
        return databaseName;
    }

    public void setDatabaseName(String databaseName) {
        this.databaseName = databaseName;
    }

    public String getDatabaseUsername() {
        return databaseUsername;
    }

    public void setDatabaseUsername(String databaseUsername) {
        this.databaseUsername = databaseUsername;
    }

    public String getDatabasePassword() {
        return databasePassword;
    }

    public void setDatabasePassword(String databasePassword) {
        this.databasePassword = databasePassword;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public boolean isSqlite() {
        return this.url.toUpperCase().contains("SQLITE");
    }
    public boolean isMySql() { return this.url.toUpperCase().contains("MYSQL"); }
    public boolean isMariaDB() { return this.url.toUpperCase().contains("MARIADB"); }
}
