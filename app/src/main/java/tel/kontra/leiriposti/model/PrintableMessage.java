package tel.kontra.leiriposti.model;

import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.print.PageFormat;
import java.awt.print.Printable;
import java.awt.print.PrinterException;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;

/**
 * PrintableMessage class implements the Printable interface to provide a way to print messages.
 * It contains the printData to be printed and implements the print method to handle the printing process.
 * 
 * This class is used to format and print messages in a specific layout.
 * 
 * @version 1.0
 * @since 0.1
 * 
 */
public class PrintableMessage implements Printable {
    
    private Message printData;

    public PrintableMessage(Message msg) {
        this.printData = msg;
    }

    public int print(Graphics g, PageFormat pf, int pageIndex) throws PrinterException {
        
        // Check if the page index is valid
        if (pageIndex > 0) {
           return NO_SUCH_PAGE;
        }

        // Get message data from the Message object
        String title = printData.getSubject();
        //String subject = printData.getSubject();
        String author = printData.getAuthor();
        String body = printData.getBody();

        Graphics2D g2d = (Graphics2D) g;
        g2d.translate(pf.getImageableX(), pf.getImageableY());

        // Set font for the title
        g2d.setFont(new Font("Serif", Font.BOLD, 24));
        g2d.drawString(title, 50, 50); // Draw the title at the top-left corner

        Image image = WeekDayImage.getImage(printData.getWeekDay()); // Load the image from the resources folder
        int imageWidth = 100;
        int imageHeight = 100;
        g2d.drawImage(image, (int) pf.getImageableWidth() - imageWidth - 20, 20, imageWidth, imageHeight, null);

        // Set font for the subject
        g2d.setFont(new Font("Serif", Font.PLAIN, 16));
        FontMetrics metrics = g2d.getFontMetrics();
        int lineHeight = metrics.getHeight();
        int maxWidth = (int) pf.getImageableWidth() - 100; // Leave some margin
        int x = 50;
        int y = 105; // Start drawing the body below the title and image

        BufferedReader reader = new BufferedReader(new StringReader(body));
        String line;
        try {
            int availableHeight = (int) pf.getImageableHeight() - (4 * lineHeight); // Leave 4 rows for the greeting

            // Set font for the body
            g2d.setFont(new Font("Serif", Font.PLAIN, 12));

            while ((line = reader.readLine()) != null) {

            int start = 0;
            while (start < line.length()) {
                
                int end = start;

                while (end < line.length() && metrics.stringWidth(line.substring(start, end + 1)) <= maxWidth) {
                end++;
                }

                if (y + lineHeight > availableHeight) {
                break; // Stop drawing if there's no more space for the body
                }

                g2d.drawString(line.substring(start, end), x, y);
                y += lineHeight;
                start = end;
            }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

        // Set font for the author greeting
        g2d.setFont(new Font("Serif", Font.ITALIC, 14));
        y += lineHeight; // Add spacing after the body
        g2d.drawString("Terveisin,", x, y); // Draw the greeting
        y += lineHeight; // Move to the next line
        g2d.drawString(author, x, y); // Draw the author's name below the greeting

        return PAGE_EXISTS;
    }
}
