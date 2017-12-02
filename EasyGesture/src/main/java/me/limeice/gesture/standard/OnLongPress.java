package me.limeice.gesture.standard;

import android.view.MotionEvent;

/**
 * Created by LimeV on 2017/12/3.
 */

public interface OnLongPress {

    /**
     * 长按事件
     *
     * @param event 触摸事件
     */
    void onLongPress(MotionEvent event);
}
