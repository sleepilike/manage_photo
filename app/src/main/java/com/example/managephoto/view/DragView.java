package com.example.managephoto.view;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.managephoto.R;
import com.example.managephoto.bean.MarginViewBean;

import me.panpf.sketch.SketchImageView;
import me.panpf.sketch.decode.ImageSizeCalculator;

//图片拖动
//内嵌SketchImageView
public class DragView extends FrameLayout {

    private float mAlpha = 0;
    //按下时的坐标
    private float mDownX;
    private float mDownY;
    //拖动的总距离
    private float mYDistanceTraveled;
    private float mXDistanceTraveled;
    //每次拖动时的移动距离
    private float mTranslateY;
    private float mTranslateX;

    private final float DEFAULT_MIN_SCALE = 0.7f;
    //在y轴上的最大平移
    private int MAX_TRANSLATE_Y = 0;
    private int MAX_Y = 0;

    private final long DEFAULT_DURATION = 300;
    long animationDuration = DEFAULT_DURATION;
    private int mOriginLeft;
    private int mOriginTop;
    private int mOriginHeight;
    private int mOriginWidth;

    //屏幕宽高
    private int screenWidth;
    private int screenHeight;
    private int targetImageTop;
    private int targetImageWidth;
    private int targetImageHeight;

    //上一次事件的y
    private int mLastY;

    int minWidth = 0;
    int minHeight = 0;

    int releaseLeft = 0;
    float releaseY = 0;
    int releaseWidth = 0;
    int releaseHeight = 0;
    int realWidth;
    int realHeight;

    //最小滑动距离
    int touchSlop = ViewConfiguration.getTouchSlop();

    int imageLeftOfAnimatorEnd = 0;
    int imageTopOfAnimatorEnd = 0;
    int imageWidthOfAnimatorEnd = 0;
    int imageHeightOfAnimatorEnd = 0;

    MarginViewBean marginViewBean;
    boolean isMultiFinger = false;
    boolean isDrag = false;
    boolean isLongHeightImage = false;//是否是高长图
    boolean isLongWidthImage = false;//是否是宽长图
    boolean isAnimating = false;//是否在动画中
    boolean isPhoto = false;
    private boolean errorImage = true;

    //图片背景
    FrameLayout contentLayout;
    View backgroundView;

    private OnFinishListener onFinishListener;
    private OnDragListener mDragListener;
    private OnShowFinishListener onShowFinishListener;
    private OnClickListener onClickListener;
    private OnReleaseListener onReleaseListener;


    public DragView(@NonNull Context context) {
        this(context,null);
    }
    public DragView(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context,attrs,0);
    }
    public DragView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        screenWidth = getResources().getDisplayMetrics().widthPixels;
        screenHeight = getResources().getDisplayMetrics().heightPixels;
        Log.d("screenWidth", "DragView: "+screenWidth);
        Log.d("screenHeight", "DragView: "+screenHeight);
        MAX_TRANSLATE_Y = screenHeight/6;
        MAX_Y = screenHeight-screenHeight/8;
        Log.d("MAX_TRANSLATE_Y", "DragView: "+MAX_TRANSLATE_Y);
        Log.d("MAX_Y", "DragView: "+MAX_Y);

        addView(LayoutInflater.from(getContext()).inflate(R.layout.content_item, null), 0);
        contentLayout = findViewById(R.id.contentLayout);
        backgroundView = findViewById(R.id.backgroundView);
        marginViewBean = new MarginViewBean(contentLayout);
    }

    //添加子view sketchView
    public void addContentChildView(View view){
        ViewGroup viewGroup = (ViewGroup) view.getParent();
        if(viewGroup != null){
            viewGroup.removeView(view);
        }
        if(view instanceof SketchImageView){
            SketchImageView sketchImageView = (SketchImageView)view;
            //sketchView 已实现放大缩小
            if(sketchImageView.getZoomer() != null){
                sketchImageView.getZoomer().setReadMode(true);
                sketchImageView.setOnClickListener(new View.OnClickListener(){

                    //单击退回原界面
                    @Override
                    public void onClick(View v) {
                        backToMin();
                    }
                });
            }
            //正常缩放比例
            sketchImageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
        }
        contentLayout.addView(view);
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        int y = (int) event.getY();
        View view = getContentView();
        if (view instanceof SketchImageView) {
            SketchImageView sketchImageView = (SketchImageView) view;
            //如果是长图  没有缩放到最小,则不给事件
            if (sketchImageView.getZoomer() != null) {
                if (isLongHeightImage || isLongWidthImage) {
                    if (sketchImageView.getZoomer().getZoomScale() > sketchImageView.getZoomer().getMinZoomScale()) {
                        return super.dispatchTouchEvent(event);
                    }
                } else if ((Math.round(sketchImageView.getZoomer().getSupportZoomScale() * 1000) / 1000f) > 1) {
                    //如果对图片进行缩放或者缩小操作 则不给事件
                    return super.dispatchTouchEvent(event);
                }
            }
        }
        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_POINTER_DOWN:
                isMultiFinger = true;
                break;
            case MotionEvent.ACTION_DOWN:

                mDownX = event.getX();
                mDownY = event.getY();
                mTranslateX = 0;
                mTranslateY = 0;
                //触摸背景捕捉事件
                if (!isTouchPointInContentLayout(contentLayout, event)) {
                    mLastY = y;
                    return true;
                }
                break;
            case MotionEvent.ACTION_MOVE:
                float moveX = event.getX();
                float moveY = event.getY();
                mTranslateX = moveX - mDownX;
                mTranslateY = moveY - mDownY;
                mYDistanceTraveled += Math.abs(mTranslateY);
                mXDistanceTraveled += Math.abs(mTranslateX);
                if (isAnimating) {
                    break;
                }

                if (view instanceof SketchImageView && (isLongHeightImage || isLongWidthImage)) {
                    //长图缩放到最小比例  拖动时显示方式需要更新  同时不能启用阅读模式
                    SketchImageView sketchImageView = (SketchImageView) view;
                    if (sketchImageView.getZoomer() != null) {
                        sketchImageView.getZoomer().setReadMode(false);
                    }
                    changeImageViewToFitCenter();

                }
                if (event.getPointerCount() != 1 || isMultiFinger) {
                    isMultiFinger = true;
                    break;
                }

                //如果滑动距离不足,则不需要事件
                if (Math.abs(mYDistanceTraveled) < touchSlop || (Math.abs(mTranslateX) > Math.abs(mYDistanceTraveled) && !isDrag)) {
                    mYDistanceTraveled = 0;
                    if (isTouchPointInContentLayout(contentLayout, event)) {
                        break;
                    }
                    break;
                }
                if (mDragListener != null) {
                    mDragListener.onDrag(this, mTranslateX, mTranslateY);
                }
                isDrag = true;
                int dy = y - mLastY;
                int newMarY = marginViewBean.getMarginTop() + dy;

                //根据移动距离和屏幕的比例来更改背景透明度
                float alphaChangePercent = mTranslateY / screenHeight;
                //往上不透明
                mAlpha = 1 - alphaChangePercent;
                dragAnd2Normal(newMarY, true);
                break;
            case MotionEvent.ACTION_UP:
                if (isAnimating) {
                    break;
                }
                //如果滑动距离不足,则不需要事件
                if (Math.abs(mYDistanceTraveled) < touchSlop || (Math.abs(mYDistanceTraveled) > Math.abs(mYDistanceTraveled) && !isDrag)) {
                    Log.d("TAG", "dispatchTouchEvent: noenough");
                    if (!isMultiFinger && onClickListener != null) {
                        onClickListener.onClick(DragView.this);
                    }
                    isMultiFinger = false;
                    if (isTouchPointInContentLayout(contentLayout, event)) {
                        break;
                    }
                    break;
                }
                //防止滑动时出现多点触控
                if (isMultiFinger && !isDrag) {
                    isMultiFinger = false;
                    break;
                }
                isMultiFinger = false;
                if (mTranslateY > MAX_TRANSLATE_Y) {
                    //缩略图
                    backToMin();
                } else {
                    //正常图片显示
                    backToNormal();
                }
                isDrag = false;
                mYDistanceTraveled = 0;
                break;
        }

        mLastY = y;
        return super.dispatchTouchEvent(event);
    }

    //触摸是否在范围内
    private boolean isTouchPointInContentLayout(View v,MotionEvent event){
        float x = event.getX();
        float y = event.getY();
        if(v == null){
            return  false;
        }
        int[] location = new int[2];
        v.getLocationOnScreen(location);
        int left = location[0];
        int top = location[1];
        int right = left + v.getMeasuredWidth();
        int bottom = top + v.getMeasuredHeight();
        return y>=top && y<=bottom && x>=left && x<=right;
    }

    //拖放 只能在y轴上
    void dragAnd2Normal(float currentY, boolean isDrag) {
        float nodeMarginPercent = (MAX_Y - currentY + targetImageTop) / MAX_Y;
        float widthPercent = DEFAULT_MIN_SCALE + (1f - DEFAULT_MIN_SCALE) * nodeMarginPercent;
        int originLeftOffset = (screenWidth - targetImageWidth) / 2;
        int leftOffset = (int) ((targetImageWidth - targetImageWidth * widthPercent) / 2);

        float left;
        if (nodeMarginPercent >= 1) {
            //处于拖动到正常大小上方
            marginViewBean.setWidth(targetImageWidth);
            marginViewBean.setHeight(targetImageHeight);
            left = mTranslateX;
            mAlpha = nodeMarginPercent;
        } else {
            marginViewBean.setWidth(targetImageWidth * widthPercent);
            marginViewBean.setHeight(targetImageHeight * widthPercent);
            left = mTranslateX + leftOffset;
        }
        if (!isDrag) {
            left = (currentY - targetImageTop) / (releaseY - targetImageTop) * releaseLeft;
        }
        backgroundView.setAlpha(mAlpha);
        marginViewBean.setMarginLeft(Math.round(left + originLeftOffset));
        marginViewBean.setMarginTop((int) (currentY));
    }
    //显示缩略图
    private void backToNormal() {

        isAnimating = true;
        releaseLeft = marginViewBean.getMarginLeft() - (screenWidth - targetImageWidth) / 2;
        releaseY = marginViewBean.getMarginTop();
        ValueAnimator valueAnimator = ValueAnimator.ofFloat(marginViewBean.getMarginTop(), targetImageTop);
        valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float value = (float) animation.getAnimatedValue();
                dragAnd2Normal(value, false);
            }
        });
        valueAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                isAnimating = false;
            }
        });
        valueAnimator.setDuration(animationDuration).start();
        if (onReleaseListener != null) {
            onReleaseListener.onRelease(true, false);
        }
        changeBackgroundViewAlpha(false);
    }



    public void backToMin(){
        if(isAnimating){
            return;
        }
        //到最小的时候，先把imageview大小设为imageview可见的大小，不包含黑色空隙
        if(isPhoto){
            //marginViewBean.getHeight() 经过拖动缩放后的高度
            float dragToReleaseScale = marginViewBean.getHeight() / (float)screenHeight;
            if(marginViewBean.getHeight() != imageHeightOfAnimatorEnd){
                releaseHeight = (int)(dragToReleaseScale*imageHeightOfAnimatorEnd);
            }else {
                releaseHeight = marginViewBean.getHeight();
            }
            if(marginViewBean.getWidth() != imageWidthOfAnimatorEnd){
                releaseWidth = (int)(dragToReleaseScale*imageWidthOfAnimatorEnd);
            }else {
                releaseWidth = marginViewBean.getWidth();
            }
            if(marginViewBean.getMarginTop() != imageTopOfAnimatorEnd){
                releaseY = marginViewBean.getMarginTop() + (int)(dragToReleaseScale*imageTopOfAnimatorEnd);
            }else {
                releaseY = marginViewBean.getMarginTop();
            }
            if(marginViewBean.getMarginLeft() != imageLeftOfAnimatorEnd){
                releaseLeft = marginViewBean.getMarginLeft() + (int)(dragToReleaseScale*imageLeftOfAnimatorEnd);
            }else {
                releaseLeft = marginViewBean.getMarginLeft();
            }
            marginViewBean.setWidth(releaseWidth);
            marginViewBean.setHeight(releaseHeight);
            marginViewBean.setMarginTop((int)releaseY);
            marginViewBean.setMarginLeft(releaseLeft);
        }
        if((isLongHeightImage || isLongWidthImage)
            && getContentView() instanceof SketchImageView){
            SketchImageView sketchImageView = (SketchImageView)getContentView();
            if(sketchImageView.getZoomer() != null){
                //长图 重新修改宽高 长图缩放到最小时 大小需要变化
                float ratio = sketchImageView.getZoomer().getZoomScale()/sketchImageView.getZoomer().getMaxZoomScale();
                if(isLongHeightImage){
                    int tempWidth = (int)(screenWidth * ratio);
                    releaseLeft = releaseLeft + (releaseWidth-tempWidth)/2;
                    releaseWidth = tempWidth;
                }else {
                    int tempHeight = (int)(screenHeight * ratio);
                    releaseY = releaseY + (releaseHeight - tempHeight)/2;
                    releaseHeight = tempHeight;
                }
                changeImageViewToCenterCrop();
            }
        }
        if(errorImage){
            changeImageViewToCenterCrop();
            ValueAnimator valueAnimator = ValueAnimator.ofFloat(releaseY, mOriginTop);
            valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    float value = (float) animation.getAnimatedValue();
                    min2NormalAndDrag2Min(value, releaseY, mOriginTop, releaseLeft, mOriginLeft, releaseWidth, mOriginWidth, releaseHeight, mOriginHeight);
                }
            });
            valueAnimator.setDuration(animationDuration).start();
        }

        if(onReleaseListener != null){
            onReleaseListener.onRelease(false,true);
        }
        changeBackgroundViewAlpha(true);
    }

    void min2NormalAndDrag2Min(float currentY, float startY, float endY, float startLeft, float endLeft,
                               float startWidth, float endWidth, float startHeight, float endHeight) {
        min2NormalAndDrag2Min(false, currentY, startY, endY, startLeft, endLeft, startWidth, endWidth, startHeight, endHeight);
    }

    void min2NormalAndDrag2Min(float endY, float endLeft, float endWidth, float endHeight) {
        min2NormalAndDrag2Min(true, 0, 0, endY, 0, endLeft, 0, endWidth, 0, endHeight);
    }

    void min2NormalAndDrag2Min(boolean showImmediately, float currentY, float startY, float endY, float startLeft, float endLeft,
                               float startWidth, float endWidth, float startHeight, float endHeight) {
        if (endY == startY) {
            return;
        }
        if (showImmediately) {
            marginViewBean.setWidth(endWidth);
            marginViewBean.setHeight(endHeight);
            marginViewBean.setMarginLeft((int) (endLeft));
            marginViewBean.setMarginTop((int) endY);
            return;
        }
        float yPercent = (currentY - startY) / (endY - startY);
        float xOffset = yPercent * (endLeft - startLeft);
        float widthOffset = yPercent * (endWidth - startWidth);
        float heightOffset = yPercent * (endHeight - startHeight);
        marginViewBean.setWidth(startWidth + widthOffset);
        marginViewBean.setHeight(startHeight + heightOffset);
        marginViewBean.setMarginLeft((int) (startLeft + xOffset));
        marginViewBean.setMarginTop((int) currentY);
    }
    private void changeImageViewToCenterCrop() {

        if (getContentView() instanceof SketchImageView) {
            //正常缩放比例
            ((SketchImageView) getContentView()).setScaleType(ImageView.ScaleType.CENTER_CROP);
        }
    }
    private void changeBackgroundViewAlpha(final boolean isToZero){
        final float end = isToZero ?0:1f;
        ValueAnimator valueAnimator = ValueAnimator.ofFloat(mAlpha,end);
        valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                isAnimating = true;
                mAlpha = (Float)valueAnimator.getAnimatedValue();
                backgroundView.setAlpha(mAlpha);
            }
        });
        valueAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                isAnimating = false;
                if(isToZero){
                    setVisibility(View.GONE);
                    if(onFinishListener != null){
                        onFinishListener.callFinish();
                    }
                }
            }
        });
        valueAnimator.setDuration(animationDuration);
        valueAnimator.start();
    }

    public void putData(int left, int top, int originWidth, int originHeight) {
        putData(left, top, originWidth, originHeight, 0, 0);
    }
    public void putData(int left, int top, int originWidth, int originHeight, int realWidth, int realHeight) {
        this.realWidth = realWidth;
        this.realHeight = realHeight;
        mOriginLeft = left;
        mOriginTop = top;
        mOriginWidth = originWidth;
        mOriginHeight = originHeight;
    }
    public void show(boolean show){
        setVisibility(View.VISIBLE);
        mAlpha = show ? 1:0;
        getLocation(mOriginWidth,mOriginHeight,show);
    }
    private void getLocation(float minViewWidth,float minViewHeight,boolean show){
        int[] locationImage = new int[2];
        contentLayout.getLocationOnScreen(locationImage);
        float targetSize;
        targetImageWidth = screenWidth;
        if(realHeight !=0 && realWidth !=0){
            notifySize(realWidth,realHeight,true);
            return;
        }else {
            targetSize = minViewHeight / minViewWidth;
            targetImageHeight = (int) (screenWidth * targetSize);
            targetImageTop = (screenHeight - targetImageHeight) / 2;
        }

        marginViewBean.setWidth(mOriginWidth);
        marginViewBean.setHeight(mOriginHeight);
        marginViewBean.setMarginLeft(mOriginLeft);
        marginViewBean.setMarginTop(mOriginTop);

        minWidth = (int) (targetImageWidth * DEFAULT_MIN_SCALE);
        minHeight = (int) (targetImageHeight * DEFAULT_MIN_SCALE);
        if (show) {
            mAlpha = 1f;
            backgroundView.setAlpha(mAlpha);
            min2NormalAndDrag2Min(targetImageTop, 0, targetImageWidth, targetImageHeight);
            if (onShowFinishListener != null) {
                onShowFinishListener.showFinish(this, true);
            }
        } else {
            ValueAnimator valueAnimator = ValueAnimator.ofFloat(mOriginTop, targetImageTop);
            valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    float value = (float) animation.getAnimatedValue();
                    min2NormalAndDrag2Min(value, mOriginTop, targetImageTop, mOriginLeft, 0,
                            mOriginWidth, targetImageWidth, mOriginHeight, targetImageHeight);
                }
            });
            valueAnimator.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    isAnimating = false;
                    if (onShowFinishListener != null) {
                        onShowFinishListener.showFinish(DragView.this, false);
                    }
                }
            });
            valueAnimator.setDuration(animationDuration).start();
            changeBackgroundViewAlpha(false);
        }
    }


    public void notifySize(int width,int height){
        notifySize(width,height,false);
    }
    public void notifySize(int width,int height,boolean show){
        realWidth = width;
        realHeight = height;
        if(realWidth == 0 || realHeight == 0)
            return;
        int newWidth;
        int newHeight;
        ImageSizeCalculator imageSizeCalculator = new ImageSizeCalculator();
        if(imageSizeCalculator.canUseReadModeByHeight(realWidth,realHeight) ||
            imageSizeCalculator.canUseReadModeByWidth(realWidth,realHeight) ||
            screenWidth/(float) screenHeight < realWidth/(float)realHeight){

            isLongHeightImage = imageSizeCalculator.canUseReadModeByHeight(realWidth,realHeight)
                    && getContentView() instanceof SketchImageView;
            isLongWidthImage = imageSizeCalculator.canUseReadModeByWidth(realWidth,realHeight)
                    && getContentView() instanceof SketchImageView;
            newWidth = screenWidth;
            //比例
            newHeight = (int) (newWidth * (realHeight/(float)realWidth));
            if(newHeight >= screenHeight || imageSizeCalculator.canUseReadModeByWidth(realWidth,realHeight)){
                newHeight = screenHeight;
            }
        }else {
            newHeight = screenHeight;
            newWidth = (int)(newHeight * (realWidth/(float)realHeight));
        }

        final int endLeft = (screenWidth - newWidth) / 2;
        final int endHeight = newHeight;
        final int endWidth = newWidth;

        if(show){
            targetImageHeight = endHeight;
            targetImageWidth = endWidth;
            targetImageTop = (screenHeight - targetImageHeight) / 2;
            marginViewBean.setHeight(targetImageHeight);
            marginViewBean.setWidth(endWidth);
            marginViewBean.setMarginTop(targetImageTop);
            marginViewBean.setMarginLeft(endLeft);
            if (isPhoto) {
                setImageDataOfAnimatorEnd();
                changeContentViewToFullscreen();
            }
            return;
        }

        //动画
        ValueAnimator animator = ValueAnimator.ofInt(targetImageTop, (screenHeight - endHeight) / 2);
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                isAnimating = true;
                int value = (int) valueAnimator.getAnimatedValue();
                min2NormalAndDrag2Min(value, targetImageTop, (screenHeight - endHeight) / 2,
                        0, endLeft, targetImageWidth, endWidth,
                        targetImageHeight, endHeight);
            }
        });
        animator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                isAnimating = false;
                setImageDataOfAnimatorEnd();
                if (isPhoto) {
                    changeContentViewToFullscreen();
                } else {
                    targetImageHeight = endHeight;
                    targetImageWidth = endWidth;
                    targetImageTop = (screenHeight - targetImageHeight) / 2;
                }
            }
        });
        animator.setDuration(animationDuration);
        animator.start();

    }

    private void setImageDataOfAnimatorEnd() {
        imageLeftOfAnimatorEnd = marginViewBean.getMarginLeft();
        imageTopOfAnimatorEnd = marginViewBean.getMarginTop();
        imageWidthOfAnimatorEnd = marginViewBean.getWidth();
        imageHeightOfAnimatorEnd = marginViewBean.getHeight();
    }
    private void changeContentViewToFullscreen() {
        targetImageHeight = screenHeight;
        targetImageWidth = screenWidth;
        targetImageTop = 0;
        changeImageViewToFitCenter();
        marginViewBean.setHeight(screenHeight);
        marginViewBean.setWidth(screenWidth);
        marginViewBean.setMarginTop(0);
        marginViewBean.setMarginLeft(0);
    }
    private void changeImageViewToFitCenter() {
        if (getContentView() instanceof SketchImageView) {
            ((SketchImageView) getContentView()).setScaleType(ImageView.ScaleType.FIT_CENTER);
        }
    }
    /**
     * 获取可滑动view中添加 的子view
     * @return
     */
    public View getContentView(){
        return contentLayout.getChildAt(0);
    }
    public ViewGroup getContentParentView(){
        return contentLayout;
    }


    public boolean isErrorImage() {
        return errorImage;
    }

    public void setErrorImage(boolean errorImage) {
        this.errorImage = errorImage;
    }
    public boolean isPhoto() {
        return isPhoto;
    }

    public void setPhoto(boolean photo) {
        isPhoto = photo;
    }


    public interface OnShowFinishListener {
        void showFinish(DragView dragView, boolean showImmediately);
    }

    public interface OnDragListener {
        void onDrag(DragView view, float moveX, float moveY);
    }

    public interface OnFinishListener {
        void callFinish();
    }

    public interface OnReleaseListener {
        void onRelease(boolean isToMax, boolean isToMin);
    }

    public interface OnClickListener {
        void onClick(DragView dragView);
    }

    public void setOnFinishListener(OnFinishListener onFinishListener) {
        this.onFinishListener = onFinishListener;
    }
    public void setOnReleaseListener(OnReleaseListener onReleaseListener) {
        this.onReleaseListener = onReleaseListener;
    }



    public void setOnShowFinishListener(OnShowFinishListener onShowFinishListener) {
        this.onShowFinishListener = onShowFinishListener;
    }

    public void setOnDragListener(OnDragListener listener) {
        mDragListener = listener;
    }
}
