package tel.kontra.leiriposti.view;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.Serializable;

import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.TextArea;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.Appender;
import org.apache.logging.log4j.core.Layout;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.appender.AbstractAppender;
import org.apache.logging.log4j.core.config.LoggerConfig;
import org.apache.logging.log4j.core.layout.PatternLayout;
import org.apache.logging.log4j.core.LoggerContext;

/**
 * ConsoleGui class is responsible for displaying a console window in the GUI.
 * It captures System.out and Log4j2 logger output and displays them in a non-editable TextArea.
 *
 * @version 1.0
 * @since 0.1
 *
 * @author Markus
 */
public class ConsoleGui {
    /**
     * Starts the console GUI window.
     * Redirects System.out and Log4j2 logger output to the TextArea.
     *
     * @param primaryStage The stage to display the console window.
     */
    public static void start(Stage primaryStage) {
        TextArea textArea = new TextArea();
        textArea.setEditable(false); // Make the TextArea non-writable by the user

        PrintStream ps = System.out;
        System.setOut(new PrintStream(new StreamCapturer("STDOUT", new Consumer() {
            @Override
            public void appendText(String text) {
                Platform.runLater(() -> textArea.appendText(text));
            }
        }, ps)));

        // Add Log4j2 appender to redirect logs to the TextArea
        addTextAreaAppender(textArea);

        BorderPane root = new BorderPane(textArea);
        Scene scene = new Scene(root, 600, 400);
        primaryStage.initOwner(MainGui.getPrimaryStage()); // Set the owner of the stage to the main GUI
        primaryStage.setTitle("Console Output");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    /**
     * Adds a custom Log4j2 appender to redirect logger output to the TextArea.
     *
     * @param textArea The TextArea to display log output.
     */
    private static void addTextAreaAppender(TextArea textArea) {
        LoggerContext context = (LoggerContext) LogManager.getContext(false);
        Layout<? extends Serializable> layout = PatternLayout.newBuilder()
                .withPattern("%d{HH:mm:ss} %-5level %logger{20} - %msg%n")
                .build();
        Appender appender = new TextAreaAppender("TextAreaAppender", layout, textArea);
        appender.start();
        // Attach to ALL loggers, not just root
        context.getConfiguration().addAppender(appender);
        for (LoggerConfig loggerConfig : context.getConfiguration().getLoggers().values()) {
            loggerConfig.addAppender(appender, org.apache.logging.log4j.Level.ALL, null);
            loggerConfig.setLevel(org.apache.logging.log4j.Level.ALL);
        }
        LoggerConfig rootLoggerConfig = context.getConfiguration().getLoggerConfig(LogManager.ROOT_LOGGER_NAME);
        rootLoggerConfig.addAppender(appender, org.apache.logging.log4j.Level.ALL, null);
        rootLoggerConfig.setLevel(org.apache.logging.log4j.Level.ALL);
        context.updateLoggers();
    }

    /**
     * Consumer interface for appending text to the TextArea.
     */
    public interface Consumer {
        void appendText(String text);
    }

    /**
     * StreamCapturer class captures System.out output and redirects it to the TextArea.
     */
    public static class StreamCapturer extends OutputStream {

        private StringBuilder buffer;
        private String prefix;
        private Consumer consumer;
        private PrintStream old;

        public StreamCapturer(String prefix, Consumer consumer, PrintStream old) {
            this.prefix = prefix;
            buffer = new StringBuilder(128);
            buffer.append("[").append(prefix).append("] ");
            this.old = old;
            this.consumer = consumer;
        }

        @Override
        public void write(int b) throws IOException {
            char c = (char) b;
            String value = Character.toString(c);
            buffer.append(value);
            if (value.equals("\n")) {
                consumer.appendText(buffer.toString());
                buffer.delete(0, buffer.length());
                buffer.append("[").append(prefix).append("] ");
            }
            old.print(c);
        }
    }

    /**
     * Custom Log4j2 Appender for writing log output to the TextArea.
     */
    private static class TextAreaAppender extends AbstractAppender {
        private final TextArea textArea;

        protected TextAreaAppender(String name, Layout<? extends Serializable> layout, TextArea textArea) {
            super(name, null, layout, false, null);
            this.textArea = textArea;
        }

        @Override
        public void append(LogEvent event) {
            final String message = new String(getLayout().toByteArray(event));
            Platform.runLater(() -> textArea.appendText(message));
        }
    }
}
