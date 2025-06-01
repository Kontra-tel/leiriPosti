package tel.kontra.leiriposti.service;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.GeneralSecurityException;
import java.util.Collections;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets; 
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.sheets.v4.SheetsScopes;
import com.google.api.client.http.javanet.NetHttpTransport;

/**
 * GoogleAuth is a utility class that handles the authentication process for accessing Google API.
 * The class provides a method to get the credentials needed for accessing the Google Sheets API.
 * It uses a local server receiver to handle the authorization code flow and stores the credentials in a local directory.
 * 
 * This is a modified version of the Google Sheets API Quickstart example.
 * The original example can be found at: https://developers.google.com/sheets/api/quickstart/java
 * 
 * This is not in anyway a safe way to store credentials, but it is not made to be a public app
 * so it is not a problem in this case.
 * 
 */
public class GoogleAuth {

    private static final Logger LOGGER = LogManager.getLogger(); // Logger for debugging

    private static final String TOKENS_DIRECTORY_PATH = "tokens";
    private static JsonFactory JsonFactory = GsonFactory.getDefaultInstance();

    // Credential scopes for Google Sheets API
    // We only need read access to the spreadsheet
    private static final List<String> SCOPES = 
        Collections.singletonList(SheetsScopes.SPREADSHEETS_READONLY);

    /**
     * Returns an authorized Credential object.
     * This method loads the client secrets from the credentials.json file,
     * builds the authorization flow, and triggers the user authorization request.
     * 
     * @param HTTP_TRANSPORT
     * @return Credential
     * @throws IOException
     * @throws GeneralSecurityException
     */
    public static Credential getCredentials(final NetHttpTransport HTTP_TRANSPORT) 
        throws IOException, GeneralSecurityException {
        
        InputStream in = GoogleAuth.class.getResourceAsStream("/credentials.json");

        // Exit if the credentials file is not found
        // This file should be in the resources directory of the project
        if (in == null) {
            LOGGER.error("Credentials file not found. Please ensure credentials.json is in the resources directory.");
            System.exit(1);
        }

        // Load client secrets from the credentials file
        GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(
            JsonFactory, new InputStreamReader(in)
        );

        // Build flow and trigger user authorization request
        GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
            HTTP_TRANSPORT, JsonFactory, clientSecrets, SCOPES)
            .setDataStoreFactory(new FileDataStoreFactory(new java.io.File(TOKENS_DIRECTORY_PATH)))
            .setAccessType("offline")
            .build();
        
        // Authorize the user and get the credentials
        LocalServerReceiver receiver = new LocalServerReceiver.Builder().setPort(8888).build();
        return new AuthorizationCodeInstalledApp(flow, receiver).authorize("user");
    }
}
