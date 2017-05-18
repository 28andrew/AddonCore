package me.andrew28.addons.core;

import ch.njol.skript.lang.SkriptEvent;
import me.andrew28.addons.core.annotations.DoNotRegister;
import org.bukkit.event.Event;

import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Andrew Tran
 */
@DoNotRegister
public abstract class ASAEvent<T extends Event> implements AutoRegisteringSkriptElement {
    private Class<? extends Event> eventClass;
    private List<EventValue> eventValues = new ArrayList<>();

    public Class<? extends Event> getEventClass() {
        if (eventClass == null) {
            eventClass = (Class<T>) ((ParameterizedType) getClass()
                    .getGenericSuperclass()).getActualTypeArguments()[0];
        }
        return eventClass;
    }

    public abstract Class<? extends SkriptEvent> getSkriptEvent();

    public void registerEventValue(EventValue<T, ?> eventValue) {
        registerEventValue(eventValue, EventTime.NORMAL);
    }

    public void registerEventValue(EventValue<T, ?> eventValue, EventTime eventTime) {
        eventValue.setEventTime(eventTime);
        eventValues.add(eventValue);
    }

    public EventValue<T, ?>[] getEventValues() {
        return eventValues.toArray(new EventValue[eventValues.size()]);
    }

    public String getName() {
        return getEventClass().getSimpleName().replaceAll(
                String.format("%s|%s|%s",
                        "(?<=[A-Z])(?=[A-Z][a-z])",
                        "(?<=[^A-Z])(?=[A-Z])",
                        "(?<=[A-Za-z])(?=[^A-Za-z])"
                ),
                " "
        );
    }
}
