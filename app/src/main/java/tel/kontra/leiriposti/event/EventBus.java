package tel.kontra.leiriposti.event;

import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.event.EventType;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * EventBus is a simple thread-safe singleton event bus for JavaFX events.
 * It allows registering event handlers for specific event types and posting events from anywhere in the application.
 *
 * Usage:
 *   - Register a handler: EventBus.getInstance().register(EventType, handler)
 *   - Post an event: EventBus.getInstance().post(event)
 *
 * @version 1.0
 * @since 0.2
 *
 * @author Markus
 */
public class EventBus {
    private static final EventBus instance = new EventBus(); // Singleton instance
    private final Map<EventType<? extends Event>, CopyOnWriteArrayList<EventHandler<? extends Event>>> listeners = new ConcurrentHashMap<>(); // Registered listeners

    /**
     * Private constructor to enforce singleton pattern.
     */
    private EventBus() {}

    /**
     * Get the singleton instance of the EventBus.
     * @return the singleton EventBus instance
     */
    public static EventBus getInstance() {
        return instance;
    }

    /**
     * Register an event handler for a specific event type.
     * @param type the event type to listen for
     * @param handler the event handler to register
     * @param <T> the type of event
     */
    public <T extends Event> void register(EventType<T> type, EventHandler<T> handler) {
        listeners.computeIfAbsent(type, k -> new CopyOnWriteArrayList<>()).add(handler);
    }

    /**
     * Post an event to all registered handlers for its type.
     * @param event the event to post
     */
    public void post(Event event) {
        CopyOnWriteArrayList<EventHandler<? extends Event>> handlers = listeners.get(event.getEventType());
        if (handlers != null) {
            for (EventHandler<? extends Event> handler : handlers) {
                @SuppressWarnings("unchecked")
                EventHandler<Event> safeHandler = (EventHandler<Event>) handler;
                safeHandler.handle(event);
            }
        }
    }
}
