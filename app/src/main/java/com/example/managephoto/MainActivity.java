package com.example.managephoto;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Activity;
import android.graphics.Rect;
import android.os.Bundle;
import android.view.View;

import com.example.managephoto.adapter.ReAdapter;
import com.example.managephoto.bean.OneImageBean;
import com.example.managephoto.resource.Image;
import com.gyf.barlibrary.ImmersionBar;

import me.panpf.sketch.SketchImageView;

public class MainActivity extends Activity implements View.OnClickListener {

    private RecyclerView recyclerView;
    private String[] images = Image.images;
    private ReAdapter adapter;
    private int spanCount;

    //沉浸式状态栏
    protected ImmersionBar immersionBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        init();

    }

    private void init(){
        spanCount = 3;
        immersionBar = ImmersionBar.with(this)
                .statusBarColor(R.color.transparent)
                .fitsSystemWindows(true);//适配 不加会截断
        immersionBar.init();

        recyclerView = findViewById(R.id.recycleView);

        adapter = new ReAdapter(this,this);

        GridLayoutManager gridLayoutManager = new GridLayoutManager(this,spanCount);
        recyclerView.setLayoutManager(gridLayoutManager);
        recyclerView.addItemDecoration(new RecyclerView.ItemDecoration() {
            @Override
            public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
                outRect.set((int) getResources().getDimension(R.dimen.divide), (int) getResources().getDimension(R.dimen.divide), (int) getResources().getDimension(R.dimen.divide), (int) getResources().getDimension(R.dimen.divide));
            }
        });

        adapter.setRemindList(images);
        recyclerView.setAdapter(adapter);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.srcImageView:
                int pos = (int)v.getTag();
                OneImage oneImage = new OneImage(MainActivity.this)
                        .indicatorVisibility(View.VISIBLE)
                        .imageUrls(images)
                        .type(OneImageBean.PHOTO)
                        .immersive(true)
                        .position(pos,0)
                        .views(recyclerView,R.id.srcImageView)
                        .loadPhotoBeforeShowBigImage(new OneImage.OnLoadPhotoBeforeShowBigImageListener() {
                            @Override
                            public void loadView(SketchImageView sketchImageView, int position) {
                                sketchImageView.displayImage(images[pos]);
                                //可以添加长按事件
                            }
                        }).start();

        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        immersionBar.destroy();
    }
}