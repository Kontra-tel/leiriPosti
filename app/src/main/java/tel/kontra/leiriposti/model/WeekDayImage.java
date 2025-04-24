package tel.kontra.leiriposti.model;

import java.awt.Image;
import java.io.IOException;
import java.time.DayOfWeek;

import javax.imageio.ImageIO;

public class WeekDayImage {

    /**
     * Constructor for WeekDayImage class.
     * 
     * Loads the images from the resources folder.
     * The images are named according to the days of the week.
     * 
     * @since 0.1
     * 
     */
    public WeekDayImage() {
    }

    /**
     * getImage method returns the image corresponding to the given day of the week.
     * 
     * @param day The day of the week (1-7) for which the image is requested.
     * @return The image corresponding to the given day of the week.
     */
    public static Image getImage(DayOfWeek day) {
        String imagePath = "./week/" + day.getValue() + ".png"; // Construct the image path based on the day of the week
        
        System.out.println(imagePath);

        try {
            Image image = ImageIO.read(
                WeekDayImage.class.getClassLoader().getResource(imagePath)); // Load the image from the file

            return image; // Return the loaded image

        } catch (IOException e) {
            System.err.println("Error loading image: " + e.getMessage());
            e.printStackTrace();

            return null; // Return null if the image could not be loaded
        }
    }
}
