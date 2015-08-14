package com.quickblox.q_municate_core.qb.commands;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.quickblox.q_municate_core.core.command.CompositeServiceCommand;
import com.quickblox.q_municate_core.service.QBService;
import com.quickblox.q_municate_core.service.QBServiceConsts;
import com.quickblox.q_municate_core.utils.ErrorUtils;
import com.quickblox.q_municate_core.utils.PrefsHelper;
import com.quickblox.q_municate_core.utils.CoreSharedHelper;
import com.quickblox.q_municate_db.managers.DataManager;

public class QBLogoutCompositeCommand extends CompositeServiceCommand {

    private static final String TAG = QBLogoutCompositeCommand.class.getSimpleName();

    public QBLogoutCompositeCommand(Context context, String successAction, String failAction) {
        super(context, successAction, failAction);
    }

    public static void start(Context context) {
        Intent intent = new Intent(QBServiceConsts.LOGOUT_ACTION, null, context, QBService.class);
        context.startService(intent);
    }

    @Override
    public Bundle perform(Bundle extras) throws Exception {
        try {
            super.perform(extras);
            resetCacheData();
            resetSharedPreferences();
            resetUserData();
        } catch (Exception e) {
            ErrorUtils.logError(TAG, e);
        }
        return extras;
    }

    private void resetCacheData() {
        DataManager.getInstance().clearAllTables();
    }

    private void resetSharedPreferences() {
        CoreSharedHelper.getSharedHelper().clearAll();
    }

    private void resetUserData() {
        PrefsHelper helper = PrefsHelper.getPrefsHelper();
        helper.delete(PrefsHelper.PREF_USER_EMAIL);
        helper.delete(PrefsHelper.PREF_USER_PASSWORD);
        helper.delete(PrefsHelper.PREF_STATUS);
    }
}