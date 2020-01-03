package org.gserve.reflectdb.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface ReflectDBField {
    /**
     * The name of the field in the database.
     * <p>
     *     E.g.
     * If the Java field is called {@code firstName},
     * then this could be called {@code FIRST_NAME}.
     * @return The name of the database column associated with this field.
     */
    String fieldName();

    /**
     * Set this to true if this column should be treated as a unique key.
     * @return True if this field is the database primary key.
     */
    boolean primaryKey() default false;

    /**
     * The type and optionally, length and precision of the field in the
     * database.
     * <p>
     * This field defaults to VARCHAR(255)
     * @return The database column datatype.
     */
    String fieldType() default "VARCHAR(255)";

    /**
     * Set this to true to specify that this field should not be nullable.
     * @return True if the field is not nullable, otherwise false.
     */
    boolean notNull() default false;
}
