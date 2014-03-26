package com.quickblox.qmunicate.qb;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.quickblox.module.auth.QBAuth;
import com.quickblox.module.chat.QBChatService;
import com.quickblox.module.users.QBUsers;
import com.quickblox.module.users.model.QBUser;
import com.quickblox.qmunicate.core.command.ServiceCommand;
import com.quickblox.qmunicate.service.QBChatHelper;
import com.quickblox.qmunicate.service.QBService;
import com.quickblox.qmunicate.service.QBServiceConsts;

public class QBLoginCommand extends ServiceCommand {

    private static final String TAG = QBLoginCommand.class.getSimpleName();
    private QBChatHelper qbChatHelper;

    public static void start(Context context, QBUser user) {
        Intent intent = new Intent(QBServiceConsts.LOGIN_ACTION, null, context, QBService.class);
        intent.putExtra(QBServiceConsts.EXTRA_USER, user);
        context.startService(intent);
    }

    public QBLoginCommand(Context context, QBChatHelper qbChatHelper, String successAction, String failAction) {
        super(context, successAction, failAction);
        this.qbChatHelper = qbChatHelper;
    }

    @Override
    public Bundle perform(Bundle extras) throws Exception {
        QBUser user = (QBUser) extras.getSerializable(QBServiceConsts.EXTRA_USER);

        QBAuth.createSession();
        String password = user.getPassword();
        user = QBUsers.signIn(user);
        user.setPassword(password);
        QBChatService.getInstance().loginWithUser(user);
        qbChatHelper.init(context);
        Bundle result = new Bundle();
        result.putSerializable(QBServiceConsts.EXTRA_USER, user);

        return result;
    }
}