package com.quickblox.q_municate_core.qb.commands.rest;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.quickblox.q_municate_core.R;
import com.quickblox.q_municate_core.core.command.ServiceCommand;
import com.quickblox.q_municate_core.models.UserCustomData;
import com.quickblox.q_municate_core.qb.helpers.QBAuthHelper;
import com.quickblox.q_municate_core.service.QBService;
import com.quickblox.q_municate_core.service.QBServiceConsts;
import com.quickblox.q_municate_core.utils.ConstsCore;
import com.quickblox.q_municate_core.utils.helpers.CoreSharedHelper;
import com.quickblox.q_municate_core.utils.Utils;
import com.quickblox.users.model.QBUser;

public class QBSocialLoginCommand extends ServiceCommand {

    private static final String TAG = QBSocialLoginCommand.class.getSimpleName();

    private final QBAuthHelper authHelper;

    public QBSocialLoginCommand(Context context, QBAuthHelper authHelper, String successAction,
            String failAction) {
        super(context, successAction, failAction);
        this.authHelper = authHelper;
    }

    public static void start(Context context, String socialProvider, String accessToken,
            String accessTokenSecret) {
        Intent intent = new Intent(QBServiceConsts.SOCIAL_LOGIN_ACTION, null, context, QBService.class);
        intent.putExtra(QBServiceConsts.EXTRA_SOCIAL_PROVIDER, socialProvider);
        intent.putExtra(QBServiceConsts.EXTRA_ACCESS_TOKEN, accessToken);
        intent.putExtra(QBServiceConsts.EXTRA_ACCESS_TOKEN_SECRET, accessTokenSecret);
        context.startService(intent);
    }

    @Override
    public Bundle perform(Bundle extras) throws Exception {
        String socialProvider = (String) extras.getSerializable(QBServiceConsts.EXTRA_SOCIAL_PROVIDER);
        String accessToken = (String) extras.getSerializable(QBServiceConsts.EXTRA_ACCESS_TOKEN);
        String accessTokenSecret = (String) extras.getSerializable(QBServiceConsts.EXTRA_ACCESS_TOKEN_SECRET);
        QBUser user = authHelper.login(socialProvider, accessToken, accessTokenSecret);
        if (user.getCustomData() == null) {
            CoreSharedHelper.getInstance().saveUsersImportInitialized(false);
            extras.putSerializable(QBServiceConsts.AUTH_ACTION_TYPE, QBServiceConsts.AUTH_TYPE_REGISTRATION);
            extras.putSerializable(QBServiceConsts.EXTRA_USER, getUserWithAvatar(user));
        } else {
            CoreSharedHelper.getInstance().saveUsersImportInitialized(true);
            extras.putSerializable(QBServiceConsts.AUTH_ACTION_TYPE, QBServiceConsts.AUTH_TYPE_LOGIN);
            extras.putSerializable(QBServiceConsts.EXTRA_USER, user);
        }
        return extras;
    }

    private QBUser getUserWithAvatar(QBUser user) {
        String avatarUrl = context.getString(R.string.inf_url_to_facebook_avatar, user.getFacebookId());
        QBUser newUser = new QBUser();
        newUser.setId(user.getId());
        newUser.setPassword(user.getPassword());
        newUser.setCustomData(Utils.customDataToString(getUserCustomData(avatarUrl)));
        return newUser;
    }

    private UserCustomData getUserCustomData(String avatarUrl) {
        String isImport = "1"; // first FB login
        return new UserCustomData(avatarUrl, ConstsCore.EMPTY_STRING, isImport);
    }
}