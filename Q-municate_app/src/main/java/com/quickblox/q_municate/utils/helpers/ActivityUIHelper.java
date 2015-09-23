package com.quickblox.q_municate.utils.helpers;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.quickblox.q_municate.R;
import com.quickblox.q_municate.ui.activities.chats.GroupDialogActivity;
import com.quickblox.q_municate.ui.activities.chats.PrivateDialogActivity;
import com.quickblox.q_municate_core.service.QBServiceConsts;
import com.quickblox.q_municate_db.managers.DataManager;
import com.quickblox.q_municate_db.models.Dialog;
import com.quickblox.q_municate_db.models.DialogOccupant;
import com.quickblox.q_municate_db.models.User;

import de.keyboardsurfer.android.widget.crouton.Crouton;

public class ActivityUIHelper {

    private Activity activity;
    private View newMessageView;
    private TextView newMessageTextView;
    private TextView senderMessageTextView;
    private Button notificationActionButton;

    private User senderUser;
    private Dialog messagesDialog;
    private boolean isPrivateMessage;

    public ActivityUIHelper(Activity activity) {
        this.activity = activity;
        initUI();
        initListeners();
    }

    private void initUI() {
        newMessageView = activity.getLayoutInflater().inflate(R.layout.list_item_new_message, null);
        newMessageTextView = (TextView) newMessageView.findViewById(R.id.message_textview);
        senderMessageTextView = (TextView) newMessageView.findViewById(R.id.sender_textview);
        notificationActionButton = (Button) newMessageView.findViewById(R.id.notification_action_button);
    }

    private void initListeners() {
        notificationActionButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                showDialog();
            }
        });
    }

    public void showChatMessageNotification(Bundle extras) {
        senderUser = (User) extras.getSerializable(QBServiceConsts.EXTRA_USER);
        String message = extras.getString(QBServiceConsts.EXTRA_CHAT_MESSAGE);
        String dialogId = extras.getString(QBServiceConsts.EXTRA_DIALOG_ID);
        isPrivateMessage = extras.getBoolean(QBServiceConsts.EXTRA_IS_PRIVATE_MESSAGE);
        if (isMessagesDialogCorrect(dialogId)) {
            showNewMessageAlert(senderUser, message);
        }
    }

    private boolean isMessagesDialogCorrect(String dialogId) {
        messagesDialog = DataManager.getInstance().getDialogDataManager().getByDialogId(dialogId);
        return messagesDialog != null;
    }

    public void showContactRequestNotification(Bundle extras) {
        int senderUserId = extras.getInt(QBServiceConsts.EXTRA_USER_ID);
        senderUser = DataManager.getInstance().getUserDataManager().get(senderUserId);
        String message = extras.getString(QBServiceConsts.EXTRA_MESSAGE);
        DialogOccupant dialogOccupant = DataManager.getInstance().getDialogOccupantDataManager().getDialogOccupantForPrivateChat(senderUserId);

        if (dialogOccupant != null) {
            String dialogId = dialogOccupant.getDialog().getDialogId();
            isPrivateMessage = true;
            if (isMessagesDialogCorrect(dialogId)) {
                showNewMessageAlert(senderUser, message);
            }
        }
    }

    public void showNewMessageAlert(User senderUser, String message) {
        if (senderUser == null) {
            return;
        }

        newMessageTextView.setText(message);
        senderMessageTextView.setText(senderUser.getFullName());
        Crouton.cancelAllCroutons();
        Crouton.show(activity, newMessageView);
    }

    protected void showDialog() {
        if (isPrivateMessage) {
            startPrivateChatActivity();
        } else {
            startGroupChatActivity();
        }
        Crouton.cancelAllCroutons();
    }

    private void startPrivateChatActivity() {
        PrivateDialogActivity.start(activity, senderUser, messagesDialog);
    }

    private void startGroupChatActivity() {
        GroupDialogActivity.start(activity, messagesDialog);
    }
}