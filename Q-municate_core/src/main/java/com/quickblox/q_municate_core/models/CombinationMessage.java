package com.quickblox.q_municate_core.models;

import com.quickblox.q_municate_db.models.Attachment;
import com.quickblox.q_municate_db.models.DialogNotification;
import com.quickblox.q_municate_db.models.DialogOccupant;
import com.quickblox.q_municate_db.models.Message;
import com.quickblox.q_municate_db.models.State;

import java.io.Serializable;
import java.util.Comparator;

// Combination DialogNotification and Message (for chats)
public class CombinationMessage implements Serializable {

    private String messageId;
    private DialogOccupant dialogOccupant;
    private Attachment attachment;
    private State state;
    private String body;
    private long createdDate;
    private DialogNotification.NotificationType notificationType;

    public CombinationMessage(DialogNotification dialogNotification) {
        this.messageId = dialogNotification.getDialogNotificationId();
        this.dialogOccupant = dialogNotification.getDialogOccupant();
        this.state = dialogNotification.getState();
        this.createdDate = dialogNotification.getCreatedDate();
        this.notificationType = dialogNotification.getNotificationType();
        this.body = dialogNotification.getBody();
    }

    public CombinationMessage(Message message) {
        this.messageId = message.getMessageId();
        this.dialogOccupant = message.getDialogOccupant();
        this.attachment = message.getAttachment();
        this.state = message.getState();
        this.body = message.getBody();
        this.createdDate = message.getCreatedDate();
    }

    public Message toMessage() {
        Message message = new Message();
        message.setMessageId(messageId);
        message.setDialogOccupant(dialogOccupant);
        message.setAttachment(attachment);
        message.setState(state);
        message.setBody(body);
        message.setCreatedDate(createdDate);
        return message;
    }

    public String getMessageId() {
        return messageId;
    }

    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }

    public DialogNotification.NotificationType getNotificationType() {
        return notificationType;
    }

    public void setNotificationType(DialogNotification.NotificationType notificationType) {
        this.notificationType = notificationType;
    }

    public long getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(long createdDate) {
        this.createdDate = createdDate;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public State getState() {
        return state;
    }

    public void setState(State state) {
        this.state = state;
    }

    public Attachment getAttachment() {
        return attachment;
    }

    public void setAttachment(Attachment attachment) {
        this.attachment = attachment;
    }

    public DialogOccupant getDialogOccupant() {
        return dialogOccupant;
    }

    public void setDialogOccupant(DialogOccupant dialogOccupant) {
        this.dialogOccupant = dialogOccupant;
    }

    public boolean isIncoming(int currentUserId) {
        return currentUserId != dialogOccupant.getUser().getUserId();
    }

    @Override
    public boolean equals(Object object) {
        if (object instanceof CombinationMessage) {
            return ((CombinationMessage) object).getMessageId().equals(messageId);
        } else {
            return false;
        }
    }

    public static class DateComparator implements Comparator<CombinationMessage> {

        @Override
        public int compare(CombinationMessage combinationMessage1, CombinationMessage combinationMessage2) {
            return ((Long) combinationMessage1.getCreatedDate()).compareTo(
                    ((Long) combinationMessage2.getCreatedDate()));
        }
    }
}