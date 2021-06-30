package com.example.managephoto.utils;

import android.content.Context;
import android.view.Window;
import android.view.WindowManager;

public class UIUtil {

    /**
     * 获取窗口宽度
     * @param context
     * @return
     */
    public static int getWidth(Context context){
        WindowManager windowManager = (WindowManager)context.getSystemService(Context.WINDOW_SERVICE);
        int width = windowManager.getDefaultDisplay().getWidth();
        return width;
    }

    /**
     * 将dip 转为 px
     * @param context
     * @param dipV
     * @return
     */
    public static int dipToPx(Context context,float dipV){
        float scale = context.getResources().getDisplayMetrics().density;
        return (int)(dipV * scale + 0.5f);
    }



}
