package tel.kontra.leiriposti.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.IOException;

import org.junit.jupiter.api.Test;

public class PropertiesControllerTest {

    // Test values
    private String testKey = "app.name";
    private String testValue = "Leiriposti";

    @Test
    void testGetInstance() {
        // Test if the singleton instance is created correctly
        PropertiesController instance1 = PropertiesController.getInstance();
        PropertiesController instance2 = PropertiesController.getInstance();
        assertSame(instance1, instance2, "Instances should be the same");
    }

    @Test
    void testGetProperty() {
        // Test if the properties are loaded correctly
        PropertiesController instance = PropertiesController.getInstance();
        String propertyValue = instance.getProperty(testKey);
        assertEquals(testValue, propertyValue, "Property value should match expected value");
    }

    @Test
    void testSetProperty() {
        // Test if the properties are set correctly
        PropertiesController instance = PropertiesController.getInstance();
        instance.setProperty(testKey, testValue);
        String propertyValue = instance.getProperty(testKey);
        assertEquals(testValue, propertyValue, "Property value should match expected value after set");
    }

    @Test
    void testReloadProperties() {
        // Test if the properties are reloaded correctly
        PropertiesController instance = PropertiesController.getInstance();
        try {
            instance.reloadProperties();
        } catch (IOException e) {
            fail("Failed to reload properties: " + e.getMessage());
        }
        String propertyValue = instance.getProperty(testKey);
        assertEquals(testValue, propertyValue, "Property value should match expected value after reload");
    }

    @Test
    void testSaveProperties() {
        // Test if the properties are saved correctly
        PropertiesController instance = PropertiesController.getInstance();
        instance.setProperty(testKey, testValue);
        try {
            instance.saveProperties();
        } catch (IOException e) {
            fail("Failed to save properties: " + e.getMessage());
        }
        String propertyValue = instance.getProperty(testKey);
        assertEquals(testValue, propertyValue, "Property value should match expected value after save");
    }
}
