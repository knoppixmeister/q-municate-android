package com.quickblox.q_municate.core.listeners;

import com.quickblox.q_municate.utils.image.ImageSource;

public interface OnImageSourcePickedListener {

    void onImageSourcePicked(ImageSource source);

    void onImageSourceClosed();
}