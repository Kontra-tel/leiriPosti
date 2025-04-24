package tel.kontra.leiriposti.model;

import java.text.SimpleDateFormat;
import java.time.DayOfWeek;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import lombok.Data;

/**
 * Message class represents a message with sender, recipient, subject, and body.
 * It is used to encapsulate the data for sending messages.
 * 
 */
@Data
public class Message {
    
    private String recipient;
    private String subject;
    private String body;
    private Date timeStamp; // The time when the message was sent
    private String author; // The author of the message
    private DayOfWeek weekDay; // The day of the week when the message was sent (1-7)
    
    // Maybe an attachment field in the future

    /**
     * Constructor for Message class.
     * 
     * @param recipient
     * @param subject
     * @param body
     * @param timeStamp
     * @param author
     */
    public Message(String recipient, String subject, String body, String timeStamp, String author) {
        
        // Set data fields
        this.author = author;
        this.recipient = recipient;
        this.subject = subject;
        this.body = body;

        // We have to do some cleaning for the timeStamp to match
        // 24.4.2025 klo 14.08.47 => 24/04/2025 14:08:47
        // Replace dots with slashes and remove "klo" (Finnish for "at")
        String[] dateTime = timeStamp.split(" klo ");

        String date = dateTime[0].replace(".", "/").replace(" ", "/");
        String time = dateTime[1].replace(".", ":").replace(" ", ":");

        // Construct the timeStamp string in the format "dd/MM/yyyy HH:mm:ss"
        timeStamp = date + " " + time;

        // Parse the timeStamp string to a Date object and set the weekDay field
        try {

            // Get timestamp from format "dd/MM/yyyy HH:mm:ss"
            SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
            formatter.setTimeZone(TimeZone.getTimeZone("Europe/Helsinki")); // Set the timezone to Helsinki
            this.timeStamp = formatter.parse(timeStamp); // Parse the date string to a Date object

            // Set the weekDay field based on the timeStamp
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(this.timeStamp);
            int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK); // Get the day of the week (1-7)
            this.weekDay = DayOfWeek.of(dayOfWeek-1); // Set the weekDay field (1=Monday, 7=Sunday)
            
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid date format: " + timeStamp, e);
        }

    }

    @Override
    public String toString() {
        return "Message Details:\n" +
            "Author: " + author + "\n" +
            "Recipient: " + recipient + "\n" +
            "Subject: " + subject + "\n" +
            "Body: " + body + "\n" +
            "Time Sent: " + timeStamp + "\n" +
            "Weekday: " + weekDay + "\n";
    }

}