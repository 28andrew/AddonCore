package me.andrew28.addons.core.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author Andrew Tran
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface BSyntaxes {
    /**
     * Gets the Binded Syntax annotations {@link BSyntax}
     * @return the Binded Syntax annotations
     */
    BSyntax[] value();
}
