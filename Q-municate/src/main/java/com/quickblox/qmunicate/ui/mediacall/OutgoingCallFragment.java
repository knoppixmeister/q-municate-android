package com.quickblox.qmunicate.ui.mediacall;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.quickblox.module.users.model.QBUser;
import com.quickblox.module.videochat.model.objects.CallType;
import com.quickblox.module.videochat_webrtc.ISignalingChannel;
import com.quickblox.module.videochat_webrtc.QBVideoChat;
import com.quickblox.module.videochat_webrtc.SignalingChannel;
import com.quickblox.module.videochat_webrtc.VideoStreamsView;
import com.quickblox.qmunicate.R;
import com.quickblox.qmunicate.core.communication.SessionDescriptionWrapper;
import com.quickblox.qmunicate.service.QBService;
import com.quickblox.qmunicate.ui.base.BaseFragment;
import com.quickblox.qmunicate.ui.utils.Consts;
import com.quickblox.qmunicate.ui.utils.DialogUtils;
import com.quickblox.qmunicate.ui.utils.ErrorUtils;

import org.webrtc.MediaConstraints;
import org.webrtc.SessionDescription;

import java.util.List;


public abstract class OutgoingCallFragment extends BaseFragment implements View.OnClickListener, ISignalingChannel.MessageObserver {

    public static final String TAG = OutgoingCallFragment.class.getSimpleName();
    protected QBVideoChat qbVideoChat;
    protected QBUser opponent;
    private Consts.CALL_DIRECTION_TYPE call_direction_type;
    private SessionDescription remoteSessionDescription;
    private boolean bounded;
    private QBService service;
    private CallType call_type;
    private ServiceConnection serviceConnection = new ChetServiceConnection();

    protected abstract int getContentView();

    protected abstract MediaConstraints getMediaConstraints();

    public static Bundle generateArguments(SessionDescriptionWrapper sessionDescriptionWrapper, QBUser user,
                                           Consts.CALL_DIRECTION_TYPE type, CallType callType) {
        Bundle args = new Bundle();
        args.putSerializable(Consts.USER, user);
        args.putSerializable(Consts.CALL_DIRECTION_TYPE_EXTRA, type);
        args.putSerializable(Consts.CALL_TYPE_EXTRA, callType);
        args.putParcelable(Consts.REMOTE_DESCRIPTION, sessionDescriptionWrapper);
        return args;
    }

    @Override
    public void onStart() {
        super.onStart();
        connectToService();
    }

    @Override
    public void onStop() {
        super.onStop();
        unbindService();
    }

    @Override
    public void onCall(QBUser user, CallType callType, SessionDescription sessionDescription, long sessionId) {

    }

    @Override
    public void onAccepted(QBUser user, SessionDescription sessionDescription, long sessionId) {
        if (isExistActivity()) {
            getBaseActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    DialogUtils.show(getActivity(), "accepted");
                }
            });
        }
        onConnectionEstablished();
    }

    @Override
    public void onStop(QBUser user, String reason, long sessionId) {
        stopCall(false);
    }

    @Override
    public void onRejected(QBUser user, long sessionId) {
        if (isExistActivity()) {
            getBaseActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    DialogUtils.show(getActivity(), "Rejected");
                    stopCall(false);
                }
            });
        }
    }

    @Override
    public void onError(List<String> errors) {
        if (isExistActivity()) {
            ErrorUtils.showError(getActivity(), errors.toString());
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (qbVideoChat != null) {
            qbVideoChat.onActivityPause();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.i(TAG, "onCreateView");
        View rootView = inflater.inflate(getContentView(), container, false);
        SessionDescriptionWrapper sessionDescriptionWrapper =
                getArguments().getParcelable(Consts.REMOTE_DESCRIPTION);
        if (sessionDescriptionWrapper != null) {
            remoteSessionDescription = sessionDescriptionWrapper.getSessionDescription();
        }
        call_direction_type = (Consts.CALL_DIRECTION_TYPE) getArguments().getSerializable(Consts.CALL_DIRECTION_TYPE_EXTRA);
        opponent = (QBUser) getArguments().getSerializable(Consts.USER);
        call_type = (CallType) getArguments().getSerializable(Consts.CALL_TYPE_EXTRA);
        rootView.findViewById(R.id.stopСallButton).setOnClickListener(this);
        postInit(rootView);
        return rootView;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.stopСallButton:
                stopCall(true);
                break;
            default:
                break;
        }
    }


    @Override
    public void onResume() {
        super.onResume();
        if (qbVideoChat != null) {
            qbVideoChat.onActivityResume();
        }
    }

    public void initChat(SignalingChannel signalingChannel) {
        MediaConstraints mediaConstraints = getMediaConstraints();
        VideoStreamsView videoView = (VideoStreamsView) getView().findViewById(R.id.ownVideoScreenImageView);
        qbVideoChat = new QBVideoChat(getActivity(), mediaConstraints, signalingChannel, videoView);
        signalingChannel.addMessageObserver(this);
        if (remoteSessionDescription != null) {
            qbVideoChat.setRemoteSessionDescription(remoteSessionDescription);
        }
        if (Consts.CALL_DIRECTION_TYPE.OUTGOING.equals(call_direction_type) && opponent != null) {
            qbVideoChat.call(opponent, call_type);
        } else {
            qbVideoChat.accept(opponent);
            onConnectionEstablished();
        }
    }

    protected void onConnectionEstablished() {

    }

    protected void postInit(View rootView) {
    }

    protected void onConnectionClosed() {

    }

    private void connectToService() {
        Intent intent = new Intent(getActivity(), QBService.class);
        if (isExistActivity()) {
            getBaseActivity().bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE);
        }
    }

    private boolean isExistActivity() {
        return ((!isDetached()) && (getBaseActivity() != null));
    }

    private void unbindService() {
        if (isExistActivity() && bounded) {
            getBaseActivity().unbindService(serviceConnection);
        }
    }

    private void stopCall(boolean sendStop) {
        if (qbVideoChat != null) {
            if (sendStop) {
                qbVideoChat.stopCall();
            }
            qbVideoChat.stop();
            qbVideoChat.dispose();
        }
        onConnectionClosed();
        getBaseActivity().finish();
    }

    private void onConnectedToService() {
        SignalingChannel signalingChannel = service.getQbChatHelper().getSignalingChannel();
        if (signalingChannel != null && isExistActivity()) {
            initChat(signalingChannel);
        } else if (isExistActivity()) {
            ErrorUtils.showError(getActivity(), "Cannot establish connection. Check internet settings");
        }
    }

    private class ChetServiceConnection implements ServiceConnection {

        @Override
        public void onServiceConnected(ComponentName name, IBinder binder) {
            Log.i(TAG, "onServiceConnected");
            bounded = true;
            service = ((QBService.QBServiceBinder) binder).getService();
            onConnectedToService();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

        }
    }
}