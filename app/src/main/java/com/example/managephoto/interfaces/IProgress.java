package com.example.managephoto.interfaces;

import android.view.View;
import android.widget.FrameLayout;

/**
 * 图片加载
 */
public interface IProgress {

    void attach(int position, FrameLayout parent);

    void onStart(int position);

    void onProgress(int position,int progress);

    void onFinish(int position);

    void onFailed(int position);

    View getProgressView(int position);
}
