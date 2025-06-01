package tel.kontra.leiriposti.controller;

import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;
import java.util.LinkedList;
import java.util.Queue;

import javax.print.PrintService;
import javax.print.attribute.Attribute;
import javax.print.attribute.HashPrintRequestAttributeSet;
import javax.print.attribute.PrintRequestAttributeSet;
import javax.print.attribute.standard.Copies;
import javax.print.attribute.standard.Sides;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import tel.kontra.leiriposti.model.Message;
import tel.kontra.leiriposti.model.MessageStatus;
import tel.kontra.leiriposti.model.PrintableMessage;
import tel.kontra.leiriposti.model.PrintersNotFoundException;

/**
 * PrinterController class is responsible for managing print services and sending data to the printer.
 * It provides methods to get available print services, set the default print service, and send data to the printer.
 * 
 * Since version 1.5 of this class, this controller also handles a printing queue to avoid sending too many
 * print jobs to the printer at once.
 * 
 * This class is used to handle printing tasks in the application.
 * It allows the user to select a printer and send data to it for printing.
 * 
 * @version 2.0
 * @since 0.1
 * 
 * @author Markus
 */
public class PrinterController {

    private static final Logger LOGGER = LogManager.getLogger(); // Logger for debugging

    private static PrinterController instance; // Singleton instance

    /**
     * List of available print services.
     * 
     * This array holds the available print services that can be used to send data to the printer.
     * It is initialized in the constructor and can be accessed through the getPrintServices() method.
     */
    private PrintService[] printServices; // List of available print services
    private PrintService defaultPrintService; // Service in use

    /**
     * Queue for print jobs.
     * 
     * This queue holds PrintableMessage objects that are to be printed.
     * It is used to manage the print jobs and ensure that we dont flood the printer with too many jobs at once.
     */
    private Queue<Message> printQueue;

    private Boolean isPaused = false; // Flag to indicate if printing is paused

    /**
     * Runnable for printing thread.
     * 
     * This runnable is used to process the print queue and send messages to the printer.
     * It runs in a separate thread to avoid blocking the main application thread.
     */
    private Runnable printThread = new Runnable() {
        @Override
        public void run() {
            while (!printQueue.isEmpty() && !isPaused) { // Continue until the queue is empty or printing is paused
                Message data = printQueue.poll(); // Get the next message from the queue
                if (data != null) {
                    try {
                        sendToPrinter(data); // Send the message to the printer
                    } catch (PrintersNotFoundException | PrinterException e) {
                        LOGGER.error("Error sending message to printer: " + e.getMessage(), e); // Log any errors
                    }
                } else {
                    LOGGER.warn("No more messages in the print queue."); // Log if there are no more messages in the queue
                }
            }
            LOGGER.info("Print queue processing completed."); // Log when the print queue processing is completed
            isPaused = true; // Set the isPaused flag to true to indicate that printing is paused
        }
    };

    /**
     * Private constructor for PrinterController class.
     * It initializes the available print services and sets the default print service.
     * 
     * @throws PrintersNotFoundException If no printers are found.
     * @throws PrinterException If an error occurs while initializing the printer job.
     */
    private PrinterController() {
        // Get print services and default print service
        printServices = PrinterJob.lookupPrintServices(); // Get all available print services

        // Log to console for debugging
        LOGGER.info("Available print services:");
        for (PrintService service : printServices) {
            LOGGER.info(" - " + service.getName()); // Log the name of each service
        }

        // Get the default print service if available
        if (printServices.length > 0) {
            defaultPrintService = printServices[0]; // Set the first service as default
            LOGGER.debug("Default print service: " + defaultPrintService.getName()); // Log the name of the default service
        } else {
            LOGGER.warn("No print services found!"); // Log a warning message
        }
    }

    /**
     * Get the singleton instance of PrinterController.
     * 
     * @return The singleton instance.
     */
    public static synchronized PrinterController getInstance() {
        if (instance == null) {
            instance = new PrinterController();
        }
        return instance;
    }

    /**
     * Get the available print services.
     * 
     * @return The array of available print services.
     */
    public PrintService[] getPrintServices() {
        return printServices;
    }

    /**
     * Get the default printer name.
     * 
     * @return The name of the default print service.
     *         Returns null if no default print service is set.
     */
    public String getDefaultPrintServiceName() {
        if (defaultPrintService != null) {
            return defaultPrintService.getName(); // Return the name of the default print service
        } else {
            return null; // No default print service set
        }
    }

    /**
     * Set the default print service.
     * 
     * @param defaultPrintService The default print service to set.
     */
    public void setDefaultPrintService(PrintService defaultPrintService) {    
        this.defaultPrintService = defaultPrintService; // Set the default print service
    }

    /**
     * Set the default print service by name.
     * 
     * @param name The name of the print service to set as default.
     */
    public void setPrintServiceByName(String name) throws PrintersNotFoundException {
        for (PrintService service : printServices) {
            if (service.getName().equals(name)) {
                setDefaultPrintService(service); // Set the default print service by name
                break;
            }
        }

        // If no service with the given name is found, defaultPrintService remains null
        if (defaultPrintService == null) {
            throw new PrintersNotFoundException("No print service found with name: " + name + "."); // Throw exception if no service is found
        } else {
            LOGGER.info("Default print service set to: " + defaultPrintService.getName()); // Log the name of the new default print service
        }
    }

    /**
     * Print the information of the default print service.
     * 
     * This method prints the name and attributes of the default print service.
     */
    public void printServiceInfo() {
        if (defaultPrintService == null) {
            LOGGER.debug("No default print service set!"); // Log if no default print service is set
            return;
        } else {
            LOGGER.debug("Default print service: " + defaultPrintService.getName()); // Log the name of the default print service
            for (Attribute a : defaultPrintService.getAttributes().toArray()) {
                LOGGER.debug("* " + a.getName() + ": " + a); // Log each attribute of the default print service
            }
        }
    }

    /**
     * Set the print queue.
     * 
     * This method sets the print queue to the provided queue.
     * It is used to manage the print jobs that are to be printed.
     * 
     * @param printQueue The queue of messages to set as the print queue.
     */
    public void setPrintQueue(Queue<Message> printQueue) {
        this.printQueue = printQueue; // Set the print queue to the provided queue
        LOGGER.debug("Print queue set with " + printQueue.size() + " messages."); // Log the size of the print queue
    }

    /**
     * Add a message to the print queue.
     * 
     * This method adds a PrintableMessage to the print queue for later printing.
     * It initializes the print queue if it is null.
     * 
     * @param message The PrintableMessage to add to the print queue.
     */
    public void addToPrintQueue(Message message) {
        if (printQueue == null) {
            printQueue = new LinkedList<>(); // Initialize the print queue if it is null
        }

        // Set message status to "PRINTING"
        message.setStatus(MessageStatus.QUEUED); // Set the status of the message to QUEUED

        printQueue.add(message); // Add the message to the print queue
        LOGGER.debug("Added message to print queue: " + message.getSubject()); // Log the addition of the message to the queue
    }

    /**
     * Get the print queue.
     * 
     * This method returns the current print queue.
     * 
     * @return The current print queue.
     */
    public Queue<Message> getPrintQueue() {
        return printQueue; // Return the current print queue
    }

    /**
     * Start printing messages from the print queue.
     * 
     * This method starts a new thread to process the print queue and send messages to the printer.
     * It will continue to process messages until the queue is empty or printing is paused.
     * 
     * @return Success code indicating the status of the printing operation.
     */
    public void doPrint() {
        
        // Check if the print queue is empty
        if (printQueue == null || printQueue.isEmpty()) {
            LOGGER.info("Print queue is empty, nothing to print!"); // Log if the print queue is empty
            return; // Exit the method if there are no messages to print
        }
        
        // Set the isPaused flag to false to allow printing
        isPaused = false; // Reset the isPaused flag to allow printing

        // Start a new thread to process the print queue
        if (printThread instanceof Thread) {
            ((Thread) printThread).start(); // Start the print thread if it is an instance of Thread
        }

        LOGGER.info("Started printing from the queue, " + printQueue.size() + " messages in the queue.");
        LOGGER.info("Using thread: " + Thread.currentThread().getName()); // Log the thread name used for printing
    }

    /**
     * Pause the printing process.
     * 
     * This method sets the isPaused flag to true, which will stop the printing process when the current job is finished.
     */
    public void pausePrinting() {
        
        LOGGER.info("Pausing printing..."); // Log that printing is being paused

        // Wait for the current print job to finish
        if (printThread instanceof Thread) {
            try {
                ((Thread) printThread).join(); // Wait for the print thread to finish
            } catch (InterruptedException e) {
                LOGGER.error("Error while waiting for print thread to finish: " + e.getMessage(), e); // Log any errors
            }
        }

        // Set the isPaused flag to true to stop further printing
        isPaused = true; // Set the isPaused flag to true
        
        LOGGER.info("Printing paused."); // Log that printing has been paused
        
        return; // Exit the method after pausing printing
    }

    /**
     * Check if the printing process is currently active.
     * 
     * This method checks the isPaused flag to determine if printing is currently active.
     * 
     * @return true if printing is not paused, false otherwise.
     */
    public boolean isPrinting() {
        return !isPaused; // Return true if printing is not paused
    }

    /**
     * Send the next message in the print queue to the printer.
     * 
     * This method retrieves the next PrintableMessage from the print queue and sends it to the printer.
     * If the print queue is empty, it does nothing.
     * 
     * This method will make the current thread wait until the print job is completed.
     * this is done to ensure that we do not flood the printer with too many print jobs at once.
     * 
     * @throws PrintersNotFoundException If no printers are found.
     * @throws PrinterException If an error occurs while printing.
     */
    private void sendToPrinter(Message data) throws PrintersNotFoundException, PrinterException {

        // Set the status of the message to PRINTING
        data.setStatus(MessageStatus.PRINTING); // Set the status of the message to PRINTING

        // Set attributes for the print job
        PrintRequestAttributeSet pras = new HashPrintRequestAttributeSet();
        pras.add(new Copies(2)); // Set the number of copies to 2
        pras.add(Sides.DUPLEX);

        // Create a PrinterJob instance and set the printable object
        PrinterJob job = PrinterJob.getPrinterJob();
        job.setPrintable(new PrintableMessage(data));

        // Set the print service to the default one
        if (defaultPrintService != null) {
            job.setPrintService(defaultPrintService); // Set the print service to the default one
        } else {
            throw new PrintersNotFoundException("No printer found!"); // No default print service set
        }

        try {
            job.print(pras); // Print the job with the specified attributes
            LOGGER.info("Printing job: " + job.getJobName()); // Log the name of the print job
            LOGGER.info("Printing to: " + defaultPrintService.getName()); // Log the name of the print service used

            job.wait(); // Wait for the print job to complete
            data.setStatus(MessageStatus.PRINTED); // Set the status of the message to PRINTED after successful printing
            LOGGER.info("Print job completed successfully for message: " + data.getSubject()); // Log successful printing

        } catch (Exception e) {
            LOGGER.error("Error printing: " + e.getMessage(), e); // Log the error
        }
    }
}
