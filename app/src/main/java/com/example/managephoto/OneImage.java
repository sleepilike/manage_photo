package com.example.managephoto;

import android.app.Activity;
import android.content.Context;
import android.content.ContextWrapper;
import android.view.View;
import android.view.Window;

import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.managephoto.bean.OneImageBean;
import com.example.managephoto.bean.ContentViewOriginBean;
import com.example.managephoto.interfaces.CircleIndexIndicator;
import com.example.managephoto.interfaces.DefaultPercentProgress;
import com.example.managephoto.interfaces.IIndicator;
import com.example.managephoto.interfaces.IProgress;
import com.example.managephoto.resource.Image;
import com.example.managephoto.view.DragView;

import java.util.ArrayList;
import java.util.List;

import me.panpf.sketch.SketchImageView;

public class OneImage {
    Context context;
    private OneImageBean oneImageBean;

    public static OnLoadPhotoBeforeShowBigImageListener onLoadPhotoBeforeShowBigImageListener;
    public static OnShowToMaxFinishListener onShowToMaxFinishListener;
    public static OnProvideViewListener onProvideViewListener;
    public static OnFinishListener onFinishListener;

    public OneImage(Context context){
        this.context = context;
        oneImageBean = new OneImageBean();
    }

    public OneImage imageUrls(String imageUrl){
        this.oneImageBean.setImageUrls(new String[]{imageUrl});
        return this;
    }
    public OneImage imageUrls(String[] imageUrls){
        this.oneImageBean.setImageUrls(imageUrls);
        return this;
    }

    public OneImage type(int type){
        this.oneImageBean.setType(type);
        return this;
    }

    public OneImage position(int position,int headerSize){
        this.oneImageBean.setPosition(position-headerSize);
        this.oneImageBean.setHeaderSize(headerSize);
        return this;
    }
    public OneImage position(int position){
        return position(position,0);
    }
    public OneImage immersive(boolean immersive){
        this.oneImageBean.setImmersive(immersive);
        return this;
    }
    public OneImage indicatorVisibility(int visibility){
        this.oneImageBean.setIndicatorVisibility(visibility);
        return this;
    }

    public OneImage views(View[] views){
        List<ContentViewOriginBean> list = new ArrayList<>();
        for(View imageView : views){
            ContentViewOriginBean imageBean = new ContentViewOriginBean();
            if (imageView == null){
                imageBean.setLeft(0);
                imageBean.setTop(0);
                imageBean.setWidth(0);
                imageBean.setHeight(0);
            }else {
                int location[] = new int[2];
                imageView.getLocationOnScreen(location);
                imageBean.setLeft(location[0]);
                imageBean.setTop(location[1]);
                imageBean.setWidth(imageView.getWidth());
                imageBean.setHeight(imageView.getHeight());
            }
            list.add(imageBean);
        }
        oneImageBean.setContentViewOriginBeans(list);
        return this;
    }
    public OneImage views(RecyclerView recyclerView,int viewId){
        List<View> imageViewList = new ArrayList<>();
        //一屏幕image数量
        int childCount = recyclerView.getChildCount();
        for(int i =0;i<childCount;i++){
            View v = (recyclerView.getChildAt(i)).findViewById(viewId);
            if(v != null){
                imageViewList.add(v);
            }
        }
        RecyclerView.LayoutManager layoutManager = recyclerView.getLayoutManager();
        int firstPos = 0;
        int lastPos = 0;
        int total = layoutManager.getItemCount() - oneImageBean.getHeaderSize();
        GridLayoutManager gridLayoutManager = (GridLayoutManager)layoutManager;
        firstPos = gridLayoutManager.findFirstVisibleItemPosition();
        lastPos = gridLayoutManager.findLastVisibleItemPosition();
        fillPlaceHolder(imageViewList,total,firstPos,lastPos);
        View[] views = new View[imageViewList.size()];
        for(int i=0;i<imageViewList.size();i++){
            views[i] = imageViewList.get(i);
        }
        return views(views);

    }

    private void fillPlaceHolder(List<View> imageViewList,int total,int first,int last){
        if(first > 0){
            for (int i=first;i>0;i--){
                imageViewList.add(0,null);
            }
        }
        if(last < total){
            for(int i=total-1-last;i>0;i--){
                imageViewList.add(null);
            }
        }
    }
    public OneImage loadPhotoBeforeShowBigImage(OnLoadPhotoBeforeShowBigImageListener on){
        onLoadPhotoBeforeShowBigImageListener = on;
        return this;
    }

    public OneImage start(){
        //图片下标标识栏
        if(ImageActivity.iIndicator == null){
            setIndicator(new CircleIndexIndicator());
        }
        //进度加载
        if(ImageActivity.iProgress == null){
            setProgress(new DefaultPercentProgress());
        }
        ImageActivity.startImageActivity(scanForActivity(context),oneImageBean);
        return this;

    }

    Activity scanForActivity(Context context) {
        if (context == null) return null;

        if (context instanceof Activity) {
            return (Activity) context;
        } else if (context instanceof ContextWrapper) {
            return scanForActivity(((ContextWrapper) context).getBaseContext());
        }
        return null;
    }

    public OneImage setIndicator(IIndicator indicator){
        ImageActivity.iIndicator = indicator;
        return this;
    }
    public OneImage setProgress(IProgress progress){
        ImageActivity.iProgress = progress;
        return this;
    }


    public interface OnLoadPhotoBeforeShowBigImageListener{
        void loadView(SketchImageView sketchImageView,int position);
    }
    public interface OnProvideViewListener {
        View provideView();
    }

    public interface OnShowToMaxFinishListener {
        void onShowToMax(DragView dragView, SketchImageView sketchImageView, View progressView);
    }

    public interface OnFinishListener {
        void finish(DragView dragView);
    }





}
