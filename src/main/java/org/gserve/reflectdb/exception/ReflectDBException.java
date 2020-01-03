package org.gserve.reflectdb.exception;
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

import java.lang.annotation.IncompleteAnnotationException;
import java.sql.SQLException;

/**
 * Utility class used to wrap exceptions and give a custom message and ErrorCode.
 * ReflectDB's documentation will maintain detailed information on these codes.
 *
 * @since  12/26/2019 10:15
 * @author Dustin K. Redmond
 */
public class ReflectDBException extends RuntimeException {

    public ReflectDBException(String message, Throwable e) {
        super(message, e);
        if (e instanceof IllegalAccessException | e instanceof NoSuchFieldException) {
            this.errorCode = ErrorCode.REFLECTION;
        } else if (e instanceof IncompleteAnnotationException | e instanceof IncompleteAnnotationException){
            this.errorCode = ErrorCode.ANNOTATION;
        } else if (e instanceof SQLException) {
            this.errorCode = ErrorCode.SQL;
        }
    }

    public ReflectDBException(String message) {
        super(message);
    }
    public ReflectDBException(Throwable e) { super(e); }

    private enum ErrorCode {
        REFLECTION,
        ANNOTATION,
        SQL
    }

    private ErrorCode errorCode;

    public ErrorCode getCode() { return this.errorCode; }
}
