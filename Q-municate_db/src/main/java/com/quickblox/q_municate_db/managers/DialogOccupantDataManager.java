package com.quickblox.q_municate_db.managers;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.stmt.PreparedQuery;
import com.j256.ormlite.stmt.QueryBuilder;
import com.quickblox.q_municate_db.managers.base.BaseManager;
import com.quickblox.q_municate_db.models.Dialog;
import com.quickblox.q_municate_db.models.DialogOccupant;
import com.quickblox.q_municate_db.models.User;
import com.quickblox.q_municate_db.utils.ErrorUtils;

import java.sql.SQLException;
import java.util.List;

public class DialogOccupantDataManager extends BaseManager<DialogOccupant> {

    private static final String TAG = DialogOccupantDataManager.class.getSimpleName();

    private Dao<Dialog, Long> dialogDao;

    public DialogOccupantDataManager(Dao<DialogOccupant, Long> dialogOccupantDao,
            Dao<Dialog, Long> dialogDao) {
        super(dialogOccupantDao, DialogOccupantDataManager.class.getSimpleName());
        this.dialogDao = dialogDao;
    }

    public List<DialogOccupant> getDialogOccupantsListByDialogId(String dialogId) {
        List<DialogOccupant> dialogOccupantsList = null;

        try {
            QueryBuilder<DialogOccupant, Long> queryBuilder = dao.queryBuilder();
            queryBuilder.where().eq(Dialog.Column.ID, dialogId);
            PreparedQuery<DialogOccupant> preparedQuery = queryBuilder.prepare();
            dialogOccupantsList = dao.query(preparedQuery);
        } catch (SQLException e) {
            ErrorUtils.logError(e);
        }

        return dialogOccupantsList;
    }

    public DialogOccupant getDialogOccupantForPrivateChat(int userId) {
        DialogOccupant dialogOccupant = null;

        try {
            QueryBuilder<DialogOccupant, Long> dialogOccupantQueryBuilder = dao.queryBuilder();
            dialogOccupantQueryBuilder.where().eq(User.Column.ID, userId);

            QueryBuilder<Dialog, Long> dialogQueryBuilder = dialogDao.queryBuilder();
            dialogQueryBuilder.where().eq(Dialog.Column.TYPE, Dialog.Type.PRIVATE);

            dialogOccupantQueryBuilder.join(dialogQueryBuilder);

            PreparedQuery<DialogOccupant> preparedQuery = dialogOccupantQueryBuilder.prepare();
            dialogOccupant = dao.queryForFirst(preparedQuery);
        } catch (SQLException e) {
            ErrorUtils.logError(e);
        }

        return dialogOccupant;
    }

    public DialogOccupant getDialogOccupant(String dialogId, int userId) {
        DialogOccupant dialogOccupant = null;

        try {
            QueryBuilder<DialogOccupant, Long> queryBuilder = dao.queryBuilder();
            queryBuilder.where().eq(Dialog.Column.ID, dialogId).and().eq(User.Column.ID, userId);
            PreparedQuery<DialogOccupant> preparedQuery = queryBuilder.prepare();
            dialogOccupant = dao.queryForFirst(preparedQuery);
        } catch (SQLException e) {
            ErrorUtils.logError(e);
        }

        return dialogOccupant;
    }
}