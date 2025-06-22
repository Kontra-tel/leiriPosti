package tel.kontra.leiriposti.model;

import java.util.Date;

import javax.print.DocPrintJob;
import javax.print.PrintService;
import javax.print.attribute.standard.QueuedJobCount;
import javax.print.event.PrintJobAdapter;
import javax.print.event.PrintJobEvent;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * PrintJobWatcher is a utility class that listens for print job events and allows
 * waiting for the completion of a print job.
 * It uses the PrintJobAdapter to handle various print job events such as completion,
 * failure, cancellation, and no more events.
 *
 * This class can be used to synchronize the printing process in applications that
 * require confirmation of print job completion before proceeding with other tasks.
 *
 * @version 1.0
 * @since 0.2
 */
public class PrintJobWatcher extends PrintJobAdapter {
    private boolean doneFlag = false;
    private Date startTime;
    private static final Logger LOGGER = LogManager.getLogger();

    /**
     * Constructs a PrintJobWatcher for the specified DocPrintJob.
     * It registers listeners for print job events to track the completion status.
     *
     * @param job The DocPrintJob to watch for events.
     */
    public PrintJobWatcher(DocPrintJob job) {

        // Get start time
        startTime = new Date();

        job.addPrintJobListener(new PrintJobAdapter() {
            @Override
            public void printJobCompleted(PrintJobEvent pje) {
                complete();
            }
            @Override
            public void printJobFailed(PrintJobEvent pje) {
                complete();
            }
            @Override
            public void printJobCanceled(PrintJobEvent pje) {
                complete();
            }
            @Override
            public void printJobNoMoreEvents(PrintJobEvent pje) {
                complete();
            }
        });
    }

    /**
     * Constructs a PrintJobWatcher for the specified PrintService.
     * It monitors the print service for job completion by checking the printer state.
     * This is used to ensure we have only one job queued at a time, which is a requirement
     * for the printing process in this application.
     *
     * @param printService The PrintService to watch for print job events.
     */
    public PrintJobWatcher(PrintService printService) {
        
        // Get start time
        startTime = new Date();

        // Monitor the print service for job completion
        new Thread(() -> {
            while (!doneFlag) {
                try {
                    Thread.sleep(2500); // Check every second
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt(); // Restore interrupted status
                }
                
                QueuedJobCount queuedJobCount = printService.getAttribute(QueuedJobCount.class);
                Integer jobCount = queuedJobCount != null ? queuedJobCount.getValue() : null;

                LOGGER.debug("Current queued job count: {}", jobCount);

                // If the printer is not busy and there are no queued jobs, mark as complete
                if (jobCount != null && jobCount == 0) {
                    complete();
                }
            }
        }).start();
    }

    /**
     * Marks the print job as completed.
     * This method is called when a print job event indicates that the job has finished,
     * either successfully or with an error.
     */
    private void complete() {
        synchronized (PrintJobWatcher.this) {
            doneFlag = true;
            PrintJobWatcher.this.notifyAll();
        }
    }

    /**
     * Waits for the print job to complete.
     * This method blocks until the print job is done or an error occurs.
     *
     * @return The time in milliseconds that the print job took to complete.
     * @throws InterruptedException if the current thread is interrupted while waiting.
     */
    public synchronized long waitForDone() throws InterruptedException {
        try {
            while (!doneFlag) {
                wait();
            }
            Date endTime = new Date();
            return endTime.getTime() - startTime.getTime();

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt(); // Restore interrupted status
            throw e; // Re-throw the exception
        }
    }
}