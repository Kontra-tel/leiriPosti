package tel.kontra.leiriposti.controller;

import tel.kontra.leiriposti.model.Message;
import tel.kontra.leiriposti.model.SheetsNotFoundException;
import tel.kontra.leiriposti.service.GoogleServiceFactory;

import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.api.services.sheets.v4.Sheets;

/**
 * SheetsController class is responsible for managing the Google Sheets API service.
 * It provides methods to retrieve messages from a Google Sheets spreadsheet.
 * 
 * Im not happy with this way of doing things as it is not very flexible as everything is hardcoded.
 * I might change this in the future to possibly use a database or create the sheets and forms dynamically.
 * 
 * @version 0.5
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

    /**
     * Private constructor for SheetsController.
     * Initializes the Sheets API service and sets the spreadsheet ID.
     * 
     * @param spreadsheetId The ID of the Google Sheets spreadsheet to be accessed.
     */
    private SheetsController() {
    }

    /**
     * Get the singleton instance of SheetsController.
     * 
     * @param spreadsheetId The ID of the Google Sheets spreadsheet to be accessed.
     * @return The singleton instance of SheetsController.
     */
    public static synchronized SheetsController getInstance() {
        if (instance == null) {
            instance = new SheetsController();
        }
        return instance;
    }

    /**
     * Connects to the Google Sheets API using the provided spreadsheet ID.
     * 
     * @param spreadsheetId The ID of the Google Sheets spreadsheet to be accessed.
     */
    public void connectToSheets(String spreadsheetId) {
        
        // Check if service is already initialized
        if (sheetsService != null) {
            sheetsService = null; // Reset the service if already initialized
            LOGGER.info("Sheets service reset.");
        }

        this.spreadsheetId = spreadsheetId; // Set the spreadsheet ID
    
        try {
            sheetsService = GoogleServiceFactory.getInstance().getSheetsService(); // Initialize the Sheets API service
        } catch (Exception e) {
            System.err.println("Error connecting to Google Sheets: " + e.getMessage());
            e.printStackTrace();
        }
        
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
     * Retrieves the spreadsheet ID.
     * 
     * @return The ID of the Google Sheets spreadsheet.
     */
    public String getSheetsId() {
        return spreadsheetId;
    }

    /**
     * Retrieves the name of the first sheet in the spreadsheet.
     * 
     * @return The name of the first sheet in the spreadsheet.
     */
    public String getSheetName() {
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
}
