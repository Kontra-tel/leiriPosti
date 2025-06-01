package tel.kontra.leiriposti.gui;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;

import tel.kontra.leiriposti.controller.SessionProfileController;
import tel.kontra.leiriposti.controller.SheetsController;

/**
 * Controller for the Initialize GUI.
 * This controller handles the initialization of the GUI, including input validation
 * for a Google Sheets ID and providing feedback to the user.
 * 
 * @version 1.0
 * @since 0.2
 * 
 * @author Markus
 */
public class SheetIdMissingGuiController {

    private static final Logger LOGGER = LogManager.getLogger(); // Logger for debugging

    private SessionProfileController sessionProfileController = SessionProfileController.getInstance();
    private SheetsController sheetsController = SheetsController.getInstance(); // Get the SheetsController instance

    @FXML
    private TextField idValue;

    @FXML
    private Button continueBtn;

    @FXML
    private void onValidate() {
        String id = idValue.getText();

        boolean valid = sheetsController.isValidSpreadsheetId(id); // Validate the spreadsheet ID using SheetsController

        continueBtn.setDisable(!valid);
        if (valid) {
            continueBtn.setTooltip(new Tooltip("Continue with this spreadsheet ID"));
        } else {
            continueBtn.setTooltip(new Tooltip("Invalid spreadsheet ID"));
        }
    }

    @FXML
    private void onHelp() {
        // Open help page or show help dialog
        // For demonstration, just print to console
        LOGGER.info("Help requested for spreadsheet ID input.");
    }

    @FXML
    private void onContinue() {
        String id = idValue.getText();
        if (id != null && !id.isEmpty()) {
            // Save the spreadsheet ID, close the window, or proceed to next step
            LOGGER.info("Continuing with spreadsheet ID: " + id);

            sessionProfileController.getSessionProfile().setSpreadsheetId(id); // Set the spreadsheet ID in the session profile
        }
    }
}
