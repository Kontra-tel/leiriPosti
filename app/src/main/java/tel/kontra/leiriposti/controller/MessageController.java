package tel.kontra.leiriposti.controller;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import tel.kontra.leiriposti.model.Message;
import tel.kontra.leiriposti.model.MessageStatus;
import tel.kontra.leiriposti.model.SheetsNotFoundException;
import tel.kontra.leiriposti.view.MainGui;

/**
 * MessageController class is responsible for managing messages in the application.
 * It follows the singleton design pattern to ensure only one instance exists.
 * 
 * @version 1.0
 * @since 0.2
 * 
 * @author Markus
 */
public class MessageController {
    	
    private static final Logger LOGGER = LogManager.getLogger(); // Logger for debugging

    private static MessageController instance; // Singleton instance
    private SheetsController sheetsController; // SheetsController instance for Google Sheets API

    private Date latestMessage; // Date of last message
    private List<Message> messages; // List of messages

    /**
     * Private constructor for MessageController.
     * Initializes the messages list.
     * 
     * @param spreadsheetId The ID of the Google Sheets spreadsheet to be accessed.
     * This constructor is private to enforce the singleton pattern.
     */
    private MessageController(SheetsController sheetsController, List<Message> messages) {
        this.sheetsController = sheetsController; // Initialize the SheetsController instance
        this.messages = messages;
        this.latestMessage = null;
    }

    /**
     * Get the singleton instance of MessageController.
     * 
     * @return The singleton instance of MessageController.
     * if the controller is not initialized, it will throw an IllegalStateException.
     */
    public static synchronized MessageController getInstance() {

        // Check that sheetsController is initialized
        if (instance == null) {
            throw new IllegalStateException("SheetsController is not initialized. Please initialize it first.");
        }
        return instance; // Return the existing instance if already created
    }

    /**
     * Get the singleton instance of MessageController.
     * 
     * @param sheetsController The SheetsController instance to be used for Google Sheets API.
     * if the controller is not initialized, it will throw an IllegalStateException.
     * @return The singleton instance of MessageController.
     */
    public static synchronized MessageController getInstance(SheetsController sheetsController) {

        // Check that sheetsController is initialized
        if (sheetsController.isInitialized() == false) {
            throw new IllegalStateException("SheetsController is not initialized. Please initialize it first.");
        }
        if (instance == null) {
            instance = new MessageController(sheetsController, new ArrayList<>()); // Create new instance if not already created
        }
        return instance;
    }

    /**
     * Get the singleton instance of MessageController with a list of messages.
     * 
     * @param sheetsController The SheetsController instance to be used for Google Sheets API.
     * @param messages The initial list of messages to be managed by the controller.
     * @return The singleton instance of MessageController.
     * if the controller is not initialized, it will throw an IllegalStateException.
     */
    public static synchronized MessageController getInstance(SheetsController sheetsController, List<Message> messages) {
        
        // Check that sheetsController is initialized
        if (sheetsController.isInitialized() == false) {
            throw new IllegalStateException("SheetsController is not initialized. Please initialize it first.");
        }
        if (instance == null) {
            instance = new MessageController(sheetsController, messages); // Create new instance if not already created
        } else {
            instance.setMessages(messages); // Update the messages list if instance already exists
        }
        return instance;
    }

    /**
     * Get the list of messages.
     * 
     * @return List of messages.
     */
    public List<Message> getMessages() {
        return messages;
    }

    /**
     * Get the list of messages filtered by status.
     * 
     * @param filter The status to filter messages by. If null, returns all messages.
     * @return List of messages filtered by the specified status.
     */
    public List<Message> getMessages(MessageStatus filter) {
        if (filter == null) {
            return messages; // Return all messages if no filter is applied
        }

        // If filter is ALL, return all messages
        if (filter == MessageStatus.ALL) {

            // Return all messages except DELETED
            List<Message> filteredMessages = new ArrayList<>();
            for (Message message : messages) {
                if (message.getStatus() != MessageStatus.DELETED) {
                    filteredMessages.add(message); // Add message to filtered list if it is not deleted
                }
            }
            return filteredMessages; // Return the filtered list of messages
        }

        List<Message> filteredMessages = new ArrayList<>();
        for (Message message : messages) {
            if (message.getStatus() == filter) {
                filteredMessages.add(message); // Add message to filtered list if it matches the filter
            }
        }
        return filteredMessages; // Return the filtered list of messages
    }

    /**
     * Set the list of messages.
     * This method replaces the current list of messages with the provided list.
     * Used to load messages from persistent storage or to update the list.
     * 
     * @param message The message to be added.
     */
    public void setMessages(List<Message> messages) {
        this.messages = messages;
    }

    /**
     * Retrieves new messages from the Google Sheets spreadsheet.
     * This method fetches messages that have not been previously retrieved.
     * It updates the internal list of messages and the timestamp of the latest message.
     */
    public void getNewMessages() throws SheetsNotFoundException {
        List<Message> messages = sheetsController.getNewMessages();

        if (messages != null && !messages.isEmpty()) {
            this.messages.addAll(messages); // Add new messages to the existing list
            
            LOGGER.info("New messages retrieved: " + messages.size());
            
            // Update timestamp of the latest message
            if (latestMessage == null || messages.get(messages.size() - 1).getTimeStamp().after(latestMessage)) {
                latestMessage = messages.get(messages.size() - 1).getTimeStamp(); // Update latest message date
            }
        } else {
            LOGGER.info("No new messages found.");
        }
    }

    /**
     * Sets the status of a message.
     * This method updates the status of a specific message in the list.
     * 
     * @param message
     * @param status
     */
    public void setMessageStatus(Message message, MessageStatus status) {
        if (message == null || status == null) {
            throw new IllegalArgumentException("Message and status cannot be null.");
        }
        
        // Find the message in the list and update its status
        for (Message msg : messages) {
            if (msg.equals(message)) {
                updateMessageStatus(message, status);
                LOGGER.info("Message status updated: " + msg.getSubject() + " to " + status);
                return;
            }
        }
        
        LOGGER.warn("Message not found: " + message.getSubject());
    }

    /**
     * Updates the status of a message.
     * This method is used internally to handle the logic for updating the status of a message,
     * for example, when a message is printed, queued, or encounters an error.
     * 
     * This method is private to encapsulate the logic for updating message status.
     * 
     * @param message The message whose status is to be updated.
     * @param status The new status to set for the message.
     */
    private void updateMessageStatus(Message message, MessageStatus status) {
        if (message == null || status == null) {
            throw new IllegalArgumentException("Message and status cannot be null.");
        }

        // If the message is currently being printed, do not change its status
        if(message.getStatus() == MessageStatus.PRINTING && !MainGui.isDebug()) {
            LOGGER.warn("Message is currently being printed: " + message.getSubject());
            return; // Do not change status if it is currently printing
        }

        // Get printer controller instance
        PrinterController printerController = PrinterController.getInstance();
        
        /**
         * Handle different message statuses.
         * This switch statement determines the action to take based on the new status of the message.
         * It updates the message status and performs actions such as adding or removing the message from the print queue.
         * 
         * The statuses are handled as follows:
         * - PRINTED: No action needed, message is already printed.
         * - PRINTING: No action needed, message is currently being printed.
         * - DELETED: No action needed, message is deleted.
         * - NOT_PRINTED: If the message was previously QUEUED, remove it from the print queue.
         * - QUEUED: If the message is not printed, add it to the print queue.
         * - ERROR: Log an error message, no further action needed.
         * 
         * This logic ensures that the message status is updated correctly and that the print queue is managed appropriately.
         */
        switch(status) {
            case PRINTED: break; // No action needed for these statuses
            case PRINTING: break; // No action needed for these statuses
            case DELETED: break; // No action needed for these statuses

            case NOT_PRINTED: 
                // If the messages last status was QUEUED, we need to remove it from the print queue
                if (message.getStatus() == MessageStatus.QUEUED) {
                    printerController.removeFromPrintQueue(message); // Remove message from the print queue
                    LOGGER.info("Message removed from print queue: " + message.getSubject());
                }

                // If the message is currently being printed do nothing
                if (message.getStatus() == MessageStatus.PRINTING) {
                    LOGGER.warn("Message is currently being printed: " + message.getSubject());
                    return; // Do not change status if it is currently printing
                }
                break;

            case QUEUED:
                // We will also need to add the message to the print queue
                printerController.addToPrintQueue(message); // Add message to the print queue
                LOGGER.info("Message queued for printing: " + message.getSubject());
                break;

            case ERROR:
                LOGGER.warn("Encountered an error while printing message: " + message.getSubject());
                break;
            
            default:
                throw new IllegalArgumentException("Invalid status: " + status);
        }
        
        // Finally, update the status of the message
        message.setStatus(status); // Update the status of the message

        LOGGER.warn("Message not found: " + message.getSubject());
    }
}
