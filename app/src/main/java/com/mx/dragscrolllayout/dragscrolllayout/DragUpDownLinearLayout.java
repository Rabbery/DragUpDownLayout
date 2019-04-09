package com.mx.dragscrolllayout.dragscrolllayout;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.support.v7.widget.LinearLayoutCompat;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.GestureDetector;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import com.mx.dragscrolllayout.R;

/**
 * @author : mx.yue
 * @date : Created on 2019-03-23
 * <p>
 * 描述:
 */

public class DragUpDownLinearLayout extends LinearLayout implements View.OnTouchListener,
        GestureDetector.OnGestureListener {
    public final static String TAG = "DragUpDownLinearLayout";
    public final static int TOP_MODE = 1;
    public final static int MIDDLE_MODE = 2;
    public final static int BOTTOM_MODE = 3;
    public int customMode = 0;
    // 手势监听对象
    private GestureDetector mGestureDetector;
    // 拖拽条的高度
    private final static int indicatorHeight = 30;
    private int indicatorHeightPx;
    // 中间位置的高度
    private int middleHeight;
    // contentView（去掉状态栏、toolbar和导航栏部分）的高度
    private int contentViewHeight;
    // 顶部其他控件的高度
    private int topHeight;
    // 屏幕的高度
    private float screenHeight;
    // 滑动开始手指落点
    private int y0;
    private float rawYDown;
    // 第一次加载标志位
    private boolean isFirstLayout = true;
    // 是否拦截事件接口回调，用于判断子控件的是否可滑动
    private RequestInterceptCallBack interceptCallBack;
    // 动画对象
    private ObjectAnimator animator = null;
    private static final String ANIMATOR_MODE = "translationY";
    // 是否触发了Filing方法，未触发交由onTouch方法完成移动
    private boolean hasFiling;
    // 是否在滚动触发的layout的标志位
    private boolean isScrolling;

    public DragUpDownLinearLayout(Context context) {
        this(context, null);
    }

    public DragUpDownLinearLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public DragUpDownLinearLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    @SuppressLint("ClickableViewAccessibility")
    private void init(Context context) {
        // 界面
        indicatorHeightPx = dp2px(indicatorHeight);
        setBackgroundColor(Color.WHITE);
        FrameLayout frameLayout = new FrameLayout(context);
        frameLayout.setLayoutParams(
                new LinearLayoutCompat.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, indicatorHeightPx));
        addView(frameLayout);
        View view = new View(context);
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(dp2px(75), dp2px(8));
        params.gravity = Gravity.CENTER;
        view.setLayoutParams(params);
        view.setBackgroundColor(Color.parseColor("#666699"));
        frameLayout.addView(view);
        // 获取屏幕的高
        DisplayMetrics dm = context.getResources().getDisplayMetrics();
        screenHeight = dm.heightPixels;
        setOnTouchListener(this);
        mGestureDetector = new GestureDetector(getContext(), this);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        boolean intercept = false;
        if (interceptCallBack != null) {
            switch (ev.getAction()) {

                case MotionEvent.ACTION_DOWN:
                    y0 = (int) ev.getY();
                    rawYDown = ev.getRawY();
                    intercept = false;
                    hasFiling = false;
                    break;

                case MotionEvent.ACTION_MOVE:
                    float dy = ev.getY() - y0;
                    Log.i(TAG, "dy" + dy);
                    if (Math.abs(dy) < 7 || animator != null || (customMode == TOP_MODE && dy < 0)) {
                        // 移动过小视为点击事件。不拦截 或者 动画尚未结束 本次不拦截
                        intercept = false;
                    } else if (dy > 0) {
                        intercept = interceptCallBack.canIntercept(true);
                    } else {
                        intercept = interceptCallBack.canIntercept(false);
                    }
                    break;

                case MotionEvent.ACTION_UP:
                    intercept = false;
                    break;
            }
        }
        return intercept;
    }

    @Override
    public boolean onTouch(View view, MotionEvent event) {
        mGestureDetector.onTouchEvent(event);
        // 是否有执行filing
        if (event.getAction() == MotionEvent.ACTION_UP) {
            if (!hasFiling) {
                isScrolling = false;
                // 松手时固定位置 计算占屏幕的百分比
                float yUP = getTop();
                float i = yUP / screenHeight;
                if (i < 0.30) {
                    animator = ObjectAnimator.ofFloat(DragUpDownLinearLayout.this, ANIMATOR_MODE,
                            getTranslationY(), getTranslationY() - getY() + topHeight);
                    customMode = TOP_MODE;
                } else if (i < 0.75) {
                    animator = ObjectAnimator.ofFloat(DragUpDownLinearLayout.this, ANIMATOR_MODE,
                            getTranslationY(), getTranslationY() + (middleHeight - getY()));
                    customMode = MIDDLE_MODE;
                } else {
                    animator = ObjectAnimator.ofFloat(DragUpDownLinearLayout.this, ANIMATOR_MODE,
                            getTranslationY(), getTranslationY() + contentViewHeight - getY() - indicatorHeightPx);
                    customMode = BOTTOM_MODE;
                }
                animator.setDuration(500);
                animator.start();
                // 动画结束时，将控件的translation偏移量转化为Top值，便于计算
                animator.addListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        super.onAnimationEnd(animation);
                        float translationY = getTranslationY();
                        setTranslationY(0);
                        layout(getLeft(), (int) (getTop() + translationY),
                                getRight(), (int) (getBottom() + translationY));
//                        animator = null;
                    }
                });
            }
        }
        return true;
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        Log.e(TAG, "onLayout" + t);
        if (isFirstLayout) {
            contentViewHeight = ((Activity) getContext()).getWindow().
                    findViewById(Window.ID_ANDROID_CONTENT).getMeasuredHeight();
            middleHeight = (contentViewHeight / 3) * 2;
            isFirstLayout = false;
            Log.e(TAG, "contentViewHeight" + contentViewHeight);
        } else {
            Log.e(TAG, "isScrolling" + isScrolling);
            if (!isScrolling) {
                switch (customMode) {
                    case TOP_MODE:
                        t = topHeight;
                        b = getHeight() + topHeight;
                        break;
                    case MIDDLE_MODE:
                        t = middleHeight;
                        b = getHeight() + middleHeight;
                        break;
                    case BOTTOM_MODE:
                        int topUp = contentViewHeight - indicatorHeightPx;
                        t = topUp;
                        b = getHeight() + topUp;
                        break;
                }
                setTop(t);
                setBottom(b);
            }
        }
        super.onLayout(changed, l, t, r, b);
    }


    @Override
    public boolean onDown(MotionEvent motionEvent) {

        return false;
    }


    @Override
    public boolean onScroll(MotionEvent motionEvent, MotionEvent motionEvent1, float distanceX, float distanceY) {
        int y = (int) motionEvent1.getY();
        // 获取本次移动的距离
        int dy = y - y0;
        int top = getTop();
        int bottom = getBottom();
        if (top <= topHeight && dy < 0) {
            // 高出顶部 则不改变位置防止超出顶部
            return false;
        }
        layout(getLeft(), (top + dy),
                getRight(), (bottom + dy));
        isScrolling = true;
        return false;
    }

    @Override
    public boolean onFling(MotionEvent motionEvent, MotionEvent motionEvent1, float x, float speedY) {
        float v = motionEvent1.getRawY() - rawYDown;
        switch (customMode) {
            case TOP_MODE:
                animator = ObjectAnimator.ofFloat(DragUpDownLinearLayout.this, ANIMATOR_MODE,
                        getTranslationY(), getTranslationY() + (middleHeight - getY()));
                customMode = MIDDLE_MODE;
                break;
            case MIDDLE_MODE:
                if (v > 0) {
                    animator = ObjectAnimator.ofFloat(DragUpDownLinearLayout.this, ANIMATOR_MODE,
                            getTranslationY(), getTranslationY() + contentViewHeight - getY() - indicatorHeightPx);
                    customMode = BOTTOM_MODE;
                } else {
                    animator = ObjectAnimator.ofFloat(DragUpDownLinearLayout.this, ANIMATOR_MODE,
                            getTranslationY(), getTranslationY() - getY() + topHeight);
                    customMode = TOP_MODE;
                }
                break;
            case BOTTOM_MODE:
                animator = ObjectAnimator.ofFloat(DragUpDownLinearLayout.this, ANIMATOR_MODE,
                        getTranslationY(), getTranslationY() + (middleHeight - getY()));
                customMode = MIDDLE_MODE;
                break;
            default:
        }

        animator.setDuration(500);
        animator.start();
        // 动画结束时，将控件的translation偏移量转化为Top值，便于计算
        animator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                float translationY = getTranslationY();
                setTranslationY(0);
                layout(getLeft(), (int) (getTop() + translationY),
                        getRight(), (int) (getBottom() + translationY));
//                animator = null;
            }
        });
        isScrolling = false;
        hasFiling = true;
        return true;
    }


    /**
     * 设置位置，同于指定初始化位置
     */
    public void setLocation(int mode) {
        switch (mode) {
            case TOP_MODE:
                layout(getLeft(),
                        topHeight,
                        getRight(),
                        getHeight() + topHeight);
                customMode = TOP_MODE;
                break;
            case MIDDLE_MODE:
                layout(getLeft(), middleHeight,
                        getRight(), middleHeight + getHeight());
                customMode = MIDDLE_MODE;
                break;
            case BOTTOM_MODE:
                int topUp = contentViewHeight - indicatorHeightPx;
                layout(getLeft(), topUp,
                        getRight(), topUp + getHeight());
                customMode = BOTTOM_MODE;
                break;
        }

    }

    @Override
    public void onLongPress(MotionEvent motionEvent) {
    }

    @Override
    public void onShowPress(MotionEvent motionEvent) {
    }

    @Override
    public boolean onSingleTapUp(MotionEvent motionEvent) {
        return false;
    }

    private int dp2px(float dipValue) {
        final float scale = getContext().getResources().getDisplayMetrics().density;
        return (int) (dipValue * scale + 0.5f);
    }

    public void setInterceptCallBack(RequestInterceptCallBack interceptCallBack) {
        this.interceptCallBack = interceptCallBack;
    }

    public interface RequestInterceptCallBack {
        boolean canIntercept(boolean isDown);
    }

    /**
     * 重新请求一次contentView 因为toolbar将它往下顶了一部分，也就是加一个偏移量
     */
    public void resetContentViewHeight(int off) {
        contentViewHeight = ((Activity) getContext()).getWindow().
                findViewById(Window.ID_ANDROID_CONTENT).getMeasuredHeight() - off;
        middleHeight = (contentViewHeight / 3) * 2;
        Log.e(TAG, "resetContentViewHeight" + contentViewHeight);
    }

    /**
     * 设置顶部高度
     */
    public void setTopHeight(int topHeight) {
        this.topHeight = topHeight;
    }
}
