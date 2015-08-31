package com.example.shenyichong.anypost;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;

/**
 * Created by shenyichong on 2015/8/28.
 */
public class Topbar extends RelativeLayout{

    private Button leftButton,rightButton;
    private TextView tvTitle;

//    private String leftText;
//    private int leftTextColor;
    private Drawable leftBackground;

//    private String rightText;
//    private int rightTextColor;
    private Drawable rightBackground;

    private String title;
    private float titleTextSize;
    private int titleTextColor;

    private RelativeLayout.LayoutParams leftParams, rightParams,titleParams;

    private topbarClickListener listener;

    public interface topbarClickListener{
        public void leftClick();
        public void rightClick();
    }

    public void setOnTopbarClickListener(topbarClickListener listener){
        this.listener = listener;
    }

    public Topbar(Context context, AttributeSet attrs) {
        super(context, attrs);
        TypedArray ta = context.obtainStyledAttributes(attrs,R.styleable.Topbar);

        //leftText = ta.getString(R.styleable.Topbar_leftText);
        //leftTextColor = ta.getColor(R.styleable.Topbar_leftTextColor, 0);
        leftBackground = ta.getDrawable(R.styleable.Topbar_leftBackground);

        //rightText = ta.getString(R.styleable.Topbar_rightText);
        //rightTextColor = ta.getColor(R.styleable.Topbar_rightTextColor, 0);
        rightBackground = ta.getDrawable(R.styleable.Topbar_rightBackground);

        title = ta.getString(R.styleable.Topbar_tiTle);
        titleTextColor = ta.getColor(R.styleable.Topbar_tiTleTextColor, 0);
        titleTextSize = ta.getDimension(R.styleable.Topbar_tiTleTextSize, 0);

        ta.recycle();

        leftButton = new Button(context);
        rightButton = new Button(context);
        tvTitle = new TextView(context);

        //leftButton.setTextColor(leftTextColor);
        leftButton.setBackground(leftBackground);
        //leftButton.setText(leftText);
        //rightButton.setTextColor(rightTextColor);
        rightButton.setBackground(rightBackground);
        //rightButton.setText(rightText);
        tvTitle.setText(title);
        tvTitle.setTextColor(titleTextColor);
        tvTitle.setTextSize(titleTextSize);
        tvTitle.setGravity(Gravity.CENTER);

        setBackgroundColor(0xFF3659A0);
        //setBackgroundResource(R.drawable.menu_dialog_button_red_pressed);

        leftParams = new RelativeLayout.LayoutParams(180, 120);
        leftParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT, TRUE);
        leftParams.leftMargin=30;
        leftParams.topMargin=25;
        addView(leftButton, leftParams);

        rightParams = new RelativeLayout.LayoutParams(180, 120);
        rightParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT, TRUE);
        rightParams.rightMargin=30;
        rightParams.topMargin=25;
        addView(rightButton, rightParams);

        titleParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.MATCH_PARENT);
        titleParams.addRule(RelativeLayout.CENTER_IN_PARENT, TRUE);
        addView(tvTitle, titleParams);

        rightButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.rightClick();
            }
        });

        leftButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.leftClick();
            }
        });
    }

    public void setLeftButtonVisible(boolean flag){
        if(flag)
            leftButton.setVisibility(View.VISIBLE);
        else
            leftButton.setVisibility(View.GONE);
    }

    public void setRightButtonVisible(boolean flag){
        if(flag)
            rightButton.setVisibility(View.VISIBLE);
        else
            rightButton.setVisibility(View.GONE);
    }
}
