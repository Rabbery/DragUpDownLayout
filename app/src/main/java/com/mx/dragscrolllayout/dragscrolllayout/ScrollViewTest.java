package com.mx.dragscrolllayout.dragscrolllayout;

import android.content.Context;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

/**
 * @author : mx.yue
 * @date : Created on 2019-04-18
 * <p>
 * 描述:
 */

public class ScrollViewTest extends RelativeLayout implements GestureDetector.OnGestureListener {

    // 手势监听对象
    private GestureDetector mGestureDetector;
    private int lastX;
    private int lastY;
    private boolean hasFiling;
    // 内容高度
    private int contentHeight;
    // 屏幕的高度
    private int screenHeight;

    public ScrollViewTest(Context context) {
        this(context, null);
    }

    public ScrollViewTest(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ScrollViewTest(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initParam(context);
    }

    private void initParam(Context context) {
        DisplayMetrics dm = context.getResources().getDisplayMetrics();
        screenHeight = dm.heightPixels;
        mGestureDetector = new GestureDetector(getContext(), this);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);
        contentHeight = b - t;
    }

    @Override
    public boolean onDown(MotionEvent e) {
        Log.e("ScrollTest", "onDown:" + e.getAction());
        return false;
    }

    @Override
    public void onShowPress(MotionEvent e) {
        Log.e("ScrollTest", "onShowPress:" + e.getAction());
    }

    @Override
    public boolean onSingleTapUp(MotionEvent e) {
        Log.e("ScrollTest", "onSingleTapUp:" + e.getAction());
        return false;
    }

    @Override
    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
        Log.e("ScrollTest", "onScroll:" + e1.getAction() + " " + e2.getAction());
        int dy = (int) e2.getRawY() - lastY;

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
        if (bottom > screenHeight) {//最下边
            bottom = screenHeight;
            top = screenHeight - contentHeight;
        }
        this.layout(left, top, right, bottom);//设置控件的新位置

        lastX = (int) e2.getRawX();//再次将滑动其实位置定位
        lastY = (int) e2.getRawY();

        return false;
    }

    @Override
    public void onLongPress(MotionEvent e) {
        Log.e("ScrollTest", "onLongPress:" + e.getAction());
    }

    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
        Log.e("ScrollTest", "onFling:" + e1.getAction() + " " + e2.getAction());
        return true;
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        boolean intercept = true;
        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
                //设定移动的初始位置相对位置
                lastX = (int) ev.getRawX();
                lastY = (int) ev.getRawY();
                hasFiling = false;
                break;
            case MotionEvent.ACTION_MOVE:
                intercept = true;
                break;
            case MotionEvent.ACTION_UP:
                intercept = false;
                break;
        }
        return intercept;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        Log.e("ScrollTest", "onTouchEvent:" + event.getAction());
        mGestureDetector.onTouchEvent(event);
        return true;
    }
}
