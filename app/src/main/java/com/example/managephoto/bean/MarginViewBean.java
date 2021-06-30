package com.example.managephoto.bean;

import android.view.View;
import android.view.ViewGroup;

public class MarginViewBean {
    private ViewGroup.MarginLayoutParams params;
    private View view;

    public MarginViewBean(View view){
        this.view = view;
        params = (ViewGroup.MarginLayoutParams)view.getLayoutParams();
    }

    public int getWidth(){
        return params.width;
    }
    public void setWidth(float width){
        params.width = Math.round(width);
        view.setLayoutParams(params);
    }

    public int getHeight(){
        return params.height;
    }
    public void setHeight(float height){
        params.height = Math.round(height);
        view.setLayoutParams(params);
    }

    public int getMarginTop(){
        return params.topMargin;
    }
    public void setMarginTop(int top){
        params.topMargin = top;
        view.setLayoutParams(params);
    }

    public int getMarginBottom(){
        return params.bottomMargin;
    }
    public void setMarginBottom(int bottom){
        params.bottomMargin = bottom;
        view.setLayoutParams(params);
    }

    public int getMarginLeft(){
        return params.leftMargin;
    }
    public void setMarginLeft(int left){
        params.leftMargin = left;
        view.setLayoutParams(params);
    }

    public int getMarginRight(){
        return params.rightMargin;
    }
    public void setMarginRight(int right){
        params.rightMargin = right;
        view.setLayoutParams(params);
    }
}
