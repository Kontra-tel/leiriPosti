package tel.kontra.leiriposti.event;

import javafx.event.Event;
import javafx.event.EventTarget;
import javafx.event.EventType;

/**
 * ValueUpdateEvent class is an event that is triggered when a value is updated.
 * It extends the javafx Event class and carries the updated value as a string.
 * 
 * This event can be used in various parts of the application to notify listeners
 * about changes in values, such as when a user updates a field in the GUI.
 * 
 * @version 1.0
 * @since 0.2
 */
public class ValueUpdateEvent extends Event {
    public static final EventType<ValueUpdateEvent> VALUE_UPDATE_EVENT_TYPE =
            new EventType<>(Event.ANY, "VALUE_UPDATE_EVENT");
    protected static final String Type = null;

    private final String value;

    /**
     * Constructs a ValueUpdateEvent with the specified value.
     *
     * @param value The updated value as a string.
     */
    public ValueUpdateEvent(String value) {
        super(VALUE_UPDATE_EVENT_TYPE);
        this.value = value;
    }

    /**
     * Constructs a ValueUpdateEvent with the specified source, target, and value.
     *
     * @param source The source of the event.
     * @param target The target of the event.
     * @param value  The updated value as a string.
     */
    public ValueUpdateEvent(Object source, EventTarget target, String value) {
        super(source, target, VALUE_UPDATE_EVENT_TYPE);
        this.value = value;
    }

    /**
     * Gets the updated value associated with this event.
     *
     * @return The updated value as a string.
     */
    public String getValue() {
        return value;
    }

    @Override
    public EventType<? extends ValueUpdateEvent> getEventType() {
        return VALUE_UPDATE_EVENT_TYPE;
    }

    @Override
    public ValueUpdateEvent copyFor(Object newSource, EventTarget newTarget) {
        return new ValueUpdateEvent(newSource, newTarget, value);
    }
}