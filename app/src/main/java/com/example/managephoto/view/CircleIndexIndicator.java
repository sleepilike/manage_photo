package com.example.managephoto.view;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;

import androidx.viewpager.widget.ViewPager;

import com.example.managephoto.interfaces.IIndicator;
import com.example.managephoto.utils.UIUtil;

import static android.view.ViewGroup.LayoutParams.WRAP_CONTENT;

//图片下标导航栏
public class CircleIndexIndicator implements IIndicator {

    private CircleIndicator circleIndicator;

    private int originBottomMargin = 10;
    private int currentBottomMargin = originBottomMargin;
    @Override
    public void attach(FrameLayout parent) {
        Log.d("CircleIndexIndicator", "attach: ");
        originBottomMargin = UIUtil.dipToPx(parent.getContext(),16);
        FrameLayout.LayoutParams indexLp = new FrameLayout.LayoutParams(WRAP_CONTENT, UIUtil.dipToPx(parent.getContext(), 36));
        indexLp.gravity = Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL;
        indexLp.bottomMargin = originBottomMargin;

        circleIndicator = new CircleIndicator(parent.getContext());
        circleIndicator.setGravity(Gravity.CENTER_VERTICAL);
        circleIndicator.setLayoutParams(indexLp);

        parent.addView(circleIndicator);
    }

    @Override
    public void onShow(ViewPager viewPager) {

        Log.d("CircleIndexIndicator", "onShow: ");
        circleIndicator.setVisibility(View.VISIBLE);
        circleIndicator.setViewPager(viewPager);
    }

    @Override
    public void move(float moveX, float moveY) {

        //在y轴上移动
        Log.d("CircleIndexIndicator", "move: ");
        if (circleIndicator == null) {
            return;
        }
        FrameLayout.LayoutParams indexLp = (FrameLayout.LayoutParams) circleIndicator.getLayoutParams();
        currentBottomMargin = Math.round(originBottomMargin - moveY / 6f);
        if (currentBottomMargin > originBottomMargin) {
            currentBottomMargin = originBottomMargin;
        }
        indexLp.bottomMargin = currentBottomMargin;
        circleIndicator.setLayoutParams(indexLp);
    }

    @Override
    public void fingerRelease(boolean isToMax, boolean isToMin) {

        Log.d("CircleIndexIndicator", "fingerRelease: ");
        if (circleIndicator == null) {
            return;
        }
        int begin = 0;
        int end = 0;
        if (isToMax) {
            begin = currentBottomMargin;
            end = originBottomMargin;
        }
        if (isToMin) {
            circleIndicator.setVisibility(View.GONE);
            return;
        }
        final FrameLayout.LayoutParams indexLp = (FrameLayout.LayoutParams) circleIndicator.getLayoutParams();
        ValueAnimator valueAnimator = ValueAnimator.ofInt(begin, end);
        valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                indexLp.bottomMargin = (int) animation.getAnimatedValue();
                circleIndicator.setLayoutParams(indexLp);
            }
        });

        valueAnimator.setDuration(300).start();
    }
}
