package tel.kontra.leiriposti.controller;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import tel.kontra.leiriposti.model.Message;
import tel.kontra.leiriposti.model.MessageStatus;
import tel.kontra.leiriposti.model.SheetsNotFoundException;

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
}
