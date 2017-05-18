package me.andrew28.addons.core;

import ch.njol.skript.classes.Changer;

import java.lang.reflect.ParameterizedType;

/**
 * @author Andrew Tran
 */
public abstract class ASAChanger<T> {
    private Class<? extends T> deltaType;

    public Class<? extends T> getDeltaType() {
        if (deltaType == null) {
            // TODO : Make less hacky
            deltaType = (Class<T>) ((ParameterizedType) getClass()
                    .getGenericSuperclass()).getActualTypeArguments()[0];
        }
        return deltaType;
    }

    public abstract Changer.ChangeMode[] getChangeModes();

    public abstract void change(T[] delta) throws NullExpressionException;
}
