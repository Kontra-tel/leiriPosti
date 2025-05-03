package tel.kontra.leiriposti;

import java.awt.print.PrinterException;

import javax.print.PrintService;

import tel.kontra.leiriposti.controller.PrinterController;
import tel.kontra.leiriposti.controller.SheetsController;
import tel.kontra.leiriposti.model.Message;
import tel.kontra.leiriposti.model.PrintersNotFoundException;
import tel.kontra.leiriposti.view.MainGui;

public class App {

    public static void main(String[] args) {
        System.out.println("Hello, Leiriposti!");

        //String sheetId = "1YvVGb-Zs83-ar92oJeyDbgPqyHcLe3BUDI6tQdGZYDc";

        //SheetsController sc = new SheetsController(sheetId);
        //PrinterController pc = new PrinterController();

        // Print all printservices
        //PrintService[] printServices = pc.getPrintServices();

        //for (PrintService printService : printServices) {
        //    System.out.println(printService.getName());
        //}
        //
        //Message msg = sc.getMessage(2);

        //System.out.println(msg);

        /// Set printer to last one in the list if the array is not empty
        //if (printServices.length > 0) {
        //    pc.setDefaultPrintService(printServices[printServices.length - 1]);
        //} else {
        //    System.err.println("No print services available.");
        //}

        // Print the message
        //try {
        //    pc.sendToPrinter(msg);
        //} catch (PrintersNotFoundException e) {
        //    System.err.println("No printers found: " + e.getMessage());
        //} catch (PrinterException e) {
        //    System.err.println("Printer error: " + e.getMessage());
        //}

        //Launch the GUI        
        MainGui.launch(MainGui.class, args);

    }
}
