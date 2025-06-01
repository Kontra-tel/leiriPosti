package tel.kontra.leiriposti.controller;

import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;
import java.util.LinkedList;
import java.util.Queue;

import javax.print.DocFlavor;
import javax.print.DocPrintJob;
import javax.print.PrintException;
import javax.print.PrintService;
import javax.print.SimpleDoc;
import javax.print.attribute.Attribute;
import javax.print.attribute.HashPrintRequestAttributeSet;
import javax.print.attribute.PrintRequestAttributeSet;
import javax.print.attribute.standard.Copies;
import javax.print.attribute.standard.Sides;

import org.apache.logging.log4j.Logger;

import javafx.application.Platform;
import javafx.scene.control.ProgressBar;

import org.apache.logging.log4j.LogManager;

import tel.kontra.leiriposti.event.EventBus;
import tel.kontra.leiriposti.event.PrintingCompleteEvent;
import tel.kontra.leiriposti.model.Message;
import tel.kontra.leiriposti.model.MessageStatus;
import tel.kontra.leiriposti.model.PrintJobWatcher;
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
    private ProgressBar progressBar; // Progress bar for printing status
    private Boolean isPaused = false; // Flag to indicate if printing is paused
    
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
    public void doPrint(ProgressBar progressBar) throws PrintersNotFoundException {
        this.progressBar = progressBar; // Set the progress bar for printing status

        if (printQueue == null || printQueue.isEmpty()) {
            LOGGER.warn("Print queue is empty!"); // Log a warning if the print queue is empty
            return; // Exit if there are no messages to print
        }

        if (defaultPrintService == null) {
            LOGGER.error("No default print service set!"); // Log an error if no default print service is set
            throw new PrintersNotFoundException("No default print service set!"); // Throw exception if no default print service is set
        }

        isPaused = false; // Set printing to active
        LOGGER.debug("Starting printing process..."); // Log the start of the printing process

        // Start the print thread to process the print queue
        if( printThread == null || !printThread.isAlive()) {
            printThread = new Thread(printProcess); // Create a new thread for printing
            printThread.start(); // Start the print thread
            LOGGER.debug("Print thread started."); // Log the start of the print thread
        } else {
            LOGGER.warn("Print thread is already running!"); // Log a warning if the print thread is already running
        }
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
     * Thread used for printing messages.
     * This thread processes the print queue and sends messages to the printer.
     * It runs in a loop until the queue is empty or printing is paused.
     */
    private Thread printThread; // Thread for printing messages

    /**
     * Runnable for processing the print queue.
     * This runnable processes messages in the print queue and sends them to the printer.
     * It handles the printing process, including creating print jobs and monitoring their completion.
     */
    private Runnable printProcess = () -> {
        while (!printQueue.isEmpty() && !isPaused) { // Continue processing while the queue is not empty and printing is not paused
            Message message = printQueue.poll(); // Get the next message from the print queue
            
            DocPrintJob printJob = defaultPrintService.createPrintJob(); // Create a print job from the default print service
            PrintRequestAttributeSet pras = getPras(defaultPrintService); // Get the PrintRequestAttributeSet for the print service
            PrintableMessage printableMessage = new PrintableMessage(message); // Create a PrintableMessage from the message

            PrintJobWatcher watcher = new PrintJobWatcher(printJob); // Create a PrintJobWatcher to monitor the print job
            
            try {
                printJob.print(new SimpleDoc(printableMessage, DocFlavor.SERVICE_FORMATTED.PRINTABLE, null), pras); // Send the printable message to the printer
                LOGGER.debug("Sent message to printer: " + message.getSubject()); // Log the sending of the message to the printer
                
                message.setStatus(MessageStatus.PRINTING); // Set the status of the message to PRINTING
                
                // Update the progress bar on the JavaFX Application Thread
                Platform.runLater(() -> {
                    if (progressBar != null) {
                        progressBar.setProgress((double) (printQueue.size() + 1) / printQueue.size()); // Update the progress bar
                    }
                });

                // Make the printThread wait until the print job is completed
                watcher.waitForDone();
                LOGGER.debug("Print job completed for message: " + message.getSubject()); // Log the completion of the print job

            } catch (PrintException e) {
                LOGGER.error("Failed to print message: " + message.getSubject(), e); // Log an error if printing fails
            } catch (InterruptedException e) {
                LOGGER.error("Print job interrupted for message: " + message.getSubject(), e); // Log an error if the print job is interrupted
            } finally {
                // Set the status of the message to PRINTED after printing
                message.setStatus(MessageStatus.PRINTED);
            }
        }
        // Notify listeners that printing is complete
        Platform.runLater(() -> {
            PrintingCompleteEvent event = new PrintingCompleteEvent("");
            EventBus.getInstance().post(event); // Post the PrintingCompleteEvent to the EventBus
        });

        // Destroy the print thread after processing the queue
        printThread = null; // Set the print thread to null to indicate that it is no longer running
    };

    /**
     * Get the PrintRequestAttributeSet for the given print service.
     * This method creates a PrintRequestAttributeSet with default attributes
     * such as number of copies and print side.
     * 
     * @param printService The print service for which to get the attributes.
     * @return PrintRequestAttributeSet with default attributes.
     */
    private PrintRequestAttributeSet getPras(PrintService printService) {
        PrintRequestAttributeSet pras = new HashPrintRequestAttributeSet(); // Create a new PrintRequestAttributeSet
        pras.add(new Copies(1)); // Set the number of copies to 1
        pras.add(Sides.ONE_SIDED); // Set the print side to one-sided

        // Check if the print service supports duplex printing
        if (printService.getAttributes().containsValue(Sides.DUPLEX)) {
            pras.add(Sides.DUPLEX); // Add duplex printing if supported
        }
        return pras; // Return the PrintRequestAttributeSet
    }

    /**
     * Remove a message from the print queue.
     * 
     * This method removes a message from the print queue if it exists.
     * It sets the status of the message to NOT_PRINTED after removal.
     * 
     * @param message The message to remove from the print queue.
     */
    public void removeFromPrintQueue(Message message) {
        if (printQueue != null && printQueue.contains(message)) {
            printQueue.remove(message); // Remove the message from the print queue
            message.setStatus(MessageStatus.NOT_PRINTED); // Set the status of the message to DELETED
            LOGGER.debug("Removed message from print queue: " + message.getSubject()); // Log the removal of the message
        } else {
            LOGGER.warn("Message not found in print queue: " + message.getSubject()); // Log if the message is not found in the queue
        }
    }
}
