package tel.kontra.leiriposti.model;

import java.util.Date;

import javax.print.DocPrintJob;
import javax.print.event.PrintJobAdapter;
import javax.print.event.PrintJobEvent;

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

    private void complete() {
        synchronized (PrintJobWatcher.this) {
            doneFlag = true;
            PrintJobWatcher.this.notifyAll();
        }
    }

    public synchronized void waitForDone() throws InterruptedException {
        try {
            while (!doneFlag) {
                wait();
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt(); // Restore interrupted status
            throw e; // Re-throw the exception
        }
    }
}
