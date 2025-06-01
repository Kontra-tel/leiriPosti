package tel.kontra.leiriposti.controller;

import tel.kontra.leiriposti.model.Message;
import tel.kontra.leiriposti.model.SheetsNotFoundException;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.api.services.sheets.v4.Sheets;

//TODO: Make range dynamic, so that it can be used for multiple sheets

/**
 * SheetsController class is responsible for managing the Google Sheets API service.
 * It provides methods to retrieve messages from a Google Sheets spreadsheet.
 * 
 * Im not happy with this way of doing things as it is not very flexible as everything is hardcoded.
 * I might change this in the future to possibly use a database or create the sheets and forms dynamically.
 * 
 * @version 0.6
 * @since 0.1
 * 
 * @author Markus
 */
public class SheetsController {

    private static SheetsController instance; // Singleton instance

    private final Logger LOGGER = LogManager.getLogger(); // Logger for debugging

    private Sheets sheetsService; // Sheets API service
    private String spreadsheetId; // Spreadsheet ID
    private String sheetName; // Sheet name

    private int latestRow; // Latest row number in the spreadsheet

    /**
     * Private constructor for SheetsController.
     * Initializes the Sheets API service and sets the spreadsheet ID.
     */
    private SheetsController(int lastRow) {

        // Check if lastRow is less than 2, if so, set it to 2
        if (lastRow < 2) {
            LOGGER.warn("Last row number is less than 2, setting it to 2.");
            lastRow = 2; // Set to 2 to avoid issues with empty sheets
        }

        this.latestRow = lastRow; // Set the latest row number
    }

    /**
     * Get the singleton instance of SheetsController.
     * @return The singleton instance of SheetsController.
     */
    public static synchronized SheetsController getInstance() {
        if (instance == null) {
            instance = new SheetsController(1);
        }
        return instance;
    }

    /**
     * Get the singleton instance of SheetsController with a specified last row.
     * 
     * @param lastRow The last row number to be set in the controller.
     * @return The singleton instance of SheetsController.
     */
    public static synchronized SheetsController getInstance(int lastRow) {
        if (instance == null) {
            instance = new SheetsController(lastRow);
        } else {
            instance.latestRow = lastRow; // Update the latest row if instance already exists
        }
        return instance;
    }

    /**
     * Initializes the SheetsController by connecting to the Google Sheets API.
     * 
     * @throws SheetsNotFoundException If the Sheets service is not found.
     * @throws GeneralSecurityException If there is a security issue while connecting to the API.
     * @throws IOException If there is an error while connecting to the API.
     */
    public void initialize(Sheets sheetsService) {
        this.sheetsService = sheetsService; // Set the Sheets service instance
    }

    /**
     * Connects to the Google Sheets API using the provided spreadsheet ID.
     * 
     * @param spreadsheetId The ID of the Google Sheets spreadsheet to be accessed.
     */
    public synchronized void connectToSheets(String spreadsheetId) {
        
        this.spreadsheetId = spreadsheetId; // Set the spreadsheet ID
        
        // Get the first sheet name dynamically
        try {
            sheetName = sheetsService.spreadsheets()
                .get(spreadsheetId)
                .execute()
                .getSheets()
                .get(0)
                .getProperties()
                .getTitle();
        } catch (Exception e) {
            LOGGER.error("Error retrieving sheet name: " + e.getMessage());
            e.printStackTrace();
        }
        LOGGER.info("Connected to Google Sheets with ID: " + spreadsheetId);
    }

    /**
     * Validates the provided spreadsheet ID.
     * Checks if the ID is not null or empty, matches the expected format,
     * and can be accessed via the Google Sheets API.
     * 
     * @param spreadsheetId The ID of the Google Sheets spreadsheet to be validated.
     * @return true if the spreadsheet ID is valid, false otherwise.
     */
    public boolean isValidSpreadsheetId(String spreadsheetId) {
        
        // Check if the spreadsheet ID is a valid Google Sheets ID
        try {
            sheetsService.spreadsheets()
                .get(spreadsheetId)
                .execute();
            LOGGER.info("Valid spreadsheet ID: " + spreadsheetId);
            return true; // Valid ID
        } catch (Exception e) {
            LOGGER.info("Invalid spreadsheet ID: " + spreadsheetId);
            LOGGER.debug("Error validating spreadsheet ID: " + e.getMessage());
            return false; // Invalid ID
        }
    }
    
    /**
     * Checks if the SheetsController is initialized.
     * 
     * @return true if the SheetsController is initialized, false otherwise.
     */
    public boolean isInitialized() {
        return sheetsService != null;
    }

    /**
     * Checks if the SheetsController is connected to a Google Sheets spreadsheet.
     * 
     * @return true if connected, false otherwise.
     */
    public synchronized boolean isConnected() {
        return sheetsService != null && spreadsheetId != null && !spreadsheetId.isEmpty();
    }

    /**
     * Retrieves the spreadsheet ID.
     * 
     * @return The ID of the Google Sheets spreadsheet.
     */
    public synchronized String getSheetsId() {
        return spreadsheetId;
    }

    /**
     * Retrieves the name of the first sheet in the spreadsheet.
     * 
     * @return The name of the first sheet in the spreadsheet.
     */
    public synchronized String getSheetName() {
        return sheetName;
    }

    /**
     * Retrieves a message from the specified row in the spreadsheet.
     * 
     * @param row The row number from which to retrieve the message.
     * @return A Message object containing the data from the specified row, or null if an error occurs.
     */
    public Message getMessage(int row) throws SheetsNotFoundException {

        if (sheetsService == null) {
            LOGGER.error("Sheets service is not initialized.");
            throw new SheetsNotFoundException("Sheets service is not initialized.");
        }

        try {
            // Always use the first sheet dynamically
            String sheetName = sheetsService.spreadsheets()
                .get(spreadsheetId)
                .execute()
                .getSheets()
                .get(0)
                .getProperties()
                .getTitle();

            // Define the range of cells to retrieve
            String range = sheetName + "!A" + row + ":E" + row; // A to E columns of the specified row

            List<List<Object>> values = sheetsService.spreadsheets().values()
                .get(spreadsheetId, range)
                .execute()
                .getValues();

            if (values != null && !values.isEmpty()) {
                List<Object> rowValues = values.get(0);

                // Create a new Message object with the retrieved values
                System.out.println("Row values: " + rowValues);
                if (rowValues.size() < 5) {
                    System.err.println("Row values do not contain enough data: " + rowValues.size());
                    return null;
                }

                return new Message(
                    (String) rowValues.get(0),  // Timestamp
                    (String) rowValues.get(1),  // Subject
                    (String) rowValues.get(2),  // Body
                    (String) rowValues.get(3),  // Recipient
                    (String) rowValues.get(4)   // Author
                );
            }
        } catch (Exception e) {
            LOGGER.error("Error retrieving message: " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Retrieves the latest row number from the spreadsheet.
     * 
     * @return The latest row number in the spreadsheet.
     */
    public int getLatestRow() {
        return latestRow;
    }

    /**
     * Gets the number of rows in the spreadsheet.
     * 
     * @return The number of rows in the spreadsheet.
     * @throws SheetsNotFoundException If the Sheets service is not initialized.
     */
    public int getNumRows() throws SheetsNotFoundException {
        if (sheetsService == null) {
            LOGGER.error("Sheets service is not initialized.");
            throw new SheetsNotFoundException("Sheets service is not initialized.");
        } 

        try {
            // Define the range to get the number of rows
            String range = sheetName + "!A:E"; // A to E columns

            List<List<Object>> values = sheetsService.spreadsheets().values()
                .get(spreadsheetId, range)
                .execute()
                .getValues();

            if (values != null) {
                return values.size(); // Return the number of rows
            }

        } catch (Exception e) {
            LOGGER.error("Error retrieving number of rows: " + e.getMessage());
            e.printStackTrace();
        }
        return 0; // Return 0 in case of an error
    }

    /**
     * Retrieves new messages from the spreadsheet starting from the latest row.
     * 
     * @return A list of Message objects containing the new messages.
     * @throws SheetsNotFoundException If the Sheets service is not initialized.
     */
    public synchronized List<Message> getNewMessages() throws SheetsNotFoundException {
        if (sheetsService == null) {
            LOGGER.error("Sheets service is not initialized.");
            throw new SheetsNotFoundException("Sheets service is not initialized.");
        }

        try {
            // Get the number of rows in the spreadsheet
            int numRows = getNumRows();
            int newLastRow = 0 ;

            // Retrieve messages from the spreadsheet start from the latest row
            List<Message> messages = new ArrayList<>();
            for (int row = latestRow + 1; row <= numRows; row++) {
                Message message = getMessage(row);
                if (message != null) {
                    messages.add(message); // Add the message to the list if it is not null
                }
                newLastRow = row; // Update the new last row number
            }

            // Update the latest row number in the controller
            if (messages.size() > 0) {
                latestRow = newLastRow; // Update the latest row number to the last retrieved row
                LOGGER.info("New messages retrieved: " + messages.size() + ", latest row updated to: " + latestRow);
            } else {
                LOGGER.info("No new messages found.");
            }
            return messages;

        } catch (Exception e) {
            LOGGER.error("Error retrieving new messages: " + e.getMessage());
            e.printStackTrace();
        }
        return Collections.emptyList(); // Return an empty list in case of an error
    }

    public int checkNewMessages() throws SheetsNotFoundException {
        if (sheetsService == null) {
            LOGGER.error("Sheets service is not initialized.");
            throw new SheetsNotFoundException("Sheets service is not initialized.");
        }

        // Check if the latest row is less than the number of rows in the spreadsheet
        int numRows = getNumRows();

        if (latestRow < numRows) {
            LOGGER.info("New messages available. Latest row: " + latestRow + ", Total rows: " + numRows);
            return numRows - latestRow; // Return the number of new messages
        } else {
            LOGGER.info("No new messages available. Latest row: " + latestRow + ", Total rows: " + numRows);
            return 0; // No new messages
        }
    }
}
