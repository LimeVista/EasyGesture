package me.limeice.gesture.standard;

import android.view.MotionEvent;

/**
 * Created by LimeV on 2017/12/3.
 */

public interface OnDrag {

    /**
     * 拖拽事件
     *
     * @param dx
     * @param dy
     */
    void onDrag(float dx, float dy);
}
