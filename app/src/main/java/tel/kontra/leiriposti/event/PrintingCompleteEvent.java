package tel.kontra.leiriposti.event;

import javafx.event.Event;
import javafx.event.EventTarget;
import javafx.event.EventType;

/**
 * PrintingCompleteEvent is fired when a print job or all queued print jobs are completed.
 * It can be used to notify listeners in the GUI or other controllers.
 *
 * @version 1.0
 * @since 0.2
 */
public class PrintingCompleteEvent extends Event {
    public static final EventType<PrintingCompleteEvent> PRINTING_COMPLETE_EVENT_TYPE =
            new EventType<>(Event.ANY, "PRINTING_COMPLETE_EVENT");

    private final String message;

    /**
     * Constructs a PrintingCompleteEvent with an optional message.
     * @param message A message describing the completion (can be null).
     */
    public PrintingCompleteEvent(String message) {
        super(PRINTING_COMPLETE_EVENT_TYPE);
        this.message = message;
    }

    /**
     * Constructs a PrintingCompleteEvent with source, target, and message.
     * @param source The source of the event.
     * @param target The target of the event.
     * @param message A message describing the completion (can be null).
     */
    public PrintingCompleteEvent(Object source, EventTarget target, String message) {
        super(source, target, PRINTING_COMPLETE_EVENT_TYPE);
        this.message = message;
    }

    /**
     * Gets the completion message.
     * @return The completion message, or null if not set.
     */
    public String getMessage() {
        return message;
    }

    @Override
    public EventType<? extends PrintingCompleteEvent> getEventType() {
        return PRINTING_COMPLETE_EVENT_TYPE;
    }

    @Override
    public PrintingCompleteEvent copyFor(Object newSource, EventTarget newTarget) {
        return new PrintingCompleteEvent(newSource, newTarget, message);
    }
}
