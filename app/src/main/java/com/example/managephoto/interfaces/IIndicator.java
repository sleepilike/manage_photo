package com.example.managephoto.interfaces;

import android.widget.FrameLayout;

import androidx.viewpager.widget.ViewPager;

//图片下标指示器
public interface IIndicator {

    void attach(FrameLayout parent);

    void onShow(ViewPager viewPager);

    /**
     * 拖动时 移动的x y距离
     * @param moveX
     * @param moveY
     */
    void move(float moveX,float moveY);

    /**
     * 手指松开
     * @param isToMax
     * @param isToMin
     */
    void fingerRelease(boolean isToMax,boolean isToMin);
}
