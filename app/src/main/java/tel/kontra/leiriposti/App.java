package tel.kontra.leiriposti;

import tel.kontra.leiriposti.view.MainGui;

public class App {

    public static void main(String[] args) {
        System.out.println("Hello, Leiriposti!");

        //String sheetId = "1SPtpixQ5uZGS9u6u5XOfk6WWgvRTNsSvggl63GdR_zA";

        // Launch the GUI        
        MainGui.launch(MainGui.class, args);

    }
}
