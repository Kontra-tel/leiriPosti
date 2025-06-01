package tel.kontra.leiriposti.view;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;

/**
 * PrinterGui class is responsible for displaying the printer GUI.
 * It loads the FXML file and sets up the primary stage for the printer interface.
 * 
 * @version 1.0
 * @since 0.2
 * 
 * @author Markus
 */
public class PrinterGui {

    /**
     * The main entry point for the Printer GUI application.
     * It initializes the PrinterGui and displays the printer settings window.
     *
     * @param printerStage The primary stage for this application, onto which the application scene can be set.
     * @throws Exception If there is an error loading the FXML file or starting the application.
     */
    public static void start(Stage printerStage) throws Exception {
        // Load the printer.fxml file
        FXMLLoader loader = new FXMLLoader(PrinterGui.class.getResource("/printer.fxml"));
        Parent root = loader.load();

        // Create a new stage for the Printer GUI
        printerStage.initOwner(MainGui.getPrimaryStage()); // Set the owner of the stage to the primary stage
        printerStage.initModality(Modality.APPLICATION_MODAL);
        printerStage.setResizable(false);
        printerStage.setTitle("Printer Settings");
        printerStage.setScene(new Scene(root));
        printerStage.showAndWait(); // Show the stage and wait for it to close
    }
}
