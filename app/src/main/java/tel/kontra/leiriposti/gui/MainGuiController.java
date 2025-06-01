package tel.kontra.leiriposti.gui;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.time.LocalDate;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.services.sheets.v4.Sheets;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.ProgressIndicator;
import javafx.stage.Stage;

import tel.kontra.leiriposti.controller.MessageController;
import tel.kontra.leiriposti.controller.PrinterController;
import tel.kontra.leiriposti.controller.SessionProfileController;
import tel.kontra.leiriposti.controller.SheetsController;
import tel.kontra.leiriposti.model.Message;
import tel.kontra.leiriposti.model.MessageStatus;
import tel.kontra.leiriposti.model.SessionProfile;
import tel.kontra.leiriposti.model.SheetsNotFoundException;
import tel.kontra.leiriposti.service.GoogleAuth;
import tel.kontra.leiriposti.service.GoogleServiceFactory;
import tel.kontra.leiriposti.view.MainGui;
import tel.kontra.leiriposti.view.PrinterGui;
import tel.kontra.leiriposti.view.SheetIdMissingGui;

/**
 * MainGuiController class is responsible for managing the main GUI of the application.
 * It handles user interactions, initializes controllers, and manages the state of the application.
 * 
 * @version 1.0
 * @since 0.1
 * 
 * @author Markus
 */
public class MainGuiController {

    // Logger
    private static final Logger LOGGER = LogManager.getLogger(); // Logger for debugging

    /**
     * Controllers used in the MainGuiController.
     * These controllers are responsible for managing different aspects of the application
     * 
     * The controllers are initialized in the initialize() method to ensure they are ready for use.
     */
    private SessionProfileController sessionProfileController;
    private PrinterController printerController;
    private SheetsController sheetsController;
    private MessageController messageController;

    /**
     * FXML components for the Main GUI.
     * These components are defined in the FXML file and are linked to this controller.
     */
    @FXML
    private ListView<String> messageList; // ListView for displaying messages

    @FXML
    private ChoiceBox<MessageStatus> showMessageChoice; // ChoiceBox for filtering messages

    @FXML
    private Label printerInfo; // Label for displaying printer information

    @FXML
    private Label sheetsInfo; // Label for displaying Google Sheets information

    @FXML
    private Label menubarMessage; // Label for displaying a message in the menu bar

    @FXML
    private Label printStatus; // Label for print status

    @FXML
    private Label latestMessage; // Label for latest message

    @FXML
    private Label queValue; // Label for messages in queue

    @FXML
    private ProgressBar printingProgressbar; // Progress bar for printing

    @FXML
    private ProgressIndicator messageProgress;

    @FXML
    private Button printingBtn; // Button to start printing

    // Event handler for opening the About dialog
    @FXML
    private void onAbout() {
        LOGGER.debug("onAbout()", LOGGER);
        // Add logic to handle opening the About dialog
    }

    @FXML
    private void doPrint() {
        LOGGER.debug("doPrint()", LOGGER);
        // Add logic to handle printing
        // This could involve calling methods from the PrinterController
        // and updating the print status label accordingly
    }

    @FXML
    private void onGetMessages() {
        LOGGER.debug("onGetMessages", LOGGER);
        
        // Get messages from the MessageController
        new Thread(() -> {

            // Set the progress indicator to indeterminate state
            messageProgress.setProgress(ProgressIndicator.INDETERMINATE_PROGRESS);
            messageProgress.setVisible(true);

            // Disable the button to prevent multiple clicks
            printingBtn.setDisable(true);

            try {
                messageController.getNewMessages();
            } catch (SheetsNotFoundException e) {
                LOGGER.error("Error retrieving new messages: " + e.getMessage(), e);
                doErrorModal(e.getMessage(), "Error retrieving new messages");
                return; // Exit the method if there is an error
            }

            // Re-enable the button after fetching messages
            printingBtn.setDisable(false);

            // Hide the progress indicator
            messageProgress.setVisible(false);
        }).start();

        // Render the message list with the current filter
        MessageStatus filter = showMessageChoice.getValue();
        renderMessageList(filter);
    }

    @FXML
    private void onFilterChnage() {
        LOGGER.debug("onFilterChange()", LOGGER);
        
        // Get the selected filter from the ChoiceBox
        MessageStatus filter = showMessageChoice.getValue();
        
        // Render the message list with the selected filter
        renderMessageList(filter);
    }

    @FXML
    private void onProfile() {
        LOGGER.debug("onProfile()", LOGGER);
        // Add logic to handle profile management
        // This could involve opening a profile management dialog or updating the session profile
    }

    // Event handler for opening the Printer GUI
    @FXML
    private void onPrinterSettings() {
        try {

            // Launch the Printer GUI
            LOGGER.debug("Opening Printer GUI...");

            PrinterGui.start(new Stage());

            // Get changes from the PrinterGuiController
            PrinterController pC = PrinterController.getInstance();
            SheetsController sC = SheetsController.getInstance();

            // Update the information labels
            printerInfo.setText("Printer - " + pC.getDefaultPrintServiceName());
            sheetsInfo.setText("Sheets - " + sC.getSheetName());

        } catch (Exception e) {
            LOGGER.error("Error loading printer settings GUI: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Initializes the MainGuiController.
     * This method is called by the JavaFX framework after the FXML file has been loaded.
     * It sets up the initial state of the GUI components and binds event handlers.
     * 
     * It also initializes other controllers like PrinterController and SheetsController
     * to ensure they are ready for use.
     * @throws GeneralSecurityException 
     * @throws IOException 
     */
    @FXML
    private void initialize() throws IOException, GeneralSecurityException {
        LOGGER.info("Initializing MainGuiController...");
        
        // Do google authentication
        NetHttpTransport httpTransport = new NetHttpTransport();
        Credential credentials = GoogleAuth.getCredentials(httpTransport);

        // Check if credentials are null
        if (credentials == null) {
            LOGGER.error("Credentials are null. Exiting application.");
            System.exit(1); // Exit the application if credentials are not available
        }

        GoogleServiceFactory googleServiceFactory = GoogleServiceFactory.getInstance(credentials);
        Sheets sheetsService = googleServiceFactory.getSheetsService();

        // Initialize SheetsController with the Google Sheets service
        sheetsController = SheetsController.getInstance();
        sheetsController.initialize(sheetsService);

        // Init controllers
        sessionProfileController = SessionProfileController.getInstance();
        SessionProfile session = sessionProfileController.getSessionProfile();
        LOGGER.info("Session Profile loaded: " + session.getSessionName());

        // Get messages from the session profile
        List<Message> messages = session.getImportedMessages();

        if (messages == null || messages.isEmpty()) {
            LOGGER.warn("No messages found in the session profile. Initializing with an empty list.");
            messages = new java.util.ArrayList<>(); // Use a mutable list!
        } else {
            LOGGER.info("Messages loaded from session profile: " + messages.size() + " messages.");
        }

        // Initialize MessageController
        messageController = MessageController.getInstance(sheetsController, messages);

        // Check if the session has a spreadsheet ID
        if (session.getSpreadsheetId() == null || session.getSpreadsheetId().isEmpty()) {
            
            // Open a dialog for the user to enter the spreadsheet ID
            LOGGER.warn("Spreadsheet ID is missing. Opening dialog to request it from the user.");
            onSpreadSheetMissing();

            // If the spreadsheet ID is missing exit application
            if( session.getSpreadsheetId() == null || session.getSpreadsheetId().isEmpty()) {
                LOGGER.error("Spreadsheet ID is still missing after dialog. Exiting application.");
                System.exit(1); // Exit the application if the spreadsheet ID is still missing
            }

        } else {
            LOGGER.info("Connecting to Google Sheets with ID: " + session.getSpreadsheetId());
        }

        // Initialize SheetsController with the session's spreadsheet ID
        sheetsController = SheetsController.getInstance();
        sheetsController.connectToSheets(session.getSpreadsheetId());

        // Printer
        printerController = PrinterController.getInstance();

        LOGGER.info("Connected to printer: " + printerController.getDefaultPrintServiceName()); // Log the default printer name
        
        // Update GUI labels with printer and sheets information
        printerInfo.setText("Printer - " + printerController.getDefaultPrintServiceName());
        sheetsInfo.setText("Sheets - " + sheetsController.getSheetName());

        /**
         * Populate showMessageChoice with options.
         * This ChoiceBox allows the user to filter messages based on their status.
         * 
         * @see MessageStatus
         */
        renderMessageList(MessageStatus.ALL); // Render the message list with all messages
    }

    /**
     * This method is called when the spreadsheet ID is missing.
     * It can be used to open a dialog or prompt the user for the spreadsheet ID.
     */
    private void onSpreadSheetMissing() {
        try {
            LOGGER.info("Opening Initialize GUI to request spreadsheet ID from the user.");

            // Start the Initialize GUI
            SheetIdMissingGui.start(new Stage());

        } catch (Exception e) {
            LOGGER.error("Error loading Initialize GUI: " + e.getMessage(), e);
        }
    }

    /**
     * Displays an error modal dialog with the specified message.
     * This method is used to show error messages to the user in a modal dialog.
     * 
     * @param message The error message to display in the dialog.
     */
    private void doErrorModal(String message, String title) {
        Alert alert = new Alert(javafx.scene.control.Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText("An error occurred");
        alert.setContentText(message);
        alert.showAndWait();
    }

    /**
     * Renders the message list in the GUI based on the selected filter.
     * This method retrieves messages from the MessageController and populates the ListView.
     * 
     * @param filter The filter to apply when retrieving messages (e.g., "All Messages", "Sent", "Received").
     */
    private void renderMessageList(MessageStatus filter) {
        LOGGER.debug("Rendering message list...");

        // Clear the current items in the ListView
        messageList.getItems().clear();

        // Get messages from the MessageController based on the filter
        List<Message> messages = messageController.getMessages(filter);
        LOGGER.debug("Number of messages to display: " + messages.size());

        // Populate the ListView with the filtered messages
        for (Message message : messages) {
            String displayText = "[" + message.getRecipient() + "] " + message.getSubject() + " - " + message.getStatus();
            messageList.getItems().add(displayText);
        }
    }

    /**
     * Event handlers for the GUI components.
     */
    // Event handler for updating labels in the GUI
    public EventHandler<ActionEvent> valueUpdateEvent = event -> {
        LOGGER.debug("Value update event triggered: " + event);

        // Update printer information label
        printerInfo.setText("Printer - " + printerController.getDefaultPrintServiceName());

        // Update sheets information label
        sheetsInfo.setText("Sheets - " + sheetsController.getSheetName());
    };
}
