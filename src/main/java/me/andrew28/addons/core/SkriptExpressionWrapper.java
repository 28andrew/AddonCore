package me.andrew28.addons.core;

import ch.njol.skript.lang.Expression;
import org.bukkit.event.Event;

/**
 * @author Andrew Tran
 */
public class SkriptExpressionWrapper<T> {
    private Expression<T> skriptExpression;

    public SkriptExpressionWrapper(Expression<T> skriptExpression) {
        this.skriptExpression = skriptExpression;
    }

    public Expression<T> getSkriptExpression() {
        return skriptExpression;
    }

    public void setSkriptExpression(Expression<T> skriptExpression) {
        this.skriptExpression = skriptExpression;
    }

    public T get(Event event) {
        return skriptExpression != null ? skriptExpression.getSingle(event) : null;
    }

    public T[] getMultiple(Event event) {
        return skriptExpression != null ? skriptExpression.getAll(event) : null;
    }
}
