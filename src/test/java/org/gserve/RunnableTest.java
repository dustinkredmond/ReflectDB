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

/**
 * Dummy class used during development. Tests can be run here, but
 * mainly use it to play around with the API.
 *
 * @since 12/24/2019 08:35
 * @author Dustin K. Redmond
 */
public class RunnableTest {

    private static final ReflectDBConfig CONFIG = new ReflectDBConfig("jdbc:sqlite:TEST_DB.db",
                    "TEST_DB",
                    "",
                    "",
                    3306,
                    "org.gserve");

    public static void main(String[] args) {
        // Test API during development here.
    }
}
