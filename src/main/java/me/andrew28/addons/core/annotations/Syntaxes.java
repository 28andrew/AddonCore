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
public @interface Syntaxes {
    /**
     * Gets the {@link Syntax} annotations
     * @return the Syntax annotations
     */
    Syntax[] value();
}
