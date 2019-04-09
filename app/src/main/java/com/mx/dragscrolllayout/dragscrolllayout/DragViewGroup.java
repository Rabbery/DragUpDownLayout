package com.mx.dragscrolllayout.dragscrolllayout;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.MotionEvent;
import android.widget.LinearLayout;

/**
 * @author : mx.yue
 * @date : Created on 2019-03-23
 * <p>
 * 描述:
 */

public class DragViewGroup extends LinearLayout {
    private int lastX, lastY, screenWidth, screenHeight;
    private int contentHeight;
    private int topHeight;
    private int middleHeight;
    private ObjectAnimator animator;
    private static final String ANIMATOR_MODE = "translationY";

    public DragViewGroup(Context context) {
        this(context, null);
    }

    public DragViewGroup(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public DragViewGroup(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        DisplayMetrics dm = getResources().getDisplayMetrics();
        screenWidth = dm.widthPixels;
        screenHeight = dm.heightPixels;//减去下边的高度
        middleHeight = 200;
    }

    //定位
    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);
        contentHeight = b - t;
        //可以在这里确定这个viewGroup的：宽 = r-l.高 = b - t
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        int action = ev.getAction();
        switch (action) {
            case MotionEvent.ACTION_DOWN:
                if(animator!=null){
                    animator.cancel();
                }
                lastX = (int) ev.getRawX();//设定移动的初始位置相对位置
                lastY = (int) ev.getRawY();
                break;
            case MotionEvent.ACTION_MOVE://移动
                //event.getRawX()事件点距离屏幕左上角的距离
//                int dx = (int) ev.getRawX() - lastX;
                int dy = (int) ev.getRawY() - lastY;

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
                if (bottom > contentHeight * 2) {//最下边
                    bottom = contentHeight * 2;
                    top = contentHeight;
                }
                this.layout(left, top, right, bottom);//设置控件的新位置
//                LogTool.e("position:" + left + ", " + top + ", " + right + ", " + bottom);
                lastX = (int) ev.getRawX();//再次将滑动其实位置定位
                lastY = (int) ev.getRawY();
                break;
            case MotionEvent.ACTION_UP:
                lastY = (int) ev.getRawY();
                float disY = getY() - middleHeight;
                if (disY > 0) {
                    if(getY() > contentHeight - 500){
                        animator = ObjectAnimator.ofFloat(DragViewGroup.this, ANIMATOR_MODE,
                                getTranslationY(), getTranslationY() + (contentHeight - getY()));
                    }else{
                        animator = ObjectAnimator.ofFloat(DragViewGroup.this, ANIMATOR_MODE,
                                getTranslationY(), getTranslationY() + (middleHeight - getY()));
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

                break;
        }
        return true;
    }

    //拦截touch事件
    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
//        LogTool.e("onInterceptTouchEvent");

        return true;
    }

}
