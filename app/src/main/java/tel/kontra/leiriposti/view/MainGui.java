package tel.kontra.leiriposti.view;

import java.time.LocalDate;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import tel.kontra.leiriposti.controller.MessageController;
import tel.kontra.leiriposti.controller.PrinterController;
import tel.kontra.leiriposti.controller.SessionProfileController;
import tel.kontra.leiriposti.controller.SheetsController;
import tel.kontra.leiriposti.model.SessionProfile;

/**
 * MainGui class is responsible for launching the main GUI of the application.
 * It initializes the primary stage and loads the FXML file for the main interface.
 * 
 * @version 1.0
 * @since 0.1
 * 
 * @author Markus
 */
public class MainGui extends Application {
    
    private static final Logger LOGGER = LogManager.getLogger(); // Logger for debugging
    private static Stage mainStage;
    private static Stage consoleStage;

    /**
     * The main entry point for the application.
     * It initializes the MainGui and displays the main window.
     *
     * @param primaryStage The primary stage for this application, onto which the application scene can be set.
     * @throws Exception If there is an error loading the FXML file or starting the application.
     */
    @Override
    public void start(Stage primaryStage) throws Exception {
        Parent root = FXMLLoader.load(getClass().getResource("/main.fxml"));
        Scene scene = new Scene(root);
        primaryStage.setResizable(false); // Disable resizing of the window
        primaryStage.setTitle("Leiriposti");
        primaryStage.setScene(scene);
        primaryStage.show();
        mainStage = primaryStage; // Store the primary stage for later use

        // If --debug open a console GUI
        String[] args = getParameters().getRaw().toArray(new String[0]);

        if( args.length > 0 && args[0].equals("--debug")) {
            LOGGER.debug("Debug mode enabled. Opening console GUI...");
            consoleStage = new Stage(); // Create a new stage for the console GUI
            ConsoleGui.start(consoleStage); // Open the console GUI if --debug is passed
        } else {
            LOGGER.info("Starting main GUI without debug mode.");
        }

        // Update program title with session name and current weekday
        SessionProfile session = SessionProfileController.getInstance().getSessionProfile(); // Get the current session profile
        // Set MainStage title to include session name and weekday
        String dayOfWeek = LocalDate.now().getDayOfWeek().toString();
        primaryStage.setTitle("Leiriposti - " + session.getSessionName() + " - " + dayOfWeek);
    }
    
    /**
     * Does cleanup when the application is closed.
     * This method is called when the application is about to close.
     * It performs necessary cleanup operations such as saving the session profile,
     * updating the last row number, selected printer, print queue, and imported messages.
     */
    @Override
    public void stop() throws Exception {
        LOGGER.info("Application is closing. Performing cleanup...");

        /**
        * Controller instances used in the MainGui.
        * These controllers are responsible for managing different aspects of the application,
        * such as session profiles, printing, sheets, and messages.
        */
        SessionProfileController sessionProfileController = SessionProfileController.getInstance();
        PrinterController printerController = PrinterController.getInstance();
        SheetsController sheetsController = SheetsController.getInstance();
        MessageController messageController = MessageController.getInstance();

        // Update the session profile with the current state
        SessionProfile sessionProfile = sessionProfileController.getSessionProfile();
        sessionProfile.setLastRow(sheetsController.getLatestRow()); // Update the last row number
        sessionProfile.setSelectedPrinter(printerController.getDefaultPrintServiceName()); // Update the selected printer
        sessionProfile.setPrintQueue(printerController.getPrintQueue()); // Update the print queue
        sessionProfile.setImportedMessages(messageController.getMessages()); // Update the imported messages
        sessionProfile.setSpreadsheetId(sheetsController.getSheetsId()); // Update the spreadsheet ID

        // Lastly save the session profiles
        LOGGER.info("Saving session profiles...");
        sessionProfileController.saveSessionProfiles();

        // Close the console stage if it was opened
        if (consoleStage != null) {
            LOGGER.debug("Closing console GUI...");
            consoleStage.close(); // Close the console GUI if it was opened
        }
    }

    /**
     * Returns the primary stage of the application.
     * This method provides access to the primary stage,
     * which can be used in other parts of the application for various purposes.
     */
    public static Stage getPrimaryStage() {
        return mainStage; // Return the primary stage for use in other parts of the application
    }
}
