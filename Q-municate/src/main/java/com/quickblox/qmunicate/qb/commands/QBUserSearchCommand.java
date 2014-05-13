package com.quickblox.qmunicate.qb.commands;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.quickblox.internal.core.request.QBPagedRequestBuilder;
import com.quickblox.module.users.QBUsers;
import com.quickblox.module.users.model.QBUser;
import com.quickblox.qmunicate.App;
import com.quickblox.qmunicate.core.command.ServiceCommand;
import com.quickblox.qmunicate.model.Friend;
import com.quickblox.qmunicate.service.QBService;
import com.quickblox.qmunicate.service.QBServiceConsts;
import com.quickblox.qmunicate.utils.Consts;

import java.util.List;

public class QBUserSearchCommand extends ServiceCommand {

    public QBUserSearchCommand(Context context, String successAction, String failAction) {
        super(context, successAction, failAction);
    }

    public static void start(Context context, String constraint) {
        Intent intent = new Intent(QBServiceConsts.USER_SEARCH_ACTION, null, context, QBService.class);
        intent.putExtra(QBServiceConsts.EXTRA_CONSTRAINT, constraint);
        context.startService(intent);
    }

    @Override
    public Bundle perform(Bundle extras) throws Exception {
        String constraint = (String) extras.getSerializable(QBServiceConsts.EXTRA_CONSTRAINT);

        QBPagedRequestBuilder requestBuilder = new QBPagedRequestBuilder();
        requestBuilder.setPage(Consts.FL_FRIENDS_PAGE_NUM);
        requestBuilder.setPerPage(Consts.FL_FRIENDS_PER_PAGE);

        Bundle params = new Bundle();
        List<QBUser> users = QBUsers.getUsersByFullName(constraint, requestBuilder, params);
        List<Friend> friendsList = Friend.createFriends(users);
        friendsList.remove(new Friend(App.getInstance().getUser()));
        params.putSerializable(QBServiceConsts.EXTRA_FRIENDS, (java.io.Serializable) friendsList);
        return params;
    }
}