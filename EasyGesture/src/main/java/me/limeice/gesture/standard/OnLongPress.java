package me.limeice.gesture.standard;

import android.view.MotionEvent;

import androidx.annotation.NonNull;

/**
 * 长按接口
 */
@FunctionalInterface
public interface OnLongPress {

    /**
     * 长按事件
     *
     * @param event 触摸事件
     */
    void onLongPress(@NonNull MotionEvent event);
}
