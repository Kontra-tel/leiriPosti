package tel.kontra.leiriposti.gui;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.services.sheets.v4.Sheets;

import io.opencensus.metrics.export.Value;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.SelectionMode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;

import tel.kontra.leiriposti.controller.MessageController;
import tel.kontra.leiriposti.controller.PrinterController;
import tel.kontra.leiriposti.controller.SessionProfileController;
import tel.kontra.leiriposti.controller.SheetsController;
import tel.kontra.leiriposti.model.Message;
import tel.kontra.leiriposti.model.MessageStatus;
import tel.kontra.leiriposti.model.PrintersNotFoundException;
import tel.kontra.leiriposti.model.SessionProfile;
import tel.kontra.leiriposti.model.SheetsNotFoundException;
import tel.kontra.leiriposti.service.GoogleAuth;
import tel.kontra.leiriposti.service.GoogleServiceFactory;
import tel.kontra.leiriposti.view.PrinterGui;
import tel.kontra.leiriposti.view.SheetIdMissingGui;
import tel.kontra.leiriposti.view.ValueUpdateEvent;

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
     * @see SessionProfileController
     * @see PrinterController
     * @see SheetsController
     * @see MessageController
     *
     * The controller are initialized in the MainGui class and injected into this controller.
     * This allows for better separation of concerns and makes the code more modular.
     */
    private SessionProfileController sessionProfileController;
    private PrinterController printerController;
    private SheetsController sheetsController;
    private MessageController messageController;

    // Add setters for dependency injection from MainGui
    public void setSessionProfileController(SessionProfileController c) { this.sessionProfileController = c; }
    public void setSheetsController(SheetsController c) { this.sheetsController = c; }
    public void setPrinterController(PrinterController c) { this.printerController = c; }
    public void setMessageController(MessageController c) { this.messageController = c; }
    public void setSessionProfile(SessionProfile s) { /* Optionally store session if needed */ }

    /**
     * FXML components for the Main GUI.
     * These components are defined in the FXML file and are linked to this controller.
     */
    @FXML
    private ListView<Message> messageList; // ListView for displaying messages

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
        printingBtn.setDisable(true); // Disable the button while fetching messages

        // Get messages from the MessageController
        new Thread(() -> {
            try {
                messageController.getNewMessages();
            } catch (SheetsNotFoundException e) {
                LOGGER.error("Error retrieving new messages: " + e.getMessage(), e);
                doErrorModal(e.getMessage(), "Error retrieving new messages");
                return; // Exit the method if there is an error
            }

            // Re-enable the button after fetching messages
            printingBtn.setDisable(false);
        }).start();

        // Render the message list with the current filter
        MessageStatus filter = showMessageChoice.getValue();
        renderMessageList(filter);
    }

    @FXML
    private void onFilterChange(ActionEvent event) {
        LOGGER.debug("onFilterChange()", LOGGER);
        
        // Get the selected filter from the ChoiceBox
        MessageStatus filter = showMessageChoice.getValue();
        
        // Render the message list with the selected filter
        renderMessageList(filter);
    }

    /**
     * When the user holds down the Shift key, change messageList
     * selection to multiple selection mode.
     * This allows the user to select multiple messages for actions like printing.
     */
    @FXML
    private void onShiftSelection(KeyEvent event) { 
        // Check if the Shift key is pressed
        if (event.isShiftDown()) {
            messageList.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        } else {
            messageList.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
        }
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
    private void initialize() {
        LOGGER.info("Initializing MainGuiController...");

        // Check if the SheetsController is initialized

        // Only UI setup and event handler binding here
        showMessageChoice.getItems().addAll(
            MessageStatus.ALL,
            MessageStatus.NOT_PRINTED,
            MessageStatus.QUEUED,
            MessageStatus.PRINTED,
            MessageStatus.ERROR
        );

        showMessageChoice.setValue(MessageStatus.ALL);
        showMessageChoice.setOnAction(this::onFilterChange);
        messageList.setOnKeyPressed(this::onShiftSelection);
        messageList.setOnMouseClicked(this::messageSelectionContextMenu);
    }

    /**
     * Call this after all dependencies are injected to finish setup.
     */
    public void postInit() {
        renderMessageList(MessageStatus.ALL);
    }

    /**
     * Polling thread for checking new messages in Google Sheets.
     * This thread runs in the background and checks for new messages every 5 seconds.
     */
    private boolean isPolling = false; // Flag to check if the polling thread is running
    private Thread pollThread = new Thread(() -> {
        LOGGER.info("Starting polling thread for new messages...");

        while (isPolling) {
            try {
                // Poll for new messages every 5 seconds
                Thread.sleep(5000); // Sleep for 5 seconds

                // Retrieve new message count from SheetsController
                int newMessageCount = sheetsController.checkNewMessages();

                if (newMessageCount > 0) {
                    LOGGER.info("New messages found: " + newMessageCount);
                    onMenuBarMessageUpdate(new ValueUpdateEvent("New messages found: " + newMessageCount)); // Fire an event to update the menubar message
                } else {
                    LOGGER.info("No new messages.");
                    onMenuBarMessageUpdate(new ValueUpdateEvent("No new messages")); // Fire an event to update the menubar message
                }

            } catch (InterruptedException e) {
                LOGGER.error("Polling thread interrupted: " + e.getMessage(), e);
                Thread.currentThread().interrupt(); // Restore the interrupted status
            } catch (SheetsNotFoundException e) {
                LOGGER.error("Error retrieving new messages: " + e.getMessage(), e);
            }
        }

        LOGGER.info("Polling thread stopped.");
    });

    /**
     * Handles the context menu for message selection in the ListView.
     * This method is triggered when the user right-clicks on a message in the ListView.
     * It displays a context menu with options to delete, add to queue, or remove from queue.
     * 
     * @param event The mouse event that triggered this method.
     */
    private ContextMenu contextMenu = new ContextMenu(); // Context menu for message selection

    /**
     * Handles the context menu for message selection in the ListView.
     * This method is triggered when the user right-clicks on a message in the ListView.
     * It displays a context menu with options to delete, add to queue, or remove from queue.
     * 
     * @param event
     */
    private void messageSelectionContextMenu(MouseEvent event) {
        if (event.getButton() == MouseButton.SECONDARY) { // Check if the right mouse button was clicked
            LOGGER.debug("Right-click detected on message list.");

            // Get the selected item or items from the ListView
            List<Message> selectedItems = messageList.getSelectionModel().getSelectedItems();
            if (!selectedItems.isEmpty()) {
                LOGGER.debug("Selected items: " + selectedItems);
                
                /**
                 * Context menu with options for the selected messages.
                 * DELETE, ADD TO QUEUE, REMOVE FROM QUEUE
                 */
                // Init the context menu if it is not already initialized
                if (contextMenu.getItems().isEmpty()) {
                    LOGGER.debug("Initializing context menu for message selection.");

                    // Delete menu item
                    MenuItem deleteItem = new MenuItem("Delete");
                    deleteItem.setOnAction(e -> {
                        for (Message selected : selectedItems) {
                            selected.setStatus(MessageStatus.DELETED); // Set the status to DELETED
                        }
                        renderMessageList(showMessageChoice.getValue());
                    });

                    // Add to Queue menu item
                    MenuItem addToQueueItem = new MenuItem("Add to Queue");
                    addToQueueItem.setOnAction(e -> {
                        for (Message selected : selectedItems) {
                            selected.setStatus(MessageStatus.QUEUED); // Set the status to QUEUED
                        }
                        renderMessageList(showMessageChoice.getValue());
                    });

                    // Remove from Queue menu item
                    MenuItem removeFromQueueItem = new MenuItem("Remove from Queue");
                    removeFromQueueItem.setOnAction(e -> {
                        for (Message selected : selectedItems) {
                            selected.setStatus(MessageStatus.NOT_PRINTED); // Set the status to NOT_PRINTED
                        }
                        renderMessageList(showMessageChoice.getValue());
                    });

                    contextMenu.getItems().addAll(removeFromQueueItem, addToQueueItem, deleteItem);
                    contextMenu.setAutoHide(true); // Automatically hide the context menu when an item is selected
                    contextMenu.setAutoFix(true); // Automatically fix the context menu position

                    // Clear the selection when the context menu is hidden
                    contextMenu.setOnHiding(e -> {
                        LOGGER.debug("Context menu hidden.");
                        messageList.getSelectionModel().clearSelection(); // Clear the selection when the context menu is hidden
                    });
                }
                LOGGER.debug("Showing context menu at mouse location: " + event.getScreenX() + ", " + event.getScreenY());
                // Show the context menu at the mouse location
                contextMenu.show(messageList, event.getScreenX(), event.getScreenY());
            }
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
            messageList.getItems().add(message);
        }
    }

    /**
     * Updates the menubar message with the new value from a ValueUpdateEvent.
     * This method is thread-safe and can be called from any thread.
     * @param event The ValueUpdateEvent containing the new value.
     */
    public void onMenuBarMessageUpdate(ValueUpdateEvent event) {
        Platform.runLater(() -> menubarMessage.setText(event.getValue()));
    }
}
