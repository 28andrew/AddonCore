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
public @interface BSyntax {
    /**
     * Gets the {@link ch.njol.skript.Skript} syntaxes
     * @return the {@link ch.njol.skript.Skript} syntaxes
     */
    String[] syntax();

    /**
     * Gets the bind names (e.g {@code "text"})
     * @return the bind names
     */
    String[] bind();
}
