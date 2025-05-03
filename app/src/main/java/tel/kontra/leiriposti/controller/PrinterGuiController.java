package tel.kontra.leiriposti.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.TreeView;

public class PrinterGuiController {

    @FXML
    private TreeView<String> printerTree; // TreeView for displaying printer hierarchy

    @FXML
    private ChoiceBox<String> printerSelection; // ChoiceBox for selecting a printer

    @FXML
    private Button applySelection; // Button to apply the selected printer

    @FXML
    private Button findPrinters; // Button to find available printers

    @FXML
    private Label selectPrinterLabel; // Label for the printer selection section

    /**
     * Event handler for the "Find Printers" button.
     */
    @FXML
    private void onFindPrinters() {
        System.out.println("Find printers button clicked.");
        // Add logic to find and populate the list of available printers
    }

    /**
     * Event handler for the "Apply" button.
     */
    @FXML
    private void onApply() {
        System.out.println("Apply button clicked.");
        String selectedPrinter = printerSelection.getValue();
        if (selectedPrinter != null) {
            System.out.println("Selected printer: " + selectedPrinter);
            // Add logic to apply the selected printer
        } else {
            System.out.println("No printer selected.");
        }
    }

    /**
     * Initialize the controller.
     * This method is called after the FXML file is loaded.
     */
    @FXML
    private void initialize() {
        System.out.println("PrinterGuiController initialized.");
        // Add initialization logic, such as populating the ChoiceBox or TreeView

        
    }
}
