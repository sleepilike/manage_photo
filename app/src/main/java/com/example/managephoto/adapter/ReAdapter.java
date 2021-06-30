package com.example.managephoto.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.managephoto.R;
import com.example.managephoto.utils.UIUtil;

import java.util.List;

import me.panpf.sketch.Sketch;
import me.panpf.sketch.SketchImageView;
import me.panpf.sketch.request.DisplayOptions;

public class ReAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder > {

    private Context context;
    private String[] remindList;
    private LayoutInflater layoutInflater;
    private View.OnClickListener listener;

    public ReAdapter(Context context,View.OnClickListener listener){
        this.context = context;
        layoutInflater = LayoutInflater.from(context);
        this.listener = listener;
    }

    public void setRemindList(String[] remindList) {
        this.remindList = remindList;
    }



    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull  ViewGroup parent, int viewType) {
        RecyclerView.ViewHolder holder = null;
        View v = layoutInflater.inflate(R.layout.item_grid,parent,false);
        holder = new ViewHolder(v);
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull  RecyclerView.ViewHolder holder, int position) {

        ViewHolder holder1 = (ViewHolder)holder;
        String itemUrl = remindList[position];
        FrameLayout.LayoutParams imageParams = (FrameLayout.LayoutParams) holder1.srcImageView.getLayoutParams();
        //设置图片的高度 宽高比
        imageParams.height = (UIUtil.getWidth(context) - UIUtil.dipToPx(context,16))/3;
        DisplayOptions displayOptions = new DisplayOptions();
        displayOptions.setLoadingImage(R.mipmap.ic_launcher);
        displayOptions.setErrorImage(R.mipmap.image_error);
        holder1.srcImageView.setShowGifFlagEnabled(R.mipmap.ic_gif);
        Sketch.with(context).display(itemUrl,holder1.srcImageView)
                .options(displayOptions)
                .commit();
        holder1.srcImageView.setOnClickListener(listener);
        holder1.srcImageView.setTag(position);
    }

    @Override
    public int getItemCount() {
        return remindList == null ? 0 : remindList.length;
    }

    class ViewHolder extends RecyclerView.ViewHolder{

        //采用SketchImageView
        SketchImageView srcImageView;
        public ViewHolder(@NonNull  View itemView) {
            super(itemView);
            srcImageView = itemView.findViewById(R.id.srcImageView);

        }
    }
}
