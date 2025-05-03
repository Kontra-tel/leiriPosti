package tel.kontra.leiriposti.model;

/**
 * SheetsNotFoundException means that no sheets were found in from the Google Sheets API.
 *
 * @since 0.1
 */
public class SheetsNotFoundException extends Exception {
    public SheetsNotFoundException(String message) {
        super(message); // Call the constructor of the superclass (Exception)
    }
}
