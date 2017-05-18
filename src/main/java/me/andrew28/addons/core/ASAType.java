package me.andrew28.addons.core;

import ch.njol.skript.classes.ClassInfo;
import me.andrew28.addons.core.annotations.DoNotRegister;

/**
 * @author Andrew Tran
 */
@DoNotRegister
public abstract class ASAType<T> implements AutoRegisteringSkriptElement {
    public abstract ClassInfo<T> getClassInfo();
}
