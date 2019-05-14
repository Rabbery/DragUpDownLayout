package com.mx.dragscrolllayout.base;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.databinding.ViewDataBinding;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.view.ViewCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.text.Html;
import android.text.Spanned;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.mx.dragscrolllayout.R;
import com.mx.dragscrolllayout.util.DisplayUtil;


/**
 * Created by ${qianzhihe} on 2017/11/21.
 * Activity基类
 */

@SuppressLint("RestrictedApi")
public abstract class BaseActivity extends AppCompatActivity implements View.OnClickListener {

    private String tagName = getClass().getSimpleName();

    protected Context context;
    protected ViewDataBinding banding;
//    String titleStr;
//    boolean isShowLeftImageView;

    onBottomChangedCallBack onBottomChangedCallBack;

    public abstract int getContentViewId();//放layoutId


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = this;
//      requestWindowFeature(Window.FEATURE_NO_TITLE);// 继承了AppCompatActivity，通过隐藏ActionBar方式来解决5.0以下版本不兼容问题
        banding = DataBindingUtil.setContentView(this, getContentViewId());
//        binding = DataBindingUtil.setContentView(this, R.layout.base_title_layout);
        setStatusBar();
    }

//    private void initBaseView() {
//        binding.baseTitleLayoutLeftImageView.setOnClickListener(this);
//        if (isShowLeftImageView) {
//            binding.baseTitleLayoutLeftImageView.setVisibility(View.VISIBLE);
//        } else {
//            binding.baseTitleLayoutLeftImageView.setVisibility(View.INVISIBLE);
//        }
//    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    protected void startActivity(Class<?> cls) {
        startActivity(getIntent(cls));
    }

    protected void startActivity(Class<?> cls, int flags) {
        startActivity(getIntent(cls, flags));
    }

    protected Intent getIntent(Class<?> cls) {
        return new Intent(context, cls);
    }

    protected Intent getIntent(Class<?> cls, int flags) {
        Intent intent = getIntent(cls);
        intent.setFlags(flags);
        return intent;
    }

    /**
     * 返回键返回事件
     * 监听返回键的返回事件，当事务数量等于1的时候，直接finish()
     *
     * @param keyCode
     * @param event
     * @return
     */
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (KeyEvent.KEYCODE_BACK == keyCode) {
            if (getSupportFragmentManager().getBackStackEntryCount() == 1) {
                finish();
                return true;
            }
        }
        return super.onKeyDown(keyCode, event);
    }

//    public void setTitle(String titleString) {
//        this.titleStr = titleString;
//    }
//
//    public void setLeftImageViewShowStatus(boolean isShow) {
//        this.isShowLeftImageView = isShow;
//    }

    public interface onBottomChangedCallBack {
        void onBottomStatusChangedCallBack(int index);
    }

    public void setOnBottomChangedCallBack(onBottomChangedCallBack changedCallBack) {
        this.onBottomChangedCallBack = changedCallBack;
    }

    /**
     * 设置文字加粗
     *
     * @param tv
     * @param msg
     */
    public void setTvBold(TextView tv, String msg) {
        String source = "<font style={font-weight:600}>" + msg + "</font>";
        Spanned spanned = Html.fromHtml(source);
        tv.setText(spanned);
    }

    /**
     * 设置文字常规粗细
     *
     * @param tv
     * @param msg
     */
    public void setTvNormal(TextView tv, String msg) {
        String source = "<font style={font-weight:400}>" + msg + "</font>";
        Spanned spanned = Html.fromHtml(source);
        tv.setText(spanned);
    }

    /**
     * 状态栏透明（支持Android 4.4 以上）
     * add on 2018.5.11 by ymx
     */
    protected void setStatusBar() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {//5.0及以上
            //加了这个属性状态栏设置颜色没效果
            if (isImmerseStatusBarTranslucent()) {
                getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            }

            getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            //根据上面设置是否对状态栏单独设置颜色
            setLollipopTitleBarColor(getLollipopTitleBarColor());

            ViewGroup mContentView = (ViewGroup) findViewById(Window.ID_ANDROID_CONTENT);
            ViewCompat.setFitsSystemWindows(mContentView.getChildAt(0), !isImmerseStatusBarTranslucent());

            View decorView = getWindow().getDecorView();
            int option = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LAYOUT_STABLE;
            decorView.setSystemUiVisibility(option);
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {//4.4到5.0
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            setViewPaddingTopByStatusHeight(getKitkatTitleBarColor());
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !isChangeStatusBarTextColor()) {//android6.0以后可以对状态栏文字颜色和图标进行修改
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        }
    }

    /**
     * 4.4状态栏颜色切换
     *
     * @param color
     */
    public void setViewPaddingTopByStatusHeight(int color) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            ViewGroup contentView = (ViewGroup) findViewById(android.R.id.content);
            View mChildView = contentView.getChildAt(0);

            int statusBarHeight = DisplayUtil.getStatusBarHeight(this);
            if (mChildView != null) {
                FrameLayout.LayoutParams lp = (FrameLayout.LayoutParams) mChildView.getLayoutParams();
                //如果已经为 ChildView 设置过了 marginTop, 再次调用时直接跳过
                if (lp != null && lp.topMargin < statusBarHeight && lp.height != statusBarHeight) {
                    ViewCompat.setFitsSystemWindows(mChildView, false);
                    lp.topMargin += statusBarHeight;
                    mChildView.setLayoutParams(lp);
                }
            }
            View statusBarView = contentView.getChildAt(0);
            if (statusBarView != null && statusBarView.getLayoutParams() != null && statusBarView.getLayoutParams().height == statusBarHeight) {
                //避免重复调用时多次添加 View
                statusBarView.setBackgroundColor(color);
                return;
            }
            statusBarView = new View(this);
            ViewGroup.LayoutParams lp = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                    statusBarHeight);
            statusBarView.setBackgroundColor(color);
            contentView.addView(statusBarView, 0, lp);
        }
    }

    /**
     * 是否使用状态栏文字和图标为暗色，如果状态栏采用了白色系，则需要使状态栏和图标为暗色，android6.0以上可以设置
     * 子界面通过覆写修改策略
     */
    public boolean isChangeStatusBarTextColor() {
        return true;
    }

    /**
     * 是否状态栏透明
     */
    public boolean isImmerseStatusBarTranslucent() {
        return false;
    }

    /**
     * 4.4状态栏颜色
     * 子界面可通过覆写修改
     */
    public int getKitkatTitleBarColor() {
        return getDefaultTitleBarColor();
    }

    /**
     * 5.0以上状态栏颜色
     * 子界面可通过覆写修改
     */
    public int getLollipopTitleBarColor() {
        return getDefaultTitleBarColor();
    }

    /**
     * 5.0以上状态栏颜色切换
     */
    public final void setLollipopTitleBarColor(int color) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {//5.0及以上
            getWindow().setStatusBarColor(color);
        }
    }

    /**
     * 状态栏默认颜色
     */
    private final int getDefaultTitleBarColor() {
        return getResources().getColor(R.color.colorStatusBar);
    }

}
