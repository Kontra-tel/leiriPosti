package tel.kontra.leiriposti.controller;

import java.io.File;
import java.io.FileInputStream;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import tel.kontra.leiriposti.model.SessionProfile;

/**
 * Controller for managing session profiles in the application.
 * 
 * This class is a singleton that provides access to the current session profile
 * and allows for setting and retrieving session profiles.
 * 
 * It is used to persist session-specific data such as selected printer,
 * imported messages, and other session-related information.
 * 
 * @version 0.1
 * @since 0.2
 * 
 * @author Markus
 */
public class SessionProfileController {
    
    private static final Logger LOGGER = LogManager.getLogger(); // Logger for debugging

    private static SessionProfileController instance; // Singleton instance
    private SessionProfile sessionProfile; // Current session profile
    private SessionProfile defaultSessionProfile; // Default session profile to be opened by default
    private List<SessionProfile> sessionProfiles = new ArrayList<>(); // List of session profiles

    /**
     * Private constructor to prevent instantiation.
     * Initializes the session profile.
     */
    private SessionProfileController() {
        loadSessionProfiles(); // Load existing session profiles from persistent storage

        // Create a default session profile if none exist
        if (sessionProfiles.isEmpty()) {
            // Create a default session profile if none is set
            sessionProfile = SessionProfile.builder()
                .sessionName("Default Session")
                .lastRow(0)
                .selectedPrinter(null)
                .importedMessages(new ArrayList<>())
                .printQueue(new LinkedList<>())
                .spreadsheetId(null)
                .isDefault(true) // Mark this as the default session profile
                .build();
            sessionProfiles.add(sessionProfile); // Add the default session profile to the list
        }

        // Set default session profile if it exists
        defaultSessionProfile = sessionProfile;

        LOGGER.info("SessionProfileController initialized with " + sessionProfiles.size() + " profiles loaded.");
    }

    /**
     * Get the singleton instance of SessionProfileController.
     * 
     * @return The singleton instance of SessionProfileController.
     */
    public static synchronized SessionProfileController getInstance() {
        if (instance == null) {
            instance = new SessionProfileController(); // Create new instance if not already created
        }
        return instance; // Return the singleton instance
    }

    /**
     * Get the current session profile.
     * 
     * @return The current session profile.
     */
    public SessionProfile getSessionProfile() {
        return sessionProfile; // Return the current session profile
    }

    /**
     * Set the current session profile.
     * 
     * @param sessionProfile The session profile to set.
     */
    public void setSessionProfile(SessionProfile sessionProfile) {
        this.sessionProfile = sessionProfile; // Set the current session profile
    }
    
    /**
     * Add a session profile to the list of session profiles.
     * 
     * @param sessionProfile The session profile to add.
     */
    public void addSessionProfile(SessionProfile sessionProfile) {
        this.sessionProfiles.add(sessionProfile); // Add the session profile to the list
    }

    /**
     * Get the list of session profiles.
     * 
     * @return The list of session profiles.
     */
    public List<SessionProfile> getSessionProfiles() {
        return sessionProfiles; // Return the list of session profiles
    }

    /**
     * Get the default session profile.
     * 
     * @return The default session profile.
     */
    public SessionProfile getDefaultSessionProfile() {
        return defaultSessionProfile; // Return the default session profile
    }

    /**
     * Set the default session profile.
     * 
     * @param defaultSessionProfile The session profile to set as default.
     */
    public void setDefaultSessionProfile(SessionProfile defaultSessionProfile) {

        this.defaultSessionProfile.setDefault(false); // Set the current default session profile to not default
        defaultSessionProfile.setDefault(true); // Set the new default session profile to default

        this.defaultSessionProfile = defaultSessionProfile; // Set the default session profile
        
        // If the default session profile is not already in the list, add it
        if (!sessionProfiles.contains(defaultSessionProfile)) {
            sessionProfiles.add(defaultSessionProfile); // Add the default session profile to the list
        }
        LOGGER.info("Default session profile set to: " + defaultSessionProfile.getSessionName());
    }

    /**
     * Save all session profiles to persistent storage.
     * This method iterates through the list of session profiles and saves each one.
     */
    public void saveSessionProfiles() {
        for(SessionProfile profile : sessionProfiles) {
            try {
                profile.save(); // Save each session profile
            } catch (Exception e) {
                LOGGER.error("Error saving session profile: " + e.getMessage(), e); // Log error if saving fails
            }
        }
    }

    /**
     * Load session profiles from persistent storage.
     * This method reads session profile files from the "session_profiles" directory
     * and populates the list of session profiles.
     */
    private void loadSessionProfiles() {
        sessionProfiles.clear();
        File dir = new File("session_profiles");
        if (dir.exists() && dir.isDirectory()) {
            File[] files = dir.listFiles((d, name) -> name.endsWith(".ser"));
            if (files != null) {
                for (File file : files) {
                    try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file))) {
                        SessionProfile profile = (SessionProfile) ois.readObject();
                        sessionProfiles.add(profile);

                        // Check if this is the default session profile
                        if (profile.isDefault()) {
                            sessionProfile = profile; // Set the default session profile
                        }

                    } catch (Exception e) {
                        LOGGER.error("Error loading session profile from file: " + file.getName(), e);
                    }
                }
            }
        }
    }
}
