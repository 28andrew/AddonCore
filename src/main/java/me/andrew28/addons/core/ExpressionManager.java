package me.andrew28.addons.core;

import ch.njol.skript.lang.Expression;
import org.bukkit.event.Event;

import java.util.HashMap;

/**
 * @author Andrew Tran
 */
public class ExpressionManager {
    private HashMap<String, SkriptExpressionWrapper> skriptExpressionWrappers = new HashMap<>();
    private Event currentEvent;

    public Event getCurrentEvent() {
        return currentEvent;
    }

    public void setCurrentEvent(Event currentEvent) {
        this.currentEvent = currentEvent;
    }

    public HashMap<String, SkriptExpressionWrapper> getSkriptExpressionWrappers() {
        return skriptExpressionWrappers;
    }

    public boolean isSet(String key) {
        return skriptExpressionWrappers.containsKey(key);
    }

    public void set(String key, SkriptExpressionWrapper skriptExpressionWrapper) {
        skriptExpressionWrappers.put(key, skriptExpressionWrapper);
    }

    public void resetBinds() {
        skriptExpressionWrappers.clear();
    }

    public void bind(String key, SkriptExpressionWrapper skriptExpressionWrapper) {
        set(key, skriptExpressionWrapper);
    }

    public void bind(ElementSyntax elementSyntax, Expression<?>[] expressions) {
        for (int index = 0; index < elementSyntax.getBinds().length; index++) {
            String bindName = elementSyntax.getBinds()[index];
            // Optional binds start with '-'
            if (ArrayUtils.indexExists(expressions, index) || bindName.startsWith("-")) {
                if (ArrayUtils.indexExists(expressions, index)) {
                    bindName = bindName.startsWith("-") ? bindName.substring(1) : bindName;
                    bind(bindName, new SkriptExpressionWrapper(expressions[index]));
                }
            } else {
                throw new IllegalStateException(String.format("Out of bounds binding error. " +
                                "Bind with name %s was #%d to be binded but there are only %d expressions."
                        , bindName, index + 1, expressions.length));
            }
        }
    }

    public SkriptExpressionWrapper getSkriptExpressionWrapper(String key) {
        return skriptExpressionWrappers.get(key);
    }

    public Object get(String key) {
        return getSkriptExpressionWrapper(key).get(currentEvent);
    }

    public Object get(String key, Object fallback) {
        if (skriptExpressionWrappers.containsKey(key)) {
            Object object = skriptExpressionWrappers.get(key).get(currentEvent);
            if (object != null) {
                return object;
            }
        }
        return fallback;
    }

    public Object[] getMultiple(String key) {
        return getSkriptExpressionWrapper(key).getMultiple(currentEvent);
    }

    public Object[] getMultiple(String key, Object[] fallback) {
        if (skriptExpressionWrappers.containsKey(key)) {
            Object[] objects = skriptExpressionWrappers.get(key).getMultiple(currentEvent);
            if (objects != null) {
                return objects;
            }
        }
        return fallback;
    }
}
