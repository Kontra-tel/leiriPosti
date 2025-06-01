package tel.kontra.leiriposti.view;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class SheetIdMissingGui {

    public static void start(Stage initStage) throws Exception {
            // Load the Initialize GUI FXML file
            FXMLLoader loader = new FXMLLoader(SheetIdMissingGui.class.getResource("/sheetInit.fxml"));
            Parent root = loader.load();

            // Create a new stage for the Initialize GUI
            initStage.initOwner(MainGui.getPrimaryStage()); // Set the owner of the stage to the primary stage
            initStage.initModality(Modality.APPLICATION_MODAL);
            initStage.setResizable(false);
            initStage.setTitle("Initialize Spreadsheet ID");
            initStage.setScene(new Scene(root));
            initStage.showAndWait(); // Show the stage and wait for it to close
    }
}
