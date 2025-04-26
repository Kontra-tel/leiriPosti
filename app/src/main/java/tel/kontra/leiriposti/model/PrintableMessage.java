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

import tel.kontra.leiriposti.util.WeekDayImage;

/**
 * PrintableMessage class implements the Printable interface to provide a way to print messages.
 * It contains the printData to be printed and implements the print method to handle the printing process.
 * 
 * This class is used to format and print messages in a specific layout.
 * 
 * Im not entirely happy with the way this is done as it is not very flexible.
 * Most of the layout is hardcoded meaning I would have to change the source code to change the layout.
 * But this works for now and there is a high chance that it will remain the same.
 * 
 * @version 0.1
 * @since 0.1
 * 
 * @author Markus
 */
public class PrintableMessage implements Printable {
    
    private Message printData;

    public PrintableMessage(Message msg) {
        this.printData = msg;
    }

    /**
     * The print method is called by the printing system to print the message.
     * It formats the message data and draws it on the graphics context.
     * 
     * @param g The graphics context to draw on.
     * @param pf The page format for the print job.
     * @param pageIndex The index of the page to be printed.
     * @return PAGE_EXISTS if the page is valid, NO_SUCH_PAGE if the page index is invalid.
     * @throws PrinterException if an error occurs during printing.
     */
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

        // Set the graphics context for printing
        // Translate the graphics context to the printable area of the page
        Graphics2D g2d = (Graphics2D) g;
        g2d.translate(pf.getImageableX(), pf.getImageableY());

        // Title
        g2d.setFont(new Font("Serif", Font.BOLD, 24));
        g2d.drawString(title, 50, 50); // Draw the title at the top-left corner

        // Image
        // Load the image corresponding to the day of the week
        Image image = WeekDayImage.getImage(printData.getWeekDay()); // Load the image from the resources folder

        // Image dimensions
        int imageWidth = 100;
        int imageHeight = 100;

        // Draw the image at the top-right corner of the page
        // The image is drawn with a margin of 20 pixels from the right and top edges of the page
        g2d.drawImage(image, (int) pf.getImageableWidth() - imageWidth - 20, 20, imageWidth, imageHeight, null);

        // Set font for the subject
        g2d.setFont(new Font("Serif", Font.PLAIN, 12));
        FontMetrics metrics = g2d.getFontMetrics();
        int lineHeight = metrics.getHeight();
        int maxWidth = (int) pf.getImageableWidth() - 100; // Leave some margin
        int x = 50;
        int y = 105; // Start drawing the body below the title and image

        BufferedReader reader = new BufferedReader(new StringReader(body));

        // Read the body line by line and draw it on the page
        // The body is split into lines based on the maximum width
        
        String line;
        try {
            int availableHeight = (int) pf.getImageableHeight() - (4 * lineHeight); // Leave 4 rows for the greeting

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

        // Set font for the author greeting and update line height
        g2d.setFont(new Font("Serif", Font.ITALIC, 14));
        metrics = g2d.getFontMetrics();
        lineHeight = metrics.getHeight(); // Update line height for the greeting

        y += lineHeight; // Add spacing after the body
        g2d.drawString("Terveisin,", x, y); // Draw the greeting
        y += lineHeight; // Move to the next line
        g2d.drawString(author, x, y); // Draw the author's name below the greeting

        return PAGE_EXISTS;
    }
}
