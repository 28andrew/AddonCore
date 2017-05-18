package me.andrew28.addons.core;

import me.andrew28.addons.core.annotations.DoNotRegister;

/**
 * @author Andrew Tran
 */
@DoNotRegister
public abstract class ASAPropertyExpression<T> extends ASAExpression<T> {
    @Override
    public String toString() {
        return "Andrew's Addon System Property Expression: " + getClass().getCanonicalName();
    }
}
