package com.zwb.arcmenu.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.RotateAnimation;
import android.view.animation.ScaleAnimation;
import android.view.animation.TranslateAnimation;
import android.widget.Toast;

import com.zwb.arcmenu.R;

import static com.zwb.arcmenu.view.ArcMenu.Position.LEFT_BOTTOM;
import static com.zwb.arcmenu.view.ArcMenu.Position.RIGHT_BOTTOM;
import static com.zwb.arcmenu.view.ArcMenu.Position.RIGHT_TOP;

/**
 * Created by zwb
 * Description 卫星菜单
 * Date 2017/5/23.
 */

public class ArcMenu extends ViewGroup implements View.OnClickListener {
    private int radius = 100;//半径
    private int position;//显示的位置
    private Position curPosition = Position.LEFT_TOP;
    private int mWidth;//控件的宽度
    private int mHeight;//控件的高度
    private View childOne;
    private Status curStatus = Status.STATUS_CLOSE;

    public enum Position {
        LEFT_TOP, LEFT_BOTTOM, RIGHT_BOTTOM, RIGHT_TOP;
    }

    public enum Status {
        STATUS_CLOSE, STATUS_OPEN;
    }

    public ArcMenu(Context context) {
        this(context, null);
    }

    public ArcMenu(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ArcMenu(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }


    private void init(Context context, AttributeSet attrs) {
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.ArcMenu);
        int count = a.getIndexCount();
        for (int i = 0; i < count; i++) {
            int attr = a.getIndex(i);
            switch (attr) {
                case R.styleable.ArcMenu_position:
                    position = a.getInt(attr, 0);
                    if (position == 0) {
                        curPosition = Position.LEFT_TOP;
                    } else if (position == 1) {
                        curPosition = LEFT_BOTTOM;
                    } else if (position == 2) {
                        curPosition = Position.RIGHT_TOP;
                    } else if (position == 3) {
                        curPosition = Position.RIGHT_BOTTOM;
                    }
                    break;
                case R.styleable.ArcMenu_radius:
                    radius = a.getDimensionPixelSize(attr,
                            (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                                    100, getResources().getDisplayMetrics()));
                    break;
            }
        }
        a.recycle();
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        if (changed) {
            layoutButton();
            int count = getChildCount();
            //除去第一个button
            for (int i = 0; i < count - 1; i++) {
                View child = getChildAt(i + 1);
                child.setVisibility(GONE);
                //Math.PI/180得到的结果就是1°  这里用Math.PI来计算表示180°，
                // 显示的范围为90度，所以是以90来平分的 Math.PI / 2 为90度
                int left = (int) ((Math.sin(Math.PI / 2 / (count - 2) * i)) * radius);
                int top = (int) ((Math.cos(Math.PI / 2 / (count - 2) * i)) * radius);
                //左下，右上
                if (curPosition == LEFT_BOTTOM || curPosition == RIGHT_BOTTOM) {
                    top = getMeasuredHeight() - top - child.getMeasuredHeight();
                }
                //右上，右下
                if (curPosition == RIGHT_TOP || curPosition == RIGHT_BOTTOM) {
                    left = getMeasuredWidth() - left - child.getMeasuredWidth();
                }
                child.layout(left, top, left + child.getMeasuredWidth(), top + child.getMeasuredHeight());

            }
        }
    }

    /**
     * 点击按钮的布局
     */
    private void layoutButton() {
        childOne = getChildAt(0);
        childOne.setOnClickListener(this);
        int l = 0;
        int t = 0;
        switch (curPosition) {
            case LEFT_TOP:
                l = 0;
                t = 0;
                break;
            case LEFT_BOTTOM:
                l = 0;
                t = getMeasuredHeight() - childOne.getMeasuredHeight();
                break;
            case RIGHT_TOP:
                l = getMeasuredWidth() - childOne.getMeasuredWidth();
                t = 0;
                break;
            case RIGHT_BOTTOM:
                l = getMeasuredWidth() - childOne.getMeasuredWidth();
                t = getMeasuredHeight() - childOne.getMeasuredHeight();
                break;
        }
        childOne.layout(l, t, l + childOne.getMeasuredWidth(), t + childOne.getMeasuredHeight());
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int count = getChildCount();
        for (int i = 0; i < count; i++) {
            View child = getChildAt(i);
            measureChild(child, widthMeasureSpec, heightMeasureSpec);
        }
        if (count >= 2) {
            mWidth = radius + getChildAt(0).getMeasuredWidth() / 2 + getChildAt(1).getMeasuredWidth() / 2;
            mHeight = radius + getChildAt(0).getMeasuredHeight() / 2 + getChildAt(1).getMeasuredHeight() / 2;
        }
        setMeasuredDimension(mWidth, mHeight);
    }

    @Override
    public void onClick(View v) {
        rotateView(v, 0, 270, 300);
        switchMenu(300);
    }

    /**
     * 旋转动画
     *
     * @param view
     * @param fromDegrees
     * @param toDegrees
     * @param duration
     */
    private void rotateView(View view, int fromDegrees, int toDegrees, int duration) {
        RotateAnimation rotateAnimation = new RotateAnimation(fromDegrees, toDegrees,
                Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        rotateAnimation.setDuration(duration);
        rotateAnimation.setFillAfter(true);
        view.startAnimation(rotateAnimation);
    }

    /**
     * 切换按钮
     */
    TranslateAnimation translateAnimation;

    private void switchMenu(int duration) {
        //需要移动到目标点--即按钮的中心位置
        int toX = childOne.getLeft();
        int toY = childOne.getTop();
        int count = getChildCount();
        for (int i = 0; i < count - 1; i++) {
            AnimationSet set = new AnimationSet(true);
            final View view = getChildAt(i + 1);
            view.setVisibility(VISIBLE);
            if (curStatus == Status.STATUS_OPEN) {
                translateAnimation = new TranslateAnimation(0, toX - view.getLeft(), 0, toY - view.getTop());
            } else {
                translateAnimation = new TranslateAnimation(toX - view.getLeft(), 0, toY - view.getTop(), 0);
            }
            translateAnimation.setDuration(duration);
            translateAnimation.setFillAfter(true);
            translateAnimation.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {
                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    Log.e("info", "onAnimationEnd==");
                    if (curStatus == Status.STATUS_CLOSE) {
                        view.clearAnimation();
                        view.setVisibility(GONE);
                    }
                }

                @Override
                public void onAnimationRepeat(Animation animation) {
                }
            });
            translateAnimation.setStartOffset(i * 100 / count);
            RotateAnimation rotateAnimation = new RotateAnimation(0, 720,
                    Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
            rotateAnimation.setDuration(duration);
            rotateAnimation.setFillAfter(true);
            set.addAnimation(rotateAnimation);
            set.addAnimation(translateAnimation);
            view.startAnimation(set);
            final int position = i + 1;
            view.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    Toast.makeText(getContext(), "===" + position, Toast.LENGTH_SHORT).show();
                    menuItem(position);
                    switchStatus();
                }
            });
        }
        switchStatus();
    }

    private void switchStatus() {
        if (curStatus == Status.STATUS_CLOSE) {
            curStatus = Status.STATUS_OPEN;
        } else {
            curStatus = Status.STATUS_CLOSE;
        }
    }

    /**
     * 对item的点击事件处理
     */
    private void menuItem(int position) {
        int count = getChildCount();
        for (int i = 0; i < count - 1; i++) {
            View child = getChildAt(i + 1);
            if (position == i + 1) {//当前点击的item
                setScaleBig(child, 300);
            } else {
                setScaleSmall(child, 300);
            }
        }
    }

    /**
     * 放大再消失
     *
     * @param view
     * @param duration
     */
    private void setScaleBig(final View view, int duration) {
        ScaleAnimation scaleAnimation = new ScaleAnimation(1.0f, 2.0f, 1.0f, 2.0f,
                Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        scaleAnimation.setDuration(duration);
        scaleAnimation.setFillAfter(true);
        scaleAnimation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                view.clearAnimation();
                view.setVisibility(GONE);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });

        AlphaAnimation alphaAnimation = new AlphaAnimation(1.0f, 0.0f);
        alphaAnimation.setDuration(duration);
        alphaAnimation.setFillAfter(true);

        AnimationSet set = new AnimationSet(true);
        set.addAnimation(scaleAnimation);
        set.addAnimation(alphaAnimation);
        view.startAnimation(set);
    }

    /**
     * 缩小再消失
     *
     * @param view
     * @param duration
     */
    private void setScaleSmall(final View view, int duration) {
        ScaleAnimation scaleAnimation = new ScaleAnimation(1.0f, 0.0f, 1.0f, 0.0f,
                Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        scaleAnimation.setDuration(duration);
        scaleAnimation.setFillAfter(true);
        scaleAnimation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                view.clearAnimation();
                view.setVisibility(GONE);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });

        AlphaAnimation alphaAnimation = new AlphaAnimation(1.0f, 0.0f);
        alphaAnimation.setDuration(duration);
        alphaAnimation.setFillAfter(true);

        AnimationSet set = new AnimationSet(true);
        set.addAnimation(scaleAnimation);
        set.addAnimation(alphaAnimation);
        view.startAnimation(set);
    }
}
