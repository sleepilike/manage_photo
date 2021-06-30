package com.example.managephoto;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentPagerAdapter;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;

import com.example.managephoto.bean.ContentViewOriginBean;
import com.example.managephoto.bean.OneImageBean;
import com.example.managephoto.interfaces.IIndicator;
import com.example.managephoto.interfaces.IProgress;
import com.example.managephoto.view.NoScrollViewPager;

import java.util.ArrayList;
import java.util.List;
//图片展示大图
public class ImageActivity extends AppCompatActivity {

    private NoScrollViewPager viewPager;
    List<ContentViewOriginBean> contentViewOriginBeans;
    List<ImageFragment> fragmentList;
    OneImageBean oneImageBean;
    FrameLayout indicatorLayout;

    static IIndicator iIndicator;
    static IProgress iProgress;

    boolean isNeedAnimation = true;

    public static void startImageActivity(Activity activity,OneImageBean oneImageBean){
        Intent intent = new Intent(activity,ImageActivity.class);
        intent.putExtra("config",oneImageBean);
        activity.startActivity(intent);
        //切换动画
        activity.overridePendingTransition(0,0);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //全屏显示 没有状态栏
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_image);
        init();

    }
    private void init(){
        viewPager = findViewById(R.id.viewPager);
        indicatorLayout = findViewById(R.id.indicatorLayout);
        oneImageBean = getIntent().getParcelableExtra("config");
        indicatorLayout.setVisibility(oneImageBean.getIndicatorVisibility());

        int currentPos = oneImageBean.getPosition();
        String[] imageUrls = oneImageBean.getImageUrls();
        contentViewOriginBeans = oneImageBean.getContentViewOriginUtils();
        fragmentList = new ArrayList<>();
        for(int i =0;i<contentViewOriginBeans.size();i++){
            ImageFragment imageFragment = ImageFragment.newInstance(
                    imageUrls[i],i,oneImageBean.getType(),
                    contentViewOriginBeans.size() == 1|| oneImageBean.getPosition() ==i,
                    contentViewOriginBeans.get(i)
            );
            fragmentList.add(imageFragment);
        }

        viewPager.setAdapter(new FragmentPagerAdapter(getSupportFragmentManager()) {
            @NonNull
            @Override
            public Fragment getItem(int position) {
                return fragmentList.get(position);
            }

            @Override
            public int getCount() {
                return fragmentList.size();
            }
        });
        viewPager.setCurrentItem(currentPos);

        if(iIndicator != null && contentViewOriginBeans.size() != 1){
            iIndicator.attach(indicatorLayout);
            iIndicator.onShow(viewPager);
        }
    }


    //判断第一次点击的时候是否需要动画  第一次动画
    // 后续viewpager滑动回到该页面的时候 没有动画
    public boolean isNeedAnimationForClickPosition(int position) {
        return isNeedAnimation && oneImageBean.getPosition() == position;
    }

    public void refreshNeedAnimation() {
        isNeedAnimation = false;
    }



    public void finishView(){
        if(OneImage.onFinishListener != null){
            OneImage.onFinishListener.finish(
                    fragmentList.get(viewPager.getCurrentItem()).getDragView()
            );
        }
        OneImage.onLoadPhotoBeforeShowBigImageListener = null;
        OneImage.onShowToMaxFinishListener = null;
        OneImage.onFinishListener = null;
        iIndicator = null;
        iProgress = null;
        finish();
        //动画
        overridePendingTransition(0,0);
    }
}