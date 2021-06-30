package com.example.managephoto;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.managephoto.bean.ContentViewOriginBean;
import com.example.managephoto.bean.OneImageBean;
import com.example.managephoto.resource.Image;
import com.example.managephoto.view.DragView;

import me.panpf.sketch.Sketch;
import me.panpf.sketch.SketchImageView;
import me.panpf.sketch.cache.DiskCache;
import me.panpf.sketch.decode.ImageAttrs;
import me.panpf.sketch.drawable.SketchGifDrawable;
import me.panpf.sketch.request.CancelCause;
import me.panpf.sketch.request.DisplayListener;
import me.panpf.sketch.request.DownloadProgressListener;
import me.panpf.sketch.request.ErrorCause;
import me.panpf.sketch.request.ImageFrom;
import me.panpf.sketch.request.LoadListener;
import me.panpf.sketch.request.LoadRequest;
import me.panpf.sketch.request.LoadResult;
import me.panpf.sketch.util.SketchUtils;

//图片显示 fragment 内嵌 dragview
public class ImageFragment extends Fragment {

    DragView dragView;
    ContentViewOriginBean contentViewOriginBean;
    String url;
    SketchImageView sketchImageView;
    int position;
    int type = OneImageBean.PHOTO;
    //加载进度圈
    FrameLayout loadingLayout;
    boolean showAnimation = false;
    boolean hasCache;

    LoadRequest loadRequest;

    public DragView getDragView(){
        return dragView;
    }

    public static ImageFragment newInstance(String url,int position,int type,boolean showAnimation,ContentViewOriginBean contentViewOriginBean){
        Bundle args = new Bundle();
        args.putString("url",url);
        args.putInt("position",position);
        args.putBoolean("showAnimation",showAnimation);
        args.putInt("type",type);
        args.putParcelable("model",contentViewOriginBean);

        ImageFragment fragment = new ImageFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull  LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_image,container,false);
        if(getArguments() != null){
            url = getArguments().getString("url");
            position = getArguments().getInt("position");
            showAnimation = getArguments().getBoolean("showAnimation");
            type = getArguments().getInt("type");
            contentViewOriginBean = getArguments().getParcelable("model");
        }

        loadingLayout = view.findViewById(R.id.loadingLayout);
        dragView = view.findViewById(R.id.dragView);
        dragView.setPhoto(type == OneImageBean.PHOTO);
        if(ImageActivity.iProgress != null){
            ImageActivity.iProgress.attach(position,loadingLayout);
        }
        loadingLayout.setVisibility(View.GONE);

        sketchImageView = new SketchImageView(getContext());
        //默认播放gif
        sketchImageView.getOptions().setDecodeGifImage(true);
        //手势放大缩小移动
        sketchImageView.setZoomEnabled(true);
        dragView.addContentChildView(sketchImageView);
        //使用setPause减少内存消耗 初始化分块显示器的暂停状态
        sketchImageView.getZoomer().getBlockDisplayer().setPause(!isVisibleToUser());
        return view;
    }

    @Override
    public void onViewCreated(@NonNull  View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if(getContext() == null || getActivity() == null)
            return;
        //先显示缩略图 过渡
        if(OneImage.onLoadPhotoBeforeShowBigImageListener != null){
            if (dragView.getContentView() instanceof SketchImageView){
                OneImage.onLoadPhotoBeforeShowBigImageListener.loadView((SketchImageView)dragView.getContentView(),position);
            }else if(dragView.getContentParentView().getChildAt(1) instanceof SketchImageView){
                OneImage.onLoadPhotoBeforeShowBigImageListener.loadView((SketchImageView) dragView.getContentParentView().getChildAt(1),0);
                dragView.getContentParentView().getChildAt(1).setVisibility(View.VISIBLE);
            }
        }

        dragView.setOnShowFinishListener(new DragView.OnShowFinishListener() {
            @Override
            public void showFinish(DragView dragView, boolean showImmediately) {
                if(type == OneImageBean.PHOTO && dragView.getContentView() instanceof SketchImageView){
                    loadImage();
                }
            }
        });

        //拖动
        dragView.setOnDragListener(new DragView.OnDragListener() {
            @Override
            public void onDrag(DragView view, float moveX, float moveY) {
                Log.d("ImageActivity", "onDrag: 111");
                if(ImageActivity.iIndicator != null){
                    ImageActivity.iIndicator.move(moveX,moveY);
                }
            }
        });

        DiskCache diskCache = Sketch.with(getContext()).getConfiguration().getDiskCache();
        hasCache = type == OneImageBean.PHOTO && !((ImageActivity) getActivity()).isNeedAnimationForClickPosition(position) && diskCache.exist(url);
        if (hasCache) {
            ((ImageActivity) getActivity()).refreshNeedAnimation();
            loadImage();
        } else {
           dragView.putData(contentViewOriginBean.getLeft(), contentViewOriginBean.getTop(),contentViewOriginBean.getWidth(), contentViewOriginBean.getHeight());
            //如果显示的点击的position  ->进行动画处理
            dragView.show(!showAnimation);
        }

        dragView.setOnFinishListener(new DragView.OnFinishListener() {
            @Override
            public void callFinish() {
                if (getContext() instanceof ImageActivity) {
                    ((ImageActivity) getContext()).finishView();
                }
                if (OneImage.onFinishListener != null) {
                    OneImage.onFinishListener.finish(dragView);
                }
            }
        });
        dragView.setOnReleaseListener(new DragView.OnReleaseListener() {
            @Override
            public void onRelease(boolean isToMax, boolean isToMin) {
                if(ImageActivity.iIndicator != null){
                    ImageActivity.iIndicator.fingerRelease(isToMax,isToMin);
                }
            }
        });
    }

    /**
     * 图片加载
     */
    private void loadImage(){
        if(getContext() == null || sketchImageView == null)
            return;
        if(hasCache)
            loadWithCache();
        else
            loadWithoutCache();
    }
    /**
     * 缓存
     */
    private void loadWithCache(){
        sketchImageView.setDisplayListener(new DisplayListener() {
            @Override
            public void onStarted() {
                loadingLayout.setVisibility(View.VISIBLE);
                if(ImageActivity.iProgress != null){
                    ImageActivity.iProgress.onStart(position);
                }
            }

            @Override
            public void onCompleted(@NonNull Drawable drawable, @NonNull ImageFrom imageFrom, @NonNull ImageAttrs imageAttrs) {

                loadingLayout.setVisibility(View.GONE);
                if(ImageActivity.iProgress != null){
                    ImageActivity.iProgress.onFinish(position);
                }
                int width = drawable.getIntrinsicWidth();
                int height = drawable.getIntrinsicHeight();
                //不显示动画直接显示图片
                dragView.putData(
                        contentViewOriginBean.getLeft(),contentViewOriginBean.getTop(),
                        contentViewOriginBean.getWidth(),contentViewOriginBean.getHeight(),
                        width,height
                );
                dragView.show(true);
            }

            @Override
            public void onError(@NonNull ErrorCause cause) {
                if (ImageActivity.iProgress != null) {
                    ImageActivity.iProgress.onFailed(position);
                }
            }

            @Override
            public void onCanceled(@NonNull CancelCause cause) {

            }
        });

        sketchImageView.setDownloadProgressListener(new DownloadProgressListener() {
            @Override
            public void onUpdateDownloadProgress(int totalLength, int completedLength) {
                loadingLayout.setVisibility(View.VISIBLE);
                int ratio = (int)(completedLength / (float)totalLength*100);
                if(ImageActivity.iProgress != null){
                    ImageActivity.iProgress.onProgress(position,ratio);
                }
            }
        });
        sketchImageView.displayImage(url);

    }

    private void loadWithoutCache(){
        loadRequest = Sketch.with(getContext()).load(url, new LoadListener() {
            @Override
            public void onStarted() {
                loadingLayout.setVisibility(View.VISIBLE);
                if (ImageActivity.iProgress != null) {
                    ImageActivity.iProgress.onStart(position);
                }
            }

            @Override
            public void onCompleted(@NonNull LoadResult result) {
                loadingLayout.setVisibility(View.GONE);
                if (ImageActivity.iProgress != null) {
                    ImageActivity.iProgress.onFinish(position);
                }
                if (result.getGifDrawable() != null) {
                    result.getGifDrawable().followPageVisible(true, true);
                }
                int w = result.getBitmap().getWidth();
                int h = result.getBitmap().getHeight();
                dragView.notifySize(w, h);
                sketchImageView.displayImage(url);
                hasCache = true;
            }

            @Override
            public void onError(@NonNull ErrorCause cause) {
                if (ImageActivity.iProgress != null) {
                    ImageActivity.iProgress.onFailed(position);
                }
                sketchImageView.setImageResource(R.mipmap.image_error);
                sketchImageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
                sketchImageView.setZoomEnabled(false);
                dragView.setErrorImage(false);
            }

            @Override
            public void onCanceled(@NonNull CancelCause cause) {
            }
        }).downloadProgressListener(new DownloadProgressListener() {
            @Override
            public void onUpdateDownloadProgress(int totalLength, int completedLength) {
                loadingLayout.setVisibility(View.VISIBLE);
                int ratio = (int) (completedLength / (float) totalLength * 100);
                if (ImageActivity.iProgress != null) {
                    ImageActivity.iProgress.onProgress(position, ratio);
                }
            }
        }).commit();
    }
    public boolean isVisibleToUser(){
        //当前fragment对用户可见/不可见
        return isResumed() && getUserVisibleHint();

    }

    @Override
    public void onResume() {
        super.onResume();
        if(getUserVisibleHint()){
            onUserVisibleChanged(true);
        }
    }
    @Override
    public void onPause() {
        super.onPause();
        if(getUserVisibleHint()){
            onUserVisibleChanged(false);
        }
    }
    protected void onUserVisibleChanged(boolean isVisibleToUser) {
        // 不可见的时候暂停分块显示器，节省内存，可见的时候恢复
        if (sketchImageView != null && sketchImageView.isZoomEnabled()) {
            sketchImageView.getZoomer().getBlockDisplayer().setPause(!isVisibleToUser);
            Drawable lastDrawable = SketchUtils.getLastDrawable(sketchImageView.getDrawable());
            if (lastDrawable != null && (lastDrawable instanceof SketchGifDrawable)) {
                ((SketchGifDrawable) lastDrawable).followPageVisible(isVisibleToUser, false);
            }
        }
    }



    @Override
    public void onDestroyView() {
        if(loadRequest != null){
            loadRequest.cancel(CancelCause.ON_DETACHED_FROM_WINDOW);
            loadRequest = null;
        }
        super.onDestroyView();
    }
}
