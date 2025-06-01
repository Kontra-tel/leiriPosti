package tel.kontra.leiriposti.view;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import tel.kontra.leiriposti.controller.MessageController;
import tel.kontra.leiriposti.controller.PrinterController;
import tel.kontra.leiriposti.controller.SessionProfileController;
import tel.kontra.leiriposti.controller.SheetsController;
import tel.kontra.leiriposti.model.Message;
import tel.kontra.leiriposti.model.MessageStatus;
import tel.kontra.leiriposti.model.SessionProfile;
import tel.kontra.leiriposti.model.PrintersNotFoundException;
import tel.kontra.leiriposti.service.GoogleAuth;
import tel.kontra.leiriposti.service.GoogleServiceFactory;
import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.services.sheets.v4.Sheets;
import tel.kontra.leiriposti.gui.MainGuiController;

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
    public static boolean isDebug = false; // Debug mode flag
    public static boolean isDebug() { return isDebug; }

    /**
     * The main entry point for the application.
     * It initializes the MainGui and displays the main window.
     *
     * @param primaryStage The primary stage for this application, onto which the application scene can be set.
     * @throws Exception If there is an error loading the FXML file or starting the application.
     */
    @Override
    public void start(Stage primaryStage) throws Exception {

        // --- Controller and Service Setup ---
        // Get session profile
        SessionProfileController sessionProfileController = SessionProfileController.getInstance();
        SessionProfile session = sessionProfileController.getSessionProfile();

        // Google API setup
        NetHttpTransport httpTransport = new NetHttpTransport();
        Credential credentials = GoogleAuth.getCredentials(httpTransport);
        if (credentials == null) {
            LOGGER.error("Credentials are null. Exiting application.");
            System.exit(1);
        }
        GoogleServiceFactory googleServiceFactory = GoogleServiceFactory.getInstance(credentials);
        Sheets sheetsService = googleServiceFactory.getSheetsService();

        /**
         * SheetsController setup.
         * This controller manages the interaction with Google Sheets,
         */
        int lastRow = session.getLastRow();
        SheetsController sheetsController = (lastRow > 0)
            ? SheetsController.getInstance(lastRow)
            : SheetsController.getInstance();

        // If spreadsheetId is missing, open the GUI for setting it up
        if (session.getSpreadsheetId() == null || session.getSpreadsheetId().isEmpty()) {
            LOGGER.warn("Spreadsheet ID is missing. Opening setup dialog.");
            SheetIdMissingGui.start(new Stage());
            // After dialog, reload the session in case it was updated
            session = sessionProfileController.getSessionProfile();
            if (session.getSpreadsheetId() == null || session.getSpreadsheetId().isEmpty()) {
                LOGGER.error("Spreadsheet ID is still missing after setup dialog. Exiting application.");
                System.exit(1);
            }
        }

        sheetsController.initialize(sheetsService);
        sheetsController.connectToSheets(session.getSpreadsheetId());

        /**
         * MessageController setup.
         * This controller manages the messages imported from Google Sheets.
         */
        List<Message> messages = session.getImportedMessages();
        if (messages == null || messages.isEmpty()) {
            messages = new ArrayList<>();
        }
        MessageController messageController = MessageController.getInstance(sheetsController, messages);

        /**
         * PrinterController setup.
         * This controller manages the printing functionality of the application.
         */
        PrinterController printerController = PrinterController.getInstance();
        try {
            printerController.setPrintServiceByName(session.getSelectedPrinter());
        } catch (PrintersNotFoundException e) {
            // Optionally show error modal here
        }

        // Update printQueue with QUEUED messages
        Queue<Message> printQueue = session.getImportedMessages().stream()
        .filter(message -> message.getStatus() == MessageStatus.QUEUED)
        .collect(Collectors.toCollection(LinkedList::new));
        printerController.setPrintQueue(printQueue);
        
        // --- End Controller and Service Setup ---
        
        // Load FXML and get controller
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/main.fxml"));
        Parent root = loader.load();
        MainGuiController controller = loader.getController();
        
        // Inject dependencies into controller
        controller.setSessionProfileController(sessionProfileController);
        controller.setSheetsController(sheetsController);
        controller.setPrinterController(printerController);
        controller.setMessageController(messageController);
        controller.setSessionProfile(session);
        controller.postInit();
        
        Scene scene = new Scene(root);
        primaryStage.setResizable(false);
        primaryStage.setTitle("Leiriposti");
        primaryStage.setScene(scene);
        mainStage = primaryStage; // Set mainStage BEFORE show()
        primaryStage.show();

        // If --debug open a console GUI
        String[] args = getParameters().getRaw().toArray(new String[0]);
        if( args.length > 0 && args[0].equals("--debug")) {
            LOGGER.debug("Debug mode enabled. Opening console GUI...");
            consoleStage = new Stage();
            ConsoleGui.start(consoleStage);
            MainGui.isDebug = true; // Enable debug mode in the application
        } else {
            LOGGER.info("Starting main GUI without debug mode.");
        }

        // Update program title with session name and current weekday
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

        mainStage.getScene().getRoot().setDisable(true); // Disable the main GUI to prevent user interaction during cleanup

        Platform.runLater(() -> {
            LOGGER.debug("Stopping message polling...");
            MainGuiController controller = (MainGuiController) mainStage.getScene().getRoot().getUserData();
            if (controller != null) {
                controller.stopPolling(); // Stop message polling in the controller
            } else {
                LOGGER.warn("MainGuiController is not set. Cannot stop message polling.");
            }
        });

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
        // sessionProfile.setPrintQueue(printerController.getPrintQueue()); // Update the print queue
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

        LOGGER.info("Cleanup completed. Application is closing.");

        // Exit the application
        Platform.exit(); // Exit the JavaFX application
        System.exit(0); // Ensure the application exits completely
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
