package com.example.managephoto.bean;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.List;

public class OneImageBean implements Parcelable {

    public static int PHOTO = 1;
    private int type = PHOTO;
    private String[] imageUrls;
    private boolean isFullScreen = false;
    private List<ContentViewOriginBean> contentViewOriginBeans;
    private int position;
    //沉浸式
    private boolean immersive;
    private int headerSize;
    private int indicatorVisibility;

    public OneImageBean(){

    }
    protected OneImageBean(Parcel in) {
        type = in.readInt();
        imageUrls = in.createStringArray();
        isFullScreen = in.readByte() != 0;
        contentViewOriginBeans = in.createTypedArrayList(ContentViewOriginBean.CREATOR);
        position = in.readInt();
        immersive = in.readByte() != 0;
        headerSize = in.readInt();
        indicatorVisibility = in.readInt();
    }

    public static final Creator<OneImageBean> CREATOR = new Creator<OneImageBean>() {
        @Override
        public OneImageBean createFromParcel(Parcel in) {
            return new OneImageBean(in);
        }

        @Override
        public OneImageBean[] newArray(int size) {
            return new OneImageBean[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(type);
        dest.writeStringArray(imageUrls);
        dest.writeByte((byte) (isFullScreen ? 1 : 0));
        dest.writeTypedList(contentViewOriginBeans);
        dest.writeInt(position);
        dest.writeByte((byte) (immersive ? 1 : 0));
        dest.writeInt(headerSize);
        dest.writeInt(indicatorVisibility);
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String[] getImageUrls() {
        return imageUrls;
    }

    public void setImageUrls(String[] imageUrls) {
        this.imageUrls = imageUrls;
    }
    public boolean isFullScreen() {
        return isFullScreen;
    }

    public void setFullScreen(boolean fullScreen) {
        isFullScreen = fullScreen;
    }

    public List<ContentViewOriginBean> getContentViewOriginUtils() {
        return contentViewOriginBeans;
    }

    public void setContentViewOriginBeans(List<ContentViewOriginBean> contentViewOriginBeans) {
        this.contentViewOriginBeans = contentViewOriginBeans;
    }

    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
    }

    public boolean isImmersive() {
        return immersive;
    }

    public void setImmersive(boolean immersive) {
        this.immersive = immersive;
    }

    public int getHeaderSize() {
        return headerSize;
    }

    public void setHeaderSize(int headerSize) {
        this.headerSize = headerSize;
    }

    public int getIndicatorVisibility() {
        return indicatorVisibility;
    }

    public void setIndicatorVisibility(int indicatorVisibility) {
        this.indicatorVisibility = indicatorVisibility;
    }
}
