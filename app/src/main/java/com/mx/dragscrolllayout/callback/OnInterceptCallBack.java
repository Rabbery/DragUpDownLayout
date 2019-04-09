package com.mx.dragscrolllayout.callback;

import android.view.MotionEvent;

/**
 * @author : mx.yue
 * @date : Created on 2019-03-22
 * <p>
 * 描述: 滚动拦截监听接口
 */
public interface OnInterceptCallBack {

    boolean checkIfIntercept(MotionEvent ev, boolean downDirect);
}
