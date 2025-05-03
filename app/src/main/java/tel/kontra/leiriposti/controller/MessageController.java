package tel.kontra.leiriposti.controller;

import java.util.Date;

import tel.kontra.leiriposti.model.Message;

public class MessageController {

    private Message[] messages;
    private Message[] newMessages;
    private Date latestMessage;

    public MessageController() {
        this.messages = new Message[0]; // Initialize messages array to empty
        this.newMessages = new Message[0]; // Initialize newMessages array to empty
        this.latestMessage = null; // Initialize latestMessage to null
    }
}
