package com.example.shenyichong.anypost;

import android.content.Context;
import android.util.AttributeSet;
import android.view.ViewGroup;

/**
 * Created by shenyichong on 2015/7/14.
 */
public class ImageShower extends ViewGroup{

    public ImageShower(Context context) {
        super(context);
    }

    public ImageShower(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ImageShower(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }
    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {

    }
}
