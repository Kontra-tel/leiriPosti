package tel.kontra.leiriposti.controller;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * PropertiesController is a utility class that handles loading and retrieving properties from a properties file.
 * It is used to manage application configuration settings.
 * 
 * The properties file is expected to be located in the resources directory of the project.
 * The class provides a method to get the value of a property by its key.
 * 
 * This class is thread-safe and uses the Singleton pattern to ensure a single instance.
 * 
 * @version 1.0
 * @since 0.1
 */
public class PropertiesController {

    private static final String propertiesFilePath = PropertiesController.class.getClassLoader().getResource("leiriposti.properties").getPath();
    private static PropertiesController instance; // Singleton instance
    private final Properties properties = new Properties();

    // Private constructor to prevent direct instantiation
    private PropertiesController() {
        // Load properties from file
        try (InputStream input = new FileInputStream(propertiesFilePath)) {
            properties.load(input);
        } catch (IOException ex) {
            System.err.println("Error loading properties file: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    /**
     * Get the singleton instance of PropertiesController.
     * 
     * @return The singleton instance.
     */
    public static synchronized PropertiesController getInstance() {
        if (instance == null) {
            instance = new PropertiesController();
        }
        return instance;
    }

    /**
     * Get the value of a property by its key.
     * 
     * @param key The key of the property to retrieve.
     * @return The value of the property, or null if not found.
     */
    public synchronized String getProperty(String key) {
        return properties.getProperty(key);
    }

    /**
     * Set the value of a property by its key.
     * 
     * @param key The key of the property to set.
     * @param value The value to set for the property.
     */
    public synchronized void setProperty(String key, String value) {
        properties.setProperty(key, value);
    }

    /**
     * Save the properties to the properties file.
     * 
     * @throws IOException If an error occurs while saving the properties.
     */
    public synchronized void saveProperties() throws IOException {
        try (FileOutputStream output = new FileOutputStream(propertiesFilePath)) {
            properties.store(output, null);
        }
    }

    /**
     * Reload the properties from the properties file.
     * 
     * @throws IOException If an error occurs while reloading the properties.
     */
    public synchronized void reloadProperties() throws IOException {
        try (InputStream input = new FileInputStream(propertiesFilePath)) {
            properties.load(input);
        }
    }

}