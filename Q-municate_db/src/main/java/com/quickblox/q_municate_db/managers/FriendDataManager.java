package com.quickblox.q_municate_db.managers;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.stmt.PreparedQuery;
import com.j256.ormlite.stmt.QueryBuilder;
import com.quickblox.q_municate_db.managers.base.BaseManager;
import com.quickblox.q_municate_db.models.Friend;
import com.quickblox.q_municate_db.models.User;
import com.quickblox.q_municate_db.utils.ErrorUtils;

import java.sql.SQLException;
import java.util.List;

public class FriendDataManager extends BaseManager<Friend> {

    private static final String TAG = FriendDataManager.class.getSimpleName();

    public FriendDataManager(Dao<Friend, Long> friendDao) {
        super(friendDao, FriendDataManager.class.getSimpleName());
    }

    public Friend getByUserId(int userId) {
        Friend friend = null;

        try {
            QueryBuilder<Friend, Long> queryBuilder = dao.queryBuilder();
            queryBuilder.where().eq(User.Column.ID, userId);
            PreparedQuery<Friend> preparedQuery = queryBuilder.prepare();
            friend = dao.queryForFirst(preparedQuery);
        } catch (SQLException e) {
            ErrorUtils.logError(e);
        }

        return friend;
    }

    public List<Friend> getFriendsByIds(List<Integer> idsList) {
        List<Friend> friendsList = null;

        try {
            QueryBuilder<Friend, Long> queryBuilder = dao.queryBuilder();
            queryBuilder.where().in(User.Column.ID, idsList);
            PreparedQuery<Friend> preparedQuery = queryBuilder.prepare();
            friendsList = dao.query(preparedQuery);
        } catch (SQLException e) {
            ErrorUtils.logError(e);
        }

        return friendsList;
    }
}