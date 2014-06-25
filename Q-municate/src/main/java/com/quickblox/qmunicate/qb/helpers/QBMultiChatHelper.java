package com.quickblox.qmunicate.qb.helpers;

import android.content.Context;

import com.quickblox.internal.core.exception.QBResponseException;
import com.quickblox.internal.module.custom.request.QBCustomObjectUpdateBuilder;
import com.quickblox.module.chat.QBChatMessage;
import com.quickblox.module.chat.QBPrivateChat;
import com.quickblox.module.chat.QBRoomChat;
import com.quickblox.module.chat.QBRoomChatManager;
import com.quickblox.module.chat.listeners.QBMessageListener;
import com.quickblox.module.chat.model.QBDialog;
import com.quickblox.module.chat.model.QBDialogType;
import com.quickblox.module.content.model.QBFile;
import com.quickblox.qmunicate.R;
import com.quickblox.qmunicate.caching.DatabaseManager;
import com.quickblox.qmunicate.model.DialogMessageCache;
import com.quickblox.qmunicate.model.Friend;
import com.quickblox.qmunicate.utils.ChatUtils;
import com.quickblox.qmunicate.utils.Consts;
import com.quickblox.qmunicate.utils.DateUtils;
import com.quickblox.qmunicate.utils.ErrorUtils;

import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.XMPPException;

import java.util.ArrayList;
import java.util.List;

public class QBMultiChatHelper extends BaseChatHelper {

    private QBRoomChatManager roomChatManager;
    private QBRoomChat roomChat;
    private RoomChatMessageListener roomChatMessageListener = new RoomChatMessageListener();
    private QBNotificationChatListener notificationChatListener = new RoomNotificationListener();

    public QBMultiChatHelper(Context context) {
        super(context);
    }

    public void sendGroupMessage(String roomJidId, String message) throws Exception {
        roomChat = roomChatManager.getRoom(roomJidId);
        if (roomChat == null) {
            return;
        }
        QBChatMessage chatMessage = getQBChatMessage(message);
        sendRoomMessage(chatMessage);
    }

    public void init() {
        super.init();
        roomChatManager = chatService.getRoomChatManager();
        addNotificationChatListener(notificationChatListener);
    }

    private void sendRoomMessage(QBChatMessage chatMessage) throws Exception {
        String error = null;
        try {
            roomChat.sendMessage(chatMessage);
        } catch (XMPPException e) {
            error = context.getString(R.string.dlg_fail_send_msg);
        } catch (SmackException.NotConnectedException e) {
            error = context.getString(R.string.dlg_fail_connection);
        }
        if (error != null) {
            throw new Exception(error);
        }
    }

    public void sendGroupMessageWithAttachImage(String roomJidId, QBFile file) throws Exception {
        roomChat = roomChatManager.getRoom(roomJidId);
        if (roomChat == null) {
            return;
        }
        QBChatMessage chatMessage = getQBChatMessageWithImage(file);
        sendRoomMessage(chatMessage);
    }

    private void tryJoinRoomChat(QBDialog dialog) {
        try {
            joinRoomChat(dialog);
        } catch (Exception e) {
            ErrorUtils.logError(e);
        }
    }

    public void tryJoinRoomChats(List<QBDialog> chatDialogsList) {
        if (!chatDialogsList.isEmpty()) {
            for (QBDialog dialog : chatDialogsList) {
                if (!QBDialogType.PRIVATE.equals(dialog.getType())) {
                    tryJoinRoomChat(dialog);
                }
            }
        }
    }

    public QBDialog createRoomChat(String roomName,
            List<Integer> friendIdsList) throws SmackException, XMPPException, QBResponseException {
        ArrayList<Integer> occupantIdsList = ChatUtils.getOccupantIdsWithUser(friendIdsList);
        QBDialog dialog = roomChatManager.createDialog(roomName, QBDialogType.GROUP, occupantIdsList);
        joinRoomChat(dialog);
        inviteFriendsToRoom(dialog, friendIdsList);
        saveDialogToCache(context, dialog);
        return dialog;
    }

    private void inviteFriendsToRoom(QBDialog dialog,
            List<Integer> friendIdsList) throws XMPPException, SmackException {
        for (Integer friendId : friendIdsList) {
            try {
                notifyFriendAboutInvitation(dialog, friendId);
            } catch (QBResponseException responseException) {

            }
        }
    }

    private void notifyFriendAboutInvitation(QBDialog dialog, Integer friendId) throws QBResponseException {
        long time = DateUtils.getCurrentTime();
        QBPrivateChat chat = chatService.getPrivateChatManager().getChat(friendId);
        if (chat == null) {
            chat = chatService.getPrivateChatManager().createChat(friendId, null);
        }
        QBChatMessage chatMessage = ChatUtils.createRoomNotificationMessage(context, dialog);
        chatMessage.setProperty(PROPERTY_DATE_SENT, time + Consts.EMPTY_STRING);
        try {
            chat.sendMessage(chatMessage);
        } catch (Exception e) {
            ErrorUtils.logError(e);
        }
    }

    public void joinRoomChat(QBDialog dialog) throws XMPPException, SmackException {
        roomChat = roomChatManager.getRoom(dialog.getRoomJid());
        if (roomChat == null) {
            roomChat = roomChatManager.createRoom(dialog.getRoomJid());
            roomChat.setRestModel(dialog);
            roomChat.addMessageListener(roomChatMessageListener);
        }
        roomChat.join();
    }


    public List<Integer> getRoomOnlineParticipantList(String roomJid) throws XMPPException {
        return new ArrayList<Integer>(roomChatManager.getRoom(roomJid).getOnlineRoomUserIds());
    }

    public void leaveRoomChat(
            String roomJid) throws XMPPException, SmackException.NotConnectedException, QBResponseException {
        roomChatManager.getRoom(roomJid).leave();

        List<Integer> userIdsList = new ArrayList<Integer>();
        userIdsList.add(user.getId());
        removeUsersFromRoom(roomJid, userIdsList);

        DatabaseManager.deleteDialogByRoomJid(context, roomJid);
    }

    public void addUsersToRoom(String roomJid, List<Integer> userIdsList) throws QBResponseException {
        QBDialog dialog = DatabaseManager.getDialogByDialogId(context, roomJid);

        QBCustomObjectUpdateBuilder requestBuilder = new QBCustomObjectUpdateBuilder();
        requestBuilder.push(com.quickblox.internal.module.chat.Consts.DIALOG_OCCUPANTS,
                userIdsList.toArray());
        updateDialog(dialog.getDialogId(), dialog.getName(), requestBuilder);
    }

    public void removeUsersFromRoom(String roomJid, List<Integer> userIdsList) throws QBResponseException {
        QBDialog dialog = DatabaseManager.getDialogByDialogId(context, roomJid);

        QBCustomObjectUpdateBuilder requestBuilder = new QBCustomObjectUpdateBuilder();
        requestBuilder.pullAll(com.quickblox.internal.module.chat.Consts.DIALOG_OCCUPANTS,
                userIdsList.toArray());
        updateDialog(dialog.getDialogId(), dialog.getName(), requestBuilder);
    }

    public void updateRoomName(String roomJid, String newName) throws QBResponseException {
        QBDialog dialog = DatabaseManager.getDialogByDialogId(context, roomJid);

        QBCustomObjectUpdateBuilder requestBuilder = new QBCustomObjectUpdateBuilder();
        updateDialog(dialog.getDialogId(), newName, requestBuilder);
    }


    private void updateDialog(String dialogId, String newName,
            QBCustomObjectUpdateBuilder requestBuilder) throws QBResponseException {
        QBDialog updatedDialog = roomChatManager.updateDialog(dialogId, newName, requestBuilder);
        DatabaseManager.saveDialog(context, updatedDialog);
    }

    private void createDialogByNotification(QBChatMessage chatMessage) {
        long time;
        String roomJidId;
        String attachUrl = null;
        time = DateUtils.getCurrentTime();
        QBDialog dialog = ChatUtils.parseDialogFromMessage(chatMessage, chatMessage.getBody(), time);
        roomJidId = dialog.getRoomJid();
        if (roomJidId != null && !QBDialogType.PRIVATE.equals(dialog.getType())) {
            tryJoinRoomChat(dialog);
            saveDialogToCache(context, dialog);
        }
    }

    private void updateDialogByNotification(QBChatMessage chatMessage) {

    }

    private class RoomChatMessageListener implements QBMessageListener<QBRoomChat> {

        @Override
        public void processMessage(QBRoomChat roomChat, QBChatMessage chatMessage) {
            Friend friend = DatabaseManager.getFriendById(context, chatMessage.getSenderId());
            String attachUrl = ChatUtils.getAttachUrlIfExists(chatMessage);
            String roomJid = chatMessage.getProperty(com.quickblox.internal.module.chat.Consts.DIALOG_ID);
            long time = Long.parseLong(chatMessage.getProperty(PROPERTY_DATE_SENT).toString());
            saveMessageToCache(new DialogMessageCache(roomJid, chatMessage.getSenderId(),
                    chatMessage.getBody(), attachUrl, time, false));
            if (!chatMessage.getSenderId().equals(user.getId())) {
                // TODO IS handle logic when friend is not in the friend list
                notifyMessageReceived(chatMessage, friend, roomJid);
            }
        }
    }

    private class RoomNotificationListener implements QBNotificationChatListener {

        @Override
        public void onReceivedNotification(String notificationType, QBChatMessage chatMessage) {
            if (ChatUtils.PROPERTY_NOTIFICATION_TYPE_CREATE_CHAT.equals(notificationType)) {
                createDialogByNotification(chatMessage);
            } else {
                updateDialogByNotification(chatMessage);
            }
        }
    }
}