package tel.kontra.leiriposti.model;

import java.util.List;
import java.util.Queue;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.FileOutputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

import lombok.Builder;
import lombok.Data;

import tel.kontra.leiriposti.controller.PropertiesController;
import tel.kontra.leiriposti.util.HashAlgorithm;
import tel.kontra.leiriposti.util.HashUtil;

/**
 * Represents a session profile in the application.
 * This class is used to store session-specific data such as selected printer,
 * imported messages, and other session-related information.
 * 
 * @version 1.0
 * @since 0.2
 */
@Data
@Builder
public class SessionProfile implements Serializable {
    
    private static final Logger LOGGER = LogManager.getLogger(); // Logger for debugging
    
    // Classes for handling hashing
    private static final PropertiesController pC = PropertiesController.getInstance();
    private static final HashUtil hashUtil = new HashUtil(pC.getProperty("hash.salt"));

    // Session ID generation
    // Using SHA-256 to hash the current time in milliseconds, truncated to 8 characters
    private final String sessionId = hashUtil.hash(
        String.valueOf(System.currentTimeMillis()),
        HashAlgorithm.SHA_256,
        8 // Truncate the session ID to 8 characters 
    ); // Unique session ID based on current time

    private String sessionName; // Name of the session profile
    private int lastRow; // Last row number in the Google Sheets spreadsheet
    private String selectedPrinter; // Name of the selected printer
    private Queue<Message> printQueue; // Queue of messages to be printed in this session
    private List<Message> importedMessages; // All messages imported in this session
    private boolean isDefault; // Flag to indicate if this is the default session profile

    /**
     * Google Sheets service data.
     * This is used to interact with Google Sheets API for retrieving messages.
     */
    private String spreadsheetId; // ID of the Google Sheets spreadsheet

    /**
     * Saves the current session profile to a file in the session_profiles directory.
     * The session profile is serialized and saved to a file named with the session ID.
     */
    public void save() {
        
        try {
            // Create the session_profiles directory if it doesn't exist
            File dir = new File("session_profiles");
            if (!dir.exists()) {
                dir.mkdirs();
            }

            String fileName = !isDefault ? sessionId : "default_session"; // Use sessionId or default name if not set

            // Serialize the session profile to a file
            try (FileOutputStream fileOut = new FileOutputStream("session_profiles/" + fileName + ".ser");
                 ObjectOutputStream out = new ObjectOutputStream(fileOut)) {
                out.writeObject(this);
                LOGGER.info("Session profile saved successfully: " + sessionId);
            }
        } catch (Exception e) {
            LOGGER.error("Error saving session profile: " + e.getMessage(), e);
        }
    }
}
