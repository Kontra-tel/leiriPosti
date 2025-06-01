package tel.kontra.leiriposti.model;

/**
 * MessageStatus enum represents the status of a message in the system.
 * It can be used to track whether a message has been printed, is in the process of printing,
 * is queued for printing, or has encountered an error.
 * 
 * <ul>
 *   <li><b>NOT_PRINTED</b>: The message has not been printed yet.</li>
 *   <li><b>PRINTED</b>: The message has been successfully printed.</li>
 *   <li><b>PRINTING</b>: The message is currently being printed.</li>
 *   <li><b>QUEUED</b>: The message is queued for printing.</li>
 *   <li><b>ERROR</b>: The message has encountered an error during processing.</li>
 * </ul>
 * 
 * This enum can be used in conjunction with the Message class to manage the state of messages
 * 
 * @version 1.0
 * @since 0.2
 */
public enum MessageStatus {
    /**
     * Message is not printed
     */
    NOT_PRINTED,

    /**
     * Message is printed
     */
    PRINTED,

    /**
     * Message is in printing process
     */
    PRINTING,

    /**
     * Message is queued for printing
     */
    QUEUED,

    /**
     * Message is in error state
     */
    ERROR,

    /**
     * ALL
     */
    ALL,
}
