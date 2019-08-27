package me.limeice.gesture.standard;

import android.view.MotionEvent;

/**
 * Created by LimeV on 2017/12/3.
 */

public interface OnDrag {

    /**
     * 拖拽事件
     *
     * @param event 触摸事件
     * @param dx    水平位移
     * @param dy    垂直位移
     */
    void onDrag(MotionEvent event, float dx, float dy);
}
