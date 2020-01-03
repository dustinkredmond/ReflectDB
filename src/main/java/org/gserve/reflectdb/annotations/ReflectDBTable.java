package org.gserve.reflectdb.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface ReflectDBTable {
    /**
     * The name of the database table with which to associate this type.
     * @return The name of the table as it appears in the database.
     */
    String tableName() default "";
}
