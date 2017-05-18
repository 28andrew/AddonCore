package me.andrew28.addons.core;

import ch.njol.skript.lang.SkriptEvent;
import ch.njol.skript.lang.util.SimpleEvent;
import me.andrew28.addons.core.annotations.DoNotRegister;
import org.bukkit.event.Event;

/**
 * @author Andrew Tran
 */
@DoNotRegister
public class ASASimpleEvent<T extends Event> extends ASAEvent<T> {
    @Override
    public Class<? extends SkriptEvent> getSkriptEvent() {
        return SimpleEvent.class;
    }
}
