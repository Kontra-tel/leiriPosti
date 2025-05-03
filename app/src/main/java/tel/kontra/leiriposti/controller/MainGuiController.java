package tel.kontra.leiriposti.controller;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.MenuItem;
import javafx.stage.Modality;
import javafx.stage.Stage;

import tel.kontra.leiriposti.view.PrinterUpdateEvent;

public class MainGuiController {
    
    private static final Logger LOGGER = LogManager.getLogger(); // Logger for debugging

    private PrinterController printerController = PrinterController.getInstance(); // Instance of PrinterController

    @FXML
    private ListView<String> messageList; // ListView for displaying messages

    @FXML
    private ChoiceBox<String> showMessageChoice; // ChoiceBox for filtering messages

    @FXML
    private Button printSelected; // Button for printing the selected message

    @FXML
    private Button printAll; // Button for printing all new messages

    @FXML
    private Button clearAllMsgs; // Button for clearing all messages

    @FXML
    private Button delAllMsgs; // Button for deleting all messages

    @FXML
    private Label printerInfo; // Label for displaying printer information

    @FXML
    private Label sheetsInfo; // Label for displaying Google Sheets information

    @FXML
    private Label menubarMessage; // Label for displaying a message in the menu bar

    @FXML
    private MenuItem onPrinterSettings; // Menu item for printer settings

    @FXML
    private MenuItem onGoogleSettings; // Menu item for Google settings

    @FXML
    private MenuItem onAbout; // Menu item for the About dialog


    // Event handler for printing the selected message
    @FXML
    private void onPrintSelected() {
        System.out.println("Print selected message clicked.");
        // Add logic to handle printing the selected message
    }

    // Event handler for printing all new messages
    @FXML
    private void onPrintAll() {
        System.out.println("Print all new messages clicked.");
        // Add logic to handle printing all new messages
    }

    // Event handler for clearing all messages
    @FXML
    private void onClearAllMsg() {
        System.out.println("Clear all messages clicked.");
        // Add logic to handle clearing all messages
    }

    // Event handler for deleting all messages
    @FXML
    private void onDelAllMsgs() {
        System.out.println("Delete all messages clicked.");
        // Add logic to handle deleting all messages
    }

    // Event handler for opening Google settings
    @FXML
    private void onGoogleSettings() {
        System.out.println("Google settings clicked.");
        // Add logic to handle opening Google settings
    }

    // Event handler for opening the About dialog
    @FXML
    private void onAbout() {
        LOGGER.debug("onAbout()", LOGGER);
        // Add logic to handle opening the About dialog
    }

    // Event handler for opening the Printer GUI
    @FXML
    private void onPrinterSettings() {
        try {
            // Load the printer.fxml file
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/printer.fxml"));
            Parent root = loader.load();

            // Create a new stage for the Printer GUI
            Stage printerStage = new Stage();
            printerStage.initModality(Modality.APPLICATION_MODAL);
            printerStage.setResizable(false);
            printerStage.setTitle("Printer Settings");
            printerStage.setScene(new Scene(root));
            printerStage.showAndWait(); // Wait for the stage to close before continuing

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

    @FXML
    private void initialize() {
        // Initialize the GUI components and set up event handlers
        LOGGER.debug("MainGuiController initialized.");
        
    }
}
