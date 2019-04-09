package com.mx.dragscrolllayout.dragscrolllayout;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.widget.LinearLayout;

import com.mx.MainActivity;
import com.mx.dragscrolllayout.util.DisplayUtil;

/**
 * @author : mx.yue
 * @date : Created on 2019-03-22
 * <p>
 * 描述:自定义拖动布局
 */

public class DragScrollVerticalLayout extends LinearLayout implements GestureDetector.OnGestureListener {

    public final static String TAG = "SlideDraglLayout";
    public final static int TOP_MODE = 1;
    public final static int MIDDLE_MODE = 2;
    public final static int BOTTOM_MODE = 3;
    private static final String ANIMATOR_MODE = "translationY";
    private int lastX, lastY;
    // 顶部控件的高度
    private int topHeight;
    // 中部停留高度
    private int middleHeight;
    // 内容高度
    private int contentHeight;
    // 当前模式
    public int customMode = 0;
    // 手势监听对象
    private GestureDetector mGestureDetector;
    // 屏幕的高度
    private float screenHeight;
    // 动画对象
    private ObjectAnimator animator = null;
    // 是否触发了Filing方法
    private boolean hasFiling;
    private MainActivity.OnScrollChangeTopListenr onScrollListener;
    private OnInterceptCallBack interceptCallBack;


    public DragScrollVerticalLayout(Context context) {
        this(context, null);
    }

    public DragScrollVerticalLayout(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initParam(context);
    }

    public DragScrollVerticalLayout(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    private void initParam(Context context) {
        middleHeight = DisplayUtil.dip2px(context, 133);
        // 获取屏幕的高
        DisplayMetrics dm = context.getResources().getDisplayMetrics();
        screenHeight = dm.heightPixels;
        mGestureDetector = new GestureDetector(getContext(), this);
    }

    public void setOnScrollChangeTopListenr(MainActivity.OnScrollChangeTopListenr onScrollChangeTopListenr) {
        this.onScrollListener = onScrollChangeTopListenr;
    }

    public void setInterceptCallBack(OnInterceptCallBack interceptCallBack) {
        this.interceptCallBack = interceptCallBack;
    }

    /**
     * 设置顶部高度
     */
    public void setTopHeight(int topHeight) {
        this.topHeight = topHeight;
    }

    public void setMiddleHeight(int middleHeight) {
        this.middleHeight = middleHeight;
        initLayout();
    }

    public int getMiddleHeight() {
        return middleHeight;
    }

    public void initLayout() {
        layout(getLeft(), middleHeight, getRight(), contentHeight + middleHeight);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);
        contentHeight = b - t;
        //可以在这里确定这个viewGroup的：宽 = r-l.高 = b - t
    }


    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        Log.e(TAG, "onTouchEvent");
        mGestureDetector.onTouchEvent(ev);
        if (ev.getAction() == MotionEvent.ACTION_UP) {
            if (!hasFiling) {
                float disY = getY() - middleHeight;
                if (disY > 0) {
                    if (disY > 400) {
                        animator = ObjectAnimator.ofFloat(DragScrollVerticalLayout.this, ANIMATOR_MODE,
                                getTranslationY(), getTranslationY() + (contentHeight - getY()));
                    } else {
                        animator = ObjectAnimator.ofFloat(DragScrollVerticalLayout.this, ANIMATOR_MODE,
                                getTranslationY(), getTranslationY() + (middleHeight - getY()));
                    }

                    animator.setDuration(200);
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
        }
        return true;
    }


    @Override
    public boolean onScroll(MotionEvent motionEvent, MotionEvent motionEvent1, float distanceX, float distanceY) {
        int dy = (int) motionEvent1.getRawY() - lastY;

        int left = this.getLeft();
        int top = this.getTop() + dy;
        int right = this.getRight();
        int bottom = this.getBottom() + dy;
//                if (left < 0) { //最左边
//                    left = 0;
//                    right = left + this.getWidth();
//                }
//                if (right > screenWidth) { //最右边
//                    right = screenWidth;
//                    left = right - this.getWidth();
//                }
        if (top < 0) {  //最上边
            top = 0;
            bottom = top + contentHeight;
        }
        if (bottom > contentHeight << 1) {//最下边
            bottom = contentHeight << 1;
            top = contentHeight;
        }
        this.layout(left, top, right, bottom);//设置控件的新位置

        lastX = (int) motionEvent1.getRawX();//再次将滑动其实位置定位
        lastY = (int) motionEvent1.getRawY();

        if (onScrollListener != null) {
            onScrollListener.onScrollChange(0, getTop());
        }
        return false;
    }

    @Override
    public boolean onFling(MotionEvent motionEvent, MotionEvent motionEvent1, float velocityX, float velocityY) {
        float disY = getY() - middleHeight;
        if (disY > 0) {
            if (disY > 400 || velocityY > 5000) {
                animator = ObjectAnimator.ofFloat(DragScrollVerticalLayout.this, ANIMATOR_MODE,
                        getTranslationY(), getTranslationY() + (contentHeight - getY()));
            } else {
                animator = ObjectAnimator.ofFloat(DragScrollVerticalLayout.this, ANIMATOR_MODE,
                        getTranslationY(), getTranslationY() + (middleHeight - getY()));
            }

            animator.setDuration(200);
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
        hasFiling = true;
        return true;
    }


    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        boolean intercept = false;
        if (animator != null) {
            animator.cancel();
            animator = null;
        }
        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
                lastX = (int) ev.getRawX();//设定移动的初始位置相对位置
                lastY = (int) ev.getRawY();
                hasFiling = false;
                break;
            case MotionEvent.ACTION_MOVE:
                int dy = (int) ev.getRawY() - lastY;
                if (Math.abs(dy) < 7) {
                    // 移动过小视为点击事件。不拦截 或者 滑到顶部 本次不拦截
                    intercept = false;
                } else if (getY() <= 0) {
                    intercept = interceptCallBack.checkIfIntercept(ev, dy > 0);
                } else {
                    intercept = true;
                }
                break;

            case MotionEvent.ACTION_UP:
                intercept = false;
                break;
        }
        return intercept;
    }

    @Override
    public boolean onDown(MotionEvent e) {
        return false;
    }

    @Override
    public void onShowPress(MotionEvent e) {

    }

    @Override
    public boolean onSingleTapUp(MotionEvent e) {
        return false;
    }

    @Override
    public void onLongPress(MotionEvent e) {

    }

    public interface OnInterceptCallBack {
        boolean checkIfIntercept(MotionEvent ev, boolean downDirect);
    }
}
