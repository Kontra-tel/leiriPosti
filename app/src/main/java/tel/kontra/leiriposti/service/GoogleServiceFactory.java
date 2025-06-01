package tel.kontra.leiriposti.service;

import java.io.IOException;
import java.security.GeneralSecurityException;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.sheets.v4.Sheets;

/**
 * GoogleServiceFactory is a singleton class that provides access to Google API services.
 * It initializes the necessary services such as Google Sheets API and provides methods
 * to retrieve these services.
 * 
 * This class ensures that only one instance of the Google API services is created,
 * which helps in managing resources efficiently and avoids multiple credential requests.
 * 
 * @version 1.0
 * @since 0.2
 */
public class GoogleServiceFactory {

    private static final String APPLICATION_NAME = "Leiriposti";
    private static GoogleServiceFactory instance; // Singleton instance
    
    // Services for Google APIs
    private Sheets sheetsService; // Sheets

    /**
     * Private constructor to prevent instantiation.
     * Initializes the Google Sheets service.
     * 
     * @throws GeneralSecurityException if there is a security error during initialization
     * @throws IOException if there is an I/O error during initialization
     */
    private GoogleServiceFactory(Credential credentials) throws GeneralSecurityException, IOException {

        // Initialize the HTTP transport
        NetHttpTransport httpTransport = GoogleNetHttpTransport.newTrustedTransport();

        // Sheets API service
        this.sheetsService = new Sheets.Builder(
            httpTransport, GsonFactory.getDefaultInstance(), credentials)
                .setApplicationName(APPLICATION_NAME)
                .build();

        /**
        * If response is 400 invalid grant, it means the credentials are invalid or expired.
        * In this case, we should handle the exception and prompt the user to re-authenticate.
        */
    }

    /**
     * Returns the singleton instance of GoogleServiceFactory.
     * 
     * This is a thread-safe method that ensures only one instance of the class is created
     * to avoid multiple credential requests and to maintain a single point of access to the APIs.
     * 
     * @return GoogleServiceFactory instance
     * @throws GeneralSecurityException if there is a security error during initialization
     * @throws IOException if there is an I/O error during initialization
     */
    public static synchronized GoogleServiceFactory getInstance(Credential credentials) throws GeneralSecurityException, IOException {
        if (instance == null) {
            instance = new GoogleServiceFactory(credentials);
        }
        return instance;
    }

    /**
     * Returns the Forms API service client.
     * 
     * @return Sheets service client
     */
    public Sheets getSheetsService() {
        return sheetsService;
    }
}
