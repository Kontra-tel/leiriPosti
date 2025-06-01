package tel.kontra.leiriposti;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.config.Configurator;

import tel.kontra.leiriposti.view.MainGui;

public class App {

    private static final Logger LOGGER = LogManager.getLogger(); // Logger for debugging

    public static void main(String[] args) {
        LOGGER.info("Hello, Leiriposti!");

        // If the application is run with the --debug flag, enable console
        if( args.length > 0 && args[0].equals("--debug")) {
            Configurator.setRootLevel(org.apache.logging.log4j.Level.DEBUG);
            LOGGER.debug("Debug mode enabled.");
        } else {
            Configurator.setRootLevel(org.apache.logging.log4j.Level.INFO);
        }

        //Launch the GUI        
        MainGui.launch(MainGui.class, args);
    }
}
