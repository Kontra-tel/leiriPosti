package tel.kontra.leiriposti.service;

import java.io.IOException;
import java.security.GeneralSecurityException;

import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.sheets.v4.Sheets;



public class GoogleServiceFactory {

    private static final String APPLICATION_NAME = "Leiriposti";
    private static GoogleServiceFactory instance; // Singleton instance
    
    // Services for Google APIs
    private Sheets  sheetsService; // Sheets

    private GoogleServiceFactory() throws GeneralSecurityException, IOException {

        // Initialize the services we need

        // HTTP transport
        // This is used to make HTTP requests to the Google APIs
        NetHttpTransport httpTransport = GoogleNetHttpTransport.newTrustedTransport();
        
        // Sheets API service
        this.sheetsService = new Sheets.Builder(
            httpTransport, GsonFactory.getDefaultInstance(), GoogleAuth.getCredentials(httpTransport))
                .setApplicationName(APPLICATION_NAME)
                .build();
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
    public static synchronized GoogleServiceFactory getInstance() throws GeneralSecurityException, IOException {
        if (instance == null) {
            instance = new GoogleServiceFactory();
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
