package tel.kontra.leiriposti.view;

import javafx.event.Event;
import javafx.event.EventType;

public class PrinterUpdateEvent extends Event {

    public static final EventType<PrinterUpdateEvent> PRINTER_UPDATE = new EventType<>(Event.ANY, "PRINTER_UPDATE_EVENT");

    private String printerName = null; // Name of the printer to be updated

    public PrinterUpdateEvent(String printerName) {
        super(PRINTER_UPDATE);
        this.printerName = printerName; // Set the printer name for the event
    }

    public String getPrinterName() {
        return printerName; // Return the name of the printer
    }
}
