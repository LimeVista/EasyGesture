package me.limeice.gesture.standard;

import android.view.MotionEvent;

import androidx.annotation.NonNull;

/**
 * 拖拽接口
 */
@FunctionalInterface
public interface OnDrag {

    /**
     * 拖拽事件
     *
     * @param event 触摸事件
     * @param dx    水平位移
     * @param dy    垂直位移
     */
    void onDrag(@NonNull MotionEvent event, float dx, float dy);
}
