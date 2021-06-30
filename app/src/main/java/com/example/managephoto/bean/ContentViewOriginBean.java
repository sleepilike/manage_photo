package com.example.managephoto.bean;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * 序列化
 */
public class ContentViewOriginBean implements Parcelable {
    private int left;
    private int top;
    private int width;
    private int height;

    public ContentViewOriginBean(){

    }


    protected ContentViewOriginBean(Parcel in) {
        this.left = in.readInt();
        this.top = in.readInt();
        this.width = in.readInt();
        this.height = in.readInt();
    }

    public static final Creator<ContentViewOriginBean> CREATOR = new Creator<ContentViewOriginBean>() {
        @Override
        public ContentViewOriginBean createFromParcel(Parcel in) {
            return new ContentViewOriginBean(in);
        }

        @Override
        public ContentViewOriginBean[] newArray(int size) {
            return new ContentViewOriginBean[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(left);
        dest.writeInt(top);
        dest.writeInt(width);
        dest.writeInt(height);
    }

    public int getLeft() {
        return left;
    }
    public void setLeft(int left) {
        this.left = left;
    }

    public int getTop() {
        return top;
    }

    public void setTop(int top) {
        this.top = top;
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }
}
