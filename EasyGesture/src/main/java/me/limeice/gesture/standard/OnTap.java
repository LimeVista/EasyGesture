package me.limeice.gesture.standard;

import android.view.MotionEvent;

import androidx.annotation.NonNull;

/**
 * 点击接口
 */
@FunctionalInterface
public interface OnTap {

    /**
     * 单击事件
     *
     * @param event 触摸事件
     */
    void onTap(@NonNull MotionEvent event);
}
