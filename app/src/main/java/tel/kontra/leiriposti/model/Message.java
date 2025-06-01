package tel.kontra.leiriposti.model;

import java.io.Serializable;
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
 * @version 1.1
 * @since 0.1
 */
@Data
public class Message implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    private String recipient;
    private String subject;
    private String body;
    private Date timeStamp; // The time when the message was sent
    private String author; // The author of the message
    private DayOfWeek weekDay; // The day of the week when the message was sent (1-7)
    private MessageStatus status = MessageStatus.NOT_PRINTED; // Status of the message (not printed, printed, printing)
    
    /**
     * Constructor for Message class.
     * 
     * @param recipient
     * @param subject
     * @param body
     * @param timeStamp
     * @param author
     */
    public Message(String timeStamp, String subject, String body, String recipient, String author) {
        
        // Set data fields
        this.author = author;
        this.recipient = recipient;
        this.subject = subject;
        this.body = body;

        /**
         * We have to do some cleaning for the timeStamp to match
         * 24.4.2025 klo 14.08.47 => 24/04/2025 14:08:47
         * Replace dots with slashes and remove "klo" (Finnish for "at")
         */
        String[] dateTime = timeStamp.split(" klo ");

        if (dateTime.length != 2) {
            throw new IllegalArgumentException("Invalid timeStamp format: " + timeStamp);
        }

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
            this.weekDay = DayOfWeek.of(dayOfWeek); // Set the weekDay field (1=Monday, 7=Sunday)
            
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid date format: " + timeStamp, e);
        }
    }

    /**
     * Sets the status of the message.
     * @param status The status to set for the message.
     * @see MessageStatus
     */
    public void setStatus(MessageStatus status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return "[" + recipient + "] " + subject + " - " + status;
    }
}