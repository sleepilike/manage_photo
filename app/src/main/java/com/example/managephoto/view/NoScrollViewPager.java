package com.example.managephoto.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.viewpager.widget.ViewPager;

public class NoScrollViewPager extends ViewPager {

    public NoScrollViewPager(@NonNull Context context) {
        super(context);
    }
    public NoScrollViewPager(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public void setCurrentItem(int item, boolean smoothScroll) {
        super.setCurrentItem(item, smoothScroll);
    }

    @Override
    public void setCurrentItem(int item) {
        super.setCurrentItem(item,false);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        try{
            switch (ev.getAction()){
                case MotionEvent.ACTION_DOWN:
                    break;
                case MotionEvent.ACTION_MOVE:
                    break;
                case MotionEvent.ACTION_UP:
                    break;
            }
            return super.onInterceptTouchEvent(ev);
        }catch (IllegalArgumentException e){
            e.printStackTrace();
        }
        return false;
    }
}
