package tel.kontra.leiriposti.controller;

import tel.kontra.leiriposti.model.Message;
import tel.kontra.leiriposti.service.GoogleServiceFactory;

import java.util.List;

import com.google.api.services.sheets.v4.Sheets;


public class SheetsController {
    
    private Sheets sheetsService; // Sheets API service
    private String spreadsheetId; // Spreadsheet ID

    /**
     * Constructor for SheetsController.
     * Initializes the Sheets API service and sets the spreadsheet ID.
     * 
     * @param spreadsheetId The ID of the Google Sheets spreadsheet to be accessed.
     */
    public SheetsController(String spreadsheetId) {
        
        // Get the Sheets API service from the GoogleServiceFactory
        try {
            this.sheetsService = GoogleServiceFactory.getInstance().getSheetsService();
        } catch (Exception e) {
            System.err.println("Error initializing SheetsController: " + e.getMessage());
            e.printStackTrace();
        }

        this.spreadsheetId = spreadsheetId; // Set the spreadsheet ID
    }

    public Message getMessage(int row) {

        // Get the message from the specified row in the spreadsheet
        try {

            // We will always use the first sheet so get it dynamically
            String sheetName = sheetsService.spreadsheets()
                .get(spreadsheetId)
                .execute().getSheets()
                    .get(0).getProperties().getTitle();

            // Define the range of cells to retrieve
            String range = sheetName + "!A" + row + ":E" + row; // A to E columns of the specified row

            List<List<Object>> values = sheetsService.spreadsheets().values()
                .get(spreadsheetId, range) // Get the values from the specified range
                .execute()
                .getValues();

            if (values != null && !values.isEmpty()) {
                List<Object> rowValues = values.get(0); // Get the first row of values

                // Create a new Message object with the retrieved values
                System.out.println("Row values: " + rowValues);
                if (rowValues.size() < 5) {
                    System.err.println("Row values do not contain enough data: " + rowValues.size());
                    return null; // Not enough data to create a Message object
                }

                return new Message(
                    (String) rowValues.get(0),  // Timestamp
                    (String) rowValues.get(1), // Subject
                    (String) rowValues.get(2), // Body
                    (String) rowValues.get(3), // Recipient
                    (String) rowValues.get(4)   // Author
                );
            }
        } catch (Exception e) {
            System.err.println("Error retrieving message: " + e.getMessage());
            e.printStackTrace();
        }

        return null; // Return null if no message is found or an error occurs
    }

}
