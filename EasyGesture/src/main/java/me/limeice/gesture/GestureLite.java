package me.limeice.gesture;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.ViewConfiguration;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.Objects;

import me.limeice.gesture.standard.DefaultDetector;

/**
 * 手势监听器
 *
 * @author LimeVista
 * @version 1.1
 */
@SuppressWarnings("unused")
public final class GestureLite implements DefaultDetector {

    /**
     * 长按事件定义
     */
    private static final int LONG_PRESS = 0x01;

    /**
     * 点击事件定义
     */
    private static final int TAP = 0x02;

    public interface OnGestureListener {

        /**
         * 长按事件
         *
         * @param e 触摸事件
         */
        default void onLongPress(@NonNull MotionEvent e) {

        }

        /**
         * 双击事件
         *
         * @param e 触摸事件
         */
        default void onDoubleTap(@NonNull MotionEvent e) {

        }

        /**
         * 单击事件
         *
         * @param e 触摸事件
         */
        default void onTap(@NonNull MotionEvent e) {

        }

        /**
         * 按下事件，参见{@link MotionEvent}
         *
         * @param e 触摸事件
         * @return {@code true}事件响应 ，{@code false}拒绝响应事件
         */
        boolean onDown(@NonNull MotionEvent e);

        /**
         * 滑动或拖动事件
         *
         * @param e1        起始触摸事件
         * @param e2        终止触摸事件
         * @param distanceX 事件产生x轴距离
         * @param distanceY 事件产生y轴距离
         * @return {@code true}事件响应 ，{@code false}拒绝响应事件
         */
        default boolean onScroll(
                @NonNull MotionEvent e1,
                @NonNull MotionEvent e2,
                float distanceX,
                float distanceY
        ) {
            return false;
        }

        /**
         * 缩放事件
         *
         * @param scale  缩放系数
         * @param focusX 缩放中心点横坐标
         * @param focusY 缩放中心点纵坐标
         * @return {@code true}事件响应 ，{@code false}拒绝响应事件
         */
        default boolean onScale(float scale, float focusX, float focusY) {
            return false;
        }

        /**
         * 快速滑动事件
         *
         * @param e1        起始触摸事件
         * @param e2        终止触摸事件
         * @param velocityX 横向加速度
         * @param velocityY 纵向加速度
         * @return {@code true}事件响应 ，{@code false}拒绝响应事件
         */
        default boolean onFling(
                @NonNull MotionEvent e1,
                @NonNull MotionEvent e2,
                float velocityX,
                float velocityY
        ) {
            return false;
        }
    }

    /**
     * 监听分发处理器
     */
    private final static class GestureHandler extends Handler {

        private final GestureLite mGesture;

        GestureHandler(GestureLite gesture) {
            super();
            mGesture = gesture;
        }

        GestureHandler(GestureLite gesture, Handler handler) {
            super(handler.getLooper());
            mGesture = gesture;
        }

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case LONG_PRESS:
                    mGesture.dispatchLongPress();
                    break;
                case TAP:
                    if (mGesture.isDoubleTapEnable && mGesture.mConfirmSingleTap) {
                        mGesture.mListener.onTap(mGesture.mCurrentDownEvent);
                    }
                    break;
                default:
                    throw new RuntimeException("Unknown gesture" + msg);
            }
        }
    }

    private boolean isScaleEnable = false;                  // 是否启用缩放手势
    private boolean isScrollEnable = false;                 // 是否启用滑动手势
    private boolean isFlingEnable = false;                  // 是否启用快速滑动手势
    private boolean isLongPressEnable = false;              // 是否启用长按事件
    private boolean isDoubleTapEnable = false;              // 双击事件

    private int mDoubleTapTimeOut = 300;                    // 双击按钮超时，default:300ms
    private int mLongPressTimeOut = 500;                    // 长按超时，default:500ms

    private final GestureHandler mHandler;                  // 时间分发处理器
    private final OnGestureListener mListener;              // 主事件监听
    private ScaleGestureDetector mScaleDetector = null;     // 缩放手势监听器

    private MotionEvent mCurrentDownEvent;
    private MotionEvent mPreviousUpEvent;

    private boolean mConfirmSingleTap;                      // 单击事件是否成立
    private boolean mInLongPress;                           // 长按是否生效
    private boolean mAlwaysInTapRegion;                     // 是否一直点击区域
    private boolean mAlwaysInDoubleTapRegion;               // 是否一直点击区域

    private float mDownFocusX, mDownFocusY, mLastFocusX, mLastFocusY;
    private float mCurFocusX, mCurFocusY;
    private long mLastTime;
    private int mTouchSlopSquare;                           // 点击区域
    private int mDoubleTouchSlopSquare;                     // 双击点击区域

    private VelocityTracker mVelocityTracker;
    private int mMinFlingVelocity;
    private int mMaxFlingVelocity;

    public GestureLite(@NonNull Context context, @NonNull OnGestureListener listener) {
        this(context, null, listener);
    }

    public GestureLite(
            @NonNull Context context,
            @Nullable Handler handler,
            @NonNull OnGestureListener listener
    ) {
        Objects.requireNonNull(listener, "The OnGestureListener must not be null...");
        mListener = listener;
        mHandler = handler != null ? new GestureHandler(this, handler) : new GestureHandler(this);
        init(context);
    }

    /**
     * 事件响应接口
     *
     * @param e 触摸事件
     * @return {@code true}响应事件 {@code false}拒绝响应事件
     */
    @SuppressWarnings("ConstantConditions")
    @Override
    public boolean onTouchEvent(@NonNull MotionEvent e) {
        final int action = e.getAction();
        if (isFlingEnable) {
            if (mVelocityTracker == null)
                mVelocityTracker = VelocityTracker.obtain();
            mVelocityTracker.addMovement(e);
        }
        //计算中心点
        final boolean pointerUp = (action & MotionEvent.ACTION_MASK) == MotionEvent.ACTION_POINTER_UP;
        final int skipIndex = pointerUp ? e.getActionIndex() : -1;
        float sumX = 0, sumY = 0;
        final int count = e.getPointerCount();
        for (int i = 0; i < count; i++) {
            if (skipIndex == i) continue;
            sumX += e.getX(i);
            sumY += e.getY(i);
        }
        final int div = pointerUp ? count - 1 : count;
        mCurFocusX = sumX / div;
        mCurFocusY = sumY / div;

        boolean isOnTouch = false;
        switch (MotionEvent.ACTION_MASK & action) {
            case MotionEvent.ACTION_DOWN:
                isOnTouch = mListener.onDown(e);
                mInLongPress = false;
                mConfirmSingleTap = true;
                mAlwaysInTapRegion = true;
                if (isOnTouch && isLongPressEnable) {
                    mHandler.removeMessages(LONG_PRESS);
                    mHandler.sendEmptyMessageDelayed(LONG_PRESS, mLongPressTimeOut);
                }
                if (isDoubleTapEnable && mAlwaysInDoubleTapRegion
                        && mPreviousUpEvent != null
                        && System.currentTimeMillis() - mLastTime < mDoubleTapTimeOut) {
                    mListener.onDoubleTap(e);
                    return false;
                }
                mDownFocusX = mLastFocusX = mCurFocusX;
                mDownFocusY = mLastFocusY = mCurFocusY;
                mAlwaysInDoubleTapRegion = true;
                if (mCurrentDownEvent != null) {
                    mCurrentDownEvent.recycle();
                }
                mCurrentDownEvent = MotionEvent.obtain(e);
                break;

            case MotionEvent.ACTION_MOVE:
                if (mInLongPress) break; // 长按已生效
                if (isScrollEnable) {
                    final float scrollX = mLastFocusX - mCurFocusX;
                    final float scrollY = mLastFocusY - mCurFocusY;
                    if (e.getPointerCount() > 1)
                        mAlwaysInTapRegion = false;
                    if (mAlwaysInTapRegion) {
                        final int dx = (int) (mCurFocusX - mDownFocusX);
                        final int dy = (int) (mCurFocusY - mDownFocusY);
                        final int distance = (dx * dx) + (dy * dy);
                        if (distance > mTouchSlopSquare) {
                            mLastFocusX = mCurFocusX;
                            mLastFocusY = mCurFocusY;
                            isOnTouch |= mListener.onScroll(mCurrentDownEvent, e, scrollX, scrollY);
                            mConfirmSingleTap = false;
                            mAlwaysInTapRegion = false;
                            if (isLongPressEnable) mHandler.removeMessages(LONG_PRESS);
                        }
                        if (distance > mDoubleTouchSlopSquare)
                            mAlwaysInDoubleTapRegion = false;   // 双击不成立
                    } else if (Math.abs(scrollX) >= 1 || Math.abs(scrollY) >= 1) {
                        mLastFocusX = mCurFocusX;
                        mLastFocusY = mCurFocusY;
                        isOnTouch |= mListener.onScroll(mCurrentDownEvent, e, scrollX, scrollY);
                        cancelTaps();
                    }
                }
                break;

            case MotionEvent.ACTION_POINTER_DOWN:
                mDownFocusX = mLastFocusX = mCurFocusX;
                mDownFocusY = mLastFocusY = mCurFocusY;
                cancelTaps();
                break;

            case MotionEvent.ACTION_POINTER_UP:
                mDownFocusX = mLastFocusX = mCurFocusX;
                mDownFocusY = mLastFocusY = mCurFocusY;
                cancelTaps();
                // 代码来自于官方源码
                // Check the dot product of current velocities.
                // If the pointer that left was opposing another velocity vector, clear.
                if (isFlingEnable) {
                    mVelocityTracker.computeCurrentVelocity(1000, mMaxFlingVelocity);
                    final int upIndex = e.getActionIndex();
                    final int id1 = e.getPointerId(upIndex);
                    final float x1 = mVelocityTracker.getXVelocity(id1);
                    final float y1 = mVelocityTracker.getYVelocity(id1);
                    for (int i = 0; i < count; i++) {
                        if (i == upIndex) continue;
                        final int id2 = e.getPointerId(i);
                        final float x = x1 * mVelocityTracker.getXVelocity(id2);
                        final float y = y1 * mVelocityTracker.getYVelocity(id2);
                        final float dot = x + y;
                        if (dot < 0) {
                            mVelocityTracker.clear();
                            break;
                        }
                    }
                }
                break;

            case MotionEvent.ACTION_UP:
                MotionEvent currentUpEvent = MotionEvent.obtain(e);
                if (isDoubleTapEnable)
                    mLastTime = System.currentTimeMillis();
                if (mConfirmSingleTap) {
                    if (isDoubleTapEnable)
                        mHandler.sendEmptyMessageDelayed(TAP, mDoubleTapTimeOut);
                    else
                        mListener.onTap(e);
                } else if (isFlingEnable) {
                    final int pointerId = e.getPointerId(0);
                    mVelocityTracker.computeCurrentVelocity(1000, mMaxFlingVelocity);
                    final float velocityY = mVelocityTracker.getYVelocity(pointerId);
                    final float velocityX = mVelocityTracker.getXVelocity(pointerId);
                    if ((Math.abs(velocityY) > mMinFlingVelocity)
                            || (Math.abs(velocityX) > mMinFlingVelocity)) {
                        isOnTouch |= mListener.onFling(mCurrentDownEvent, e, velocityX, velocityY);
                    }
                }
                if (isLongPressEnable)
                    mHandler.removeMessages(LONG_PRESS);
                if (mPreviousUpEvent != null) {
                    mPreviousUpEvent.recycle();
                }
                mPreviousUpEvent = currentUpEvent;
                if (mVelocityTracker != null) {
                    mVelocityTracker.recycle();
                    mVelocityTracker = null;
                }
                break;

            case MotionEvent.ACTION_CANCEL:
                cancel();
                return false;
        }
        if (isScaleEnable)
            isOnTouch |= mScaleDetector.onTouchEvent(e);
        return isOnTouch;
    }

    /**
     * 执行长按事件
     */
    private void dispatchLongPress() {
        mHandler.removeMessages(TAP);
        mConfirmSingleTap = false;
        mInLongPress = true;
        mListener.onLongPress(mCurrentDownEvent);
    }

    private void cancel() {
        mConfirmSingleTap = false;
        mAlwaysInTapRegion = false;
        mAlwaysInDoubleTapRegion = false;
        mLastTime = 0;  // 取消双击
        mHandler.removeMessages(LONG_PRESS);
        mHandler.removeMessages(TAP);
    }

    /**
     * 取消各种点击事件
     */
    private void cancelTaps() {
        mConfirmSingleTap = false;
        mLastTime = 0;  // 取消双击
        if (isLongPressEnable)
            mHandler.removeMessages(LONG_PRESS);
    }

    private void init(Context context) {
        ViewConfiguration config = ViewConfiguration.get(context);
        int touchSlop = config.getScaledTouchSlop();
        mTouchSlopSquare = touchSlop * touchSlop;
        touchSlop = config.getScaledDoubleTapSlop();
        mDoubleTouchSlopSquare = touchSlop * touchSlop;
        mMinFlingVelocity = config.getScaledMinimumFlingVelocity();
        mMaxFlingVelocity = config.getScaledMaximumFlingVelocity();
    }

    /**
     * 缩放手势监听器
     */
    public class ScaleGestureDetector implements DefaultDetector {

        private float mScale;

        private float mLastLength;

        ScaleGestureDetector() {
            mScale = 1.0f;
        }

        private float calcLength(MotionEvent e) {
            return (float) Math.sqrt(
                    Math.pow(e.getX(0) - e.getX(1), 2)
                            + Math.pow(e.getY(0) - e.getY(1), 2));
        }

        /**
         * 事件响应接口
         *
         * @param e 触摸事件
         * @return {@code true} 响应事件, {@code false} 拒绝响应事件
         */
        @Override
        public boolean onTouchEvent(@NonNull MotionEvent e) {
            if (e.getPointerCount() < 2)
                return false;
            boolean is = false;
            switch (MotionEvent.ACTION_MASK & e.getAction()) {

                case MotionEvent.ACTION_POINTER_DOWN:
                case MotionEvent.ACTION_POINTER_UP:
                    mLastLength = calcLength(e);
                    is = true;
                    break;

                case MotionEvent.ACTION_MOVE:
                    float cur = calcLength(e);
                    mScale = cur / mLastLength;
                    if (Math.abs(mScale - 1.0f) > 0.01f) {
                        mLastLength = cur;
                        is = mListener.onScale(mScale, mCurFocusX, mCurFocusY);
                    }
                    break;
            }
            return is;
        }
    }

    /**
     * 启用、禁用缩放手势
     *
     * @param scaleEnable {@code true}开启，{@code false}禁用
     * @return self
     */
    public GestureLite setScaleEnable(boolean scaleEnable) {
        isScaleEnable = scaleEnable;
        if (isScaleEnable && mScaleDetector == null)
            mScaleDetector = new ScaleGestureDetector();
        return this;
    }

    /**
     * 启用、禁用滑动手势
     *
     * @param scrollEnable {@code true}开启，{@code false}禁用
     * @return self
     */
    public GestureLite setScrollEnable(boolean scrollEnable) {
        isScrollEnable = scrollEnable;
        return this;
    }

    /**
     * 启用、禁用快速滑动手势
     *
     * @param flingEnable {@code true}开启，{@code false}禁用
     * @return self
     */
    public GestureLite setFlingEnable(boolean flingEnable) {
        isFlingEnable = flingEnable;
        return this;
    }

    /**
     * 启用、禁用长按手势
     *
     * @param longPressEnable {@code true}开启，{@code false}禁用
     * @return self
     */
    public GestureLite setLongPressEnable(boolean longPressEnable) {
        isLongPressEnable = longPressEnable;
        return this;
    }

    /**
     * 启用、禁用双击手势
     *
     * @param doubleTapEnable {@code true}开启，{@code false}禁用
     * @return self
     */
    public GestureLite setDoubleTapEnable(boolean doubleTapEnable) {
        isDoubleTapEnable = doubleTapEnable;
        return this;
    }

    /**
     * 设置双击超时时间
     *
     * @param mDoubleTapTimeOut 时间（毫秒）
     * @return self
     */
    public GestureLite setDoubleTapTimeOut(int mDoubleTapTimeOut) {
        this.mDoubleTapTimeOut = mDoubleTapTimeOut;
        return this;
    }

    /**
     * 设置长按超时时间
     *
     * @param mLongPressTimeOut 时间（毫秒）
     * @return self
     */
    public GestureLite setLongPressTimeOut(int mLongPressTimeOut) {
        this.mLongPressTimeOut = mLongPressTimeOut;
        return this;
    }

    /**
     * 是否启用缩放手势
     *
     * @return {@code true}开启，{@code false}禁用
     */
    public boolean isScaleEnable() {
        return isScaleEnable;
    }

    /**
     * 是否启用滑动手势
     *
     * @return {@code true}开启，{@code false}禁用
     */
    public boolean isScrollEnable() {
        return isScrollEnable;
    }

    /**
     * 是否启用快速滑动手势
     *
     * @return {@code true}开启，{@code false}禁用
     */
    public boolean isFlingEnable() {
        return isFlingEnable;
    }

    /**
     * 是否启用长按手势
     *
     * @return {@code true}开启，{@code false}禁用
     */
    public boolean isLongPressEnable() {
        return isLongPressEnable;
    }

    /**
     * 是否启用双击手势
     *
     * @return {@code true}开启，{@code false}禁用
     */
    public boolean isDoubleTapEnable() {
        return isDoubleTapEnable;
    }

    /**
     * 返回双击最大间隔时间
     *
     * @return 间隔时间
     */
    public int getDoubleTapTimeOut() {
        return mDoubleTapTimeOut;
    }

    /**
     * 返回长按间隔时间
     *
     * @return 间隔时间
     */
    public int getLongPressTimeOut() {
        return mLongPressTimeOut;
    }

    /**
     * 简单手势监听器
     *
     * @deprecated OnGestureListener 使用默认接口，这类没有存在意义了
     */
    @Deprecated
    public static class SimpleListener implements OnGestureListener {

        @Override
        public boolean onDown(@NonNull MotionEvent e) {
            return false;
        }
    }
}
