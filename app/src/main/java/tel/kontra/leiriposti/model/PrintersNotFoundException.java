package tel.kontra.leiriposti.model;

/**
 * PrintersNotFoundException means that no printers were found in the system.
 *
 * @since 0.1
 */
public class PrintersNotFoundException extends Exception {
    public PrintersNotFoundException(String message) {
        super(message); // Call the constructor of the superclass (Exception)
    }
}
