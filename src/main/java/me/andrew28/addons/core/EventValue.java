package me.andrew28.addons.core;

import ch.njol.skript.util.Getter;
import org.bukkit.event.Event;

import java.lang.reflect.ParameterizedType;

/**
 * @author Andrew Tran
 */
public abstract class EventValue<E extends Event, V> extends Getter<V, E> {
    private Class<? extends Event> eventClass;
    private Class<?> valueClass;
    private EventTime eventTime;

    /**
     * Gets the time it was retrieved from an event
     *
     * @return the time it was retrieved from an event
     */
    public EventTime getEventTime() {
        return eventTime;
    }

    /**
     * Sets the time it was retrieved from an event
     *
     * @param eventTime the time to set
     */
    public void setEventTime(EventTime eventTime) {
        this.eventTime = eventTime;
    }

    /**
     * Gets the class of the {@link Event}
     *
     * @return the class of the Event
     */
    public Class<? extends Event> getEventClass() {
        if (eventClass == null) {
            eventClass = (Class<E>) ((ParameterizedType) getClass()
                    .getGenericSuperclass()).getActualTypeArguments()[0];
        }
        return eventClass;
    }

    /**
     * Gets the class of the value
     *
     * @return the class of the value
     */
    public Class<?> getValueClass() {
        if (valueClass == null) {
            valueClass = (Class<E>) ((ParameterizedType) getClass()
                    .getGenericSuperclass()).getActualTypeArguments()[1];
        }
        return valueClass;
    }
}
