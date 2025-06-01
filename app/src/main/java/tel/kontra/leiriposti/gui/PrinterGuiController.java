package tel.kontra.leiriposti.gui;

import javax.print.DocFlavor;
import javax.print.PrintService;
import javax.print.attribute.PrintServiceAttributeSet;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;

import tel.kontra.leiriposti.controller.PrinterController;
import tel.kontra.leiriposti.model.PrintersNotFoundException;

/**
 * PrinterGuiController class is responsible for managing the printer GUI.
 * It handles the display of available printers, their attributes, and allows the user to select a printer.
 * 
 * @version 1.0
 * @since 0.1
 * 
 * @author Markus
 */
public class PrinterGuiController {

    private static final Logger LOGGER = LogManager.getLogger(); // Logger for debugging

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
        LOGGER.debug("Find printers button clicked.");
        populatePrinterTree(); // Repopulate the printer tree
    }

    /**
     * Event handler for the "Apply" button.
     */
    @FXML
    private void onApply() {
        LOGGER.debug("Apply button clicked.");
        String selectedPrinter = printerSelection.getValue();

        if (selectedPrinter != null) {
            LOGGER.debug("Selected printer: " + selectedPrinter);
            // Add logic to apply the selected printer

            PrinterController printerController = PrinterController.getInstance();

            // Set the default print service based on the selected printer name
            try {
                printerController.setPrintServiceByName(selectedPrinter);
            } catch (PrintersNotFoundException e) {
                LOGGER.error("Printer not found: " + selectedPrinter, e);
                selectPrinterLabel.setText("Error: Printer not found.");
                return; // Exit the method if the printer is not found
            } catch (Exception e) {
                LOGGER.error("An error occurred while setting the printer: " + selectedPrinter, e);
                selectPrinterLabel.setText("Error: Unable to set printer.");
                return; // Exit the method if any other error occurs
            }

            // INFO: I've just realized that the error for setting
            // a non-existing printer is caught only when printing

            // Trigger MainGui to update the selected printer
            LOGGER.info("Printer selection applied: " + selectedPrinter);

        } else {
            LOGGER.warn("No printer selected.");
        }
    }

    /**
     * Initialize the controller.
     * This method is called after the FXML file is loaded.
     */
    @FXML
    private void initialize() {
        LOGGER.debug("PrinterGuiController initialized.");

        // Initialize the printer selection choice box
        // Set a default prompt text for the choice box
        PrinterController printerController = PrinterController.getInstance();
        String defaultPrinterName = printerController.getDefaultPrintServiceName();

        if (defaultPrinterName != null) {
            printerSelection.setValue(defaultPrinterName); // Set the prompt text to show the selected printer
        }

        // Set a root node for the TreeView
        TreeItem<String> rootItem = new TreeItem<>("Available Printers");
        rootItem.setExpanded(true); // Expand the root node by default
        printerTree.setRoot(rootItem);

        // Populate the printer tree with available print services
        populatePrinterTree();
    }

    /**
     * Populate the printer tree with available print services and their attributes.
     * This method runs in a separate thread to avoid blocking the UI thread.
     * 
     * This is also bit of a hacky function, as it does multiple things at once:
     * 1. It populates the printer tree with available print services.
     * 2. It adds the printer names to the choice box for selection.
     * 
     * This is not best practice, but it works and really everything doesn't need to be
     * separated into smaller methods for this simple task.
     */
    private void populatePrinterTree() {
        // Clear the existing tree items
        printerTree.getRoot().getChildren().clear();

        // Get the available print services
        PrinterController printerController = PrinterController.getInstance();
        PrintService[] printServices = printerController.getPrintServices();

        // Add the printers to the choice box for selection
        printerSelection.getItems().clear(); // Clear existing items
        
        /**
        * Run the for loop in parallel to avoid blocking the UI thread
        * Quick and dirty way to run the for loop in parallel
        * This is not the best practice, but it works for this case as I cant be assed
        * to implement the javaFX concurrency API for this simple task
        *
        * Note: This turned out not to be that simple after all, but here we are.
        */
        new Thread(() -> {
            for (PrintService service : printServices) {
                // Add the printer name to the choice box
            printerSelection.getItems().add(service.getName()); // Add the printer name to the choice box

            // Create a tree item for the printer
            TreeItem<String> printerItem = new TreeItem<>(service.getName());
            printerTree.getRoot().getChildren().add(printerItem); // Add the printer item to the tree view

            // Add a header for doc flavors
            TreeItem<String> docFlavorHeader = new TreeItem<>("Doc Flavors");
            printerItem.getChildren().add(docFlavorHeader); // Add the doc flavor header to the printer item

            // Get supported doc flavors
            DocFlavor[] docFlavors = service.getSupportedDocFlavors();
            for (DocFlavor flavor : docFlavors) {
                String flavorString = flavor.getMimeType(); // Get the MIME type of the doc flavor
                
                // Ignore duplicate doc flavors
                if (docFlavorHeader.getChildren().stream().anyMatch(item -> item.getValue().equals(flavorString))) {
                    LOGGER.debug("Duplicate doc flavor ignored: " + flavorString);
                    continue; // Skip to the next iteration if the doc flavor already exists
                }

                // Add the doc flavor under the flavor header
                TreeItem<String> flavorItem = new TreeItem<>(flavorString);
                docFlavorHeader.getChildren().add(flavorItem); // Add the flavor item to the tree view

                // Log the doc flavor for debugging
                LOGGER.debug("Doc Flavor: " + flavorString);
            }

            // Add a header for attributes
            TreeItem<String> attributesHeader = new TreeItem<>("Attributes");
            printerItem.getChildren().add(attributesHeader); // Add the attributes header to the printer item

            // Get print service attribute set
            PrintServiceAttributeSet attributes = service.getAttributes();
            if (attributes != null) {
                for (Object attribute : attributes.toArray()) {
                    // Get the attribute name and value
                    String attributeName = attribute.getClass().getSimpleName(); // Get the class name of the attribute
                    String attributeValue = attribute.toString(); // Get the value of the attribute

                    // Add the attribute under the printer.attributes item
                    TreeItem<String> attributeItem = new TreeItem<>(attributeName + ": " + attributeValue);
                    attributesHeader.getChildren().add(attributeItem); // Add the attribute item to the tree view

                    // Log the attribute for debugging
                    LOGGER.debug("Attribute: " + attributeName + ", Value: " + attributeValue);
                }
            } else {
                LOGGER.warn("No attributes found for service: " + service.getName());
            }
            }
        }).start(); // Start the thread to populate the printer tree in parallel
    }
}
