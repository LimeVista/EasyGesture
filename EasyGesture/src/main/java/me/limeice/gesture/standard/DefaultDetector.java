package me.limeice.gesture.standard;

import android.view.MotionEvent;

import androidx.annotation.NonNull;

/**
 * 默认手势监听器接口
 *
 * @author LimeVista
 * @version 1.0
 */
public interface DefaultDetector {

    /**
     * 事件响应接口
     *
     * @param e 触摸事件
     * @return {@code true}响应事件 {@code false}拒绝响应事件
     */
    boolean onTouchEvent(@NonNull MotionEvent e);
}
