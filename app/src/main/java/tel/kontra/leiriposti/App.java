package tel.kontra.leiriposti;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import tel.kontra.leiriposti.view.MainGui;

public class App {

    private static final Logger LOGGER = LogManager.getLogger(); // Logger for debugging

    public static void main(String[] args) {
        LOGGER.info("Hello, Leiriposti!");

        //Launch the GUI        
        MainGui.launch(MainGui.class, args);
    }
}
