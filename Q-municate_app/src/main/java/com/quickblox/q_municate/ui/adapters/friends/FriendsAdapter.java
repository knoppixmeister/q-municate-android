package com.quickblox.q_municate.ui.adapters.friends;

import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.quickblox.q_municate.R;
import com.quickblox.q_municate.ui.activities.base.BaseActivity;
import com.quickblox.q_municate.ui.adapters.base.BaseClickListenerViewHolder;
import com.quickblox.q_municate.ui.adapters.base.BaseRecyclerViewAdapter;
import com.quickblox.q_municate.ui.adapters.base.BaseViewHolder;
import com.quickblox.q_municate.ui.views.roundedimageview.RoundedImageView;
import com.quickblox.q_municate.utils.DateUtils;
import com.quickblox.q_municate_db.models.User;

import java.util.List;

import butterknife.Bind;

public class FriendsAdapter extends BaseRecyclerViewAdapter<User, BaseClickListenerViewHolder<User>> {

    public FriendsAdapter(BaseActivity baseActivity, List<User> usersList) {
        super(baseActivity, usersList);
    }

    @Override
    public BaseClickListenerViewHolder<User> onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ViewHolder(this, layoutInflater.inflate(R.layout.item_friend, parent, false));
    }

    @Override
    public void onBindViewHolder(BaseClickListenerViewHolder<User> baseClickListenerViewHolder, final int position) {
        User user = getItem(position);
        ViewHolder viewHolder = (ViewHolder) baseClickListenerViewHolder;

        initFirstLetter(viewHolder, position, user);

        viewHolder.nameTextView.setText(user.getFullName());
        viewHolder.labelTextView.setText(context.getString(R.string.last_seen,
                DateUtils.toTodayYesterdayShortDateWithoutYear2(user.getLastLogin()),
                DateUtils.formatDateSimpleTime(user.getLastLogin())));

        displayAvatarImage(user.getAvatar(), viewHolder.avatarImageView);
    }

    private void initFirstLetter(ViewHolder viewHolder, int position, User user) {
        if (TextUtils.isEmpty(user.getFullName())) {
            return;
        }

        Character firstLatter = user.getFullName().toUpperCase().charAt(0);
        if (position == 0) {
            setLetterVisible(viewHolder, firstLatter);
        } else {
            Character beforeFirstLatter;
            User beforeUser = getItem(position - 1);
            if (beforeUser != null && beforeUser.getFullName() != null) {
                beforeFirstLatter = beforeUser.getFullName().toUpperCase().charAt(0);

                if (!firstLatter.equals(beforeFirstLatter)) {
                    setLetterVisible(viewHolder, firstLatter);
                }
            }
        }
    }

    private void setLetterVisible(ViewHolder viewHolder, Character character) {
        viewHolder.firstLatterTextView.setText(String.valueOf(character));
        viewHolder.firstLatterTextView.setVisibility(View.VISIBLE);
    }

    protected static class ViewHolder extends BaseViewHolder<User> {

        @Bind(R.id.first_latter_textview)
        TextView firstLatterTextView;

        @Bind(R.id.avatar_imageview)
        RoundedImageView avatarImageView;

        @Bind(R.id.name_textview)
        TextView nameTextView;

        @Bind(R.id.label_textview)
        TextView labelTextView;

        public ViewHolder(FriendsAdapter adapter, View view) {
            super(adapter, view);
        }
    }
}