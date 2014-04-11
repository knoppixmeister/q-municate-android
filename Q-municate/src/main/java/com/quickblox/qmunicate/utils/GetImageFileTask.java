package com.quickblox.qmunicate.utils;

import android.widget.ImageView;

import com.quickblox.qmunicate.core.concurrency.BaseAsyncTask;

import java.io.File;
import java.io.IOException;

public class GetImageFileTask extends BaseAsyncTask {
    private OnGetImageFileListener listener;

    public GetImageFileTask(OnGetImageFileListener listener) {
        this.listener = listener;
    }

    @Override
    public void onResult(Object imageFile) {
        listener.onGotImageFile((File) imageFile);
    }

    @Override
    public void onException(Exception e) {
    }

    @Override
    public Object performInBackground(Object[] params) throws Exception {
        File imageFile = null;
        ImageHelper imageHelper = (ImageHelper) params[0];
        ImageView imageView = (ImageView) params[1];

        try {
            imageFile = imageHelper.getFileFromImageView(imageView);
        } catch (IOException e) {
            onException(e);
        }

        return imageFile;
    }
}