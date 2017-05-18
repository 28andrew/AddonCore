package me.andrew28.addons.core;

import ch.njol.skript.classes.Converter;
import me.andrew28.addons.core.annotations.DoNotRegister;

import java.lang.reflect.ParameterizedType;

/**
 * @author Andrew Tran
 */
@DoNotRegister
public abstract class ASAConverter<F, T> implements AutoRegisteringSkriptElement, Converter<F, T> {
    private Class<? extends F> fromClass;
    private Class<? extends T> toClass;

    public Class<? extends F> getFromClass() {
        if (fromClass == null) {
            fromClass = (Class<F>) ((ParameterizedType) getClass()
                    .getGenericSuperclass()).getActualTypeArguments()[0];
        }
        return fromClass;
    }

    public Class<? extends T> getToClass() {
        if (toClass == null) {
            toClass = (Class<T>) ((ParameterizedType) getClass()
                    .getGenericSuperclass()).getActualTypeArguments()[1];
        }
        return toClass;
    }
}
