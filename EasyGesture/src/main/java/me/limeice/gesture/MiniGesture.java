package me.limeice.gesture;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.view.MotionEvent;
import android.view.ViewConfiguration;

import me.limeice.gesture.standard.DefaultDetector;
import me.limeice.gesture.standard.OnDrag;
import me.limeice.gesture.standard.OnLongPress;
import me.limeice.gesture.standard.OnTap;

/**
 * Created by LimeV on 2017/12/3.
 *
 * @author LimeVista
 * @version 1.0
 */

public final class MiniGesture implements DefaultDetector {

    private static final int LONG_PRESS = 0x01;

    /**
     * 当使用此接口时，OnDrag, OnLongPress, OnTap单独接口全部忽略
     */
    public interface OnGestureListener extends OnDrag, OnLongPress, OnTap {

    }

    /**
     * 监听线程，不存在
     */
    @SuppressLint("HandlerLeak")
    private final class GestureHandler extends Handler {

        GestureHandler() {
            super();
        }

        GestureHandler(Handler handler) {
            super(handler.getLooper());
        }

        @Override
        public void handleMessage(Message msg) {
            //noinspection SwitchStatementWithTooFewBranches
            switch (msg.what) {
                case LONG_PRESS:
                    dispatchLongPress();
                    break;
                default:
                    throw new RuntimeException("Unknown gesture" + msg);
            }
        }
    }

    private GestureHandler mHandler;        // 长按处理事件驱动

    private MotionEvent mCurEvent;          // 当前事件

    private OnGestureListener mListener;    // 总监听事件

    private OnDrag mDrag;                   // 拖拽事件

    private OnLongPress mLongPress;         // 长按事件

    private OnTap mTap;                     // 单击事件

    private int mLongPressTimeOut = 500;    // 长按超时

    private int mTouchSlopSquare;           // 触摸超出范围区域

    private float mLastFocusX, mLastFocusY; // 上一次焦点 x,y 轴值

    private boolean mAlwaysInTapRegion;     // 判定点击

    private boolean mInLongPress;           // 长按是否响应


    public MiniGesture(Context context) {
        this(context, null);
    }

    public MiniGesture(Context context, Handler handler) {
        if (handler == null)
            mHandler = new GestureHandler();
        else
            mHandler = new GestureHandler(handler);
        init(context);
    }

    /**
     * 执行长按事件
     */
    private void dispatchLongPress() {
        mInLongPress = true;
        if (mListener != null)
            mListener.onLongPress(mCurEvent);
        else
            mLongPress.onLongPress(mCurEvent);
    }

    @Override
    public boolean onTouchEvent(MotionEvent e) {
        switch (e.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mHandler.sendEmptyMessageDelayed(LONG_PRESS, mLongPressTimeOut);
                mAlwaysInTapRegion = true;
                mLastFocusX = e.getX();
                mLastFocusY = e.getY();
                mInLongPress = false;
                if (mCurEvent != null)
                    mCurEvent.recycle();
                mCurEvent = MotionEvent.obtain(e);
                return true;

            case MotionEvent.ACTION_MOVE:
                if (mInLongPress)
                    return false;
                final float dx = e.getX() - mLastFocusX;
                final float dy = e.getY() - mLastFocusY;
                if (mAlwaysInTapRegion) {
                    if ((dx * dx) + (dy * dy) > mTouchSlopSquare) {
                        mAlwaysInTapRegion = false;
                        mLastFocusX = e.getX();
                        mLastFocusY = e.getY();
                        mHandler.removeMessages(LONG_PRESS);
                        if (mListener != null)
                            mListener.onDrag(e, dx, dy);
                        else
                            mDrag.onDrag(e, dx, dy);
                    }
                } else {
                    if (Math.abs(dx) > 1 || Math.abs(dy) > 1) {
                        if (mListener != null)
                            mListener.onDrag(e, dx, dy);
                        else
                            mDrag.onDrag(e, dx, dy);
                        mLastFocusX = e.getX();
                        mLastFocusY = e.getY();
                    }
                }
                break;

            case MotionEvent.ACTION_UP:
                if (mInLongPress)
                    return false;
                mHandler.removeMessages(LONG_PRESS);
                if (mAlwaysInTapRegion) {
                    if (mListener != null)
                        mListener.onTap(e);
                    else
                        mTap.onTap(e);
                }
                return true;
        }
        return false;
    }

    private void init(Context context) {
        ViewConfiguration config = ViewConfiguration.get(context);
        int touchSlop = config.getScaledTouchSlop();
        mTouchSlopSquare = touchSlop * touchSlop;
        mDrag = (e, x, y) -> {
        };
        mLongPress = e -> {
        };
        mTap = e -> {
        };
    }

    /**
     * 设置所有监听事件，当此监听事件代理为{@code null}时，使用分监听事件，不为 null 时全部使用此事件
     *
     * @param listener 监听事件
     * @return self
     */
    public MiniGesture setOnGestureListener(OnGestureListener listener) {
        this.mListener = listener;
        return this;
    }

    /**
     * 设置拖拽事件，当{@link OnGestureListener} 不为 null 时失效
     *
     * @param drag 拖拽事件
     * @return self
     */
    public MiniGesture setDrag(OnDrag drag) {
        if (drag == null)
            mDrag = (e, x, y) -> {
            };
        else
            this.mDrag = drag;
        return this;
    }

    /**
     * 设置长按事件，当{@link OnGestureListener} 不为 null 时失效
     *
     * @param longPress 长按事件
     * @return self
     */
    public MiniGesture setLongPress(OnLongPress longPress) {
        if (longPress == null)
            mLongPress = e -> {
            };
        else
            this.mLongPress = longPress;
        return this;
    }

    /**
     * 设置单击事件，当{@link OnGestureListener} 不为 null 时失效
     *
     * @param tap 单击事件
     * @return self
     */
    public MiniGesture setTap(OnTap tap) {
        if (tap == null)
            mTap = e -> {
            };
        else
            this.mTap = tap;
        return this;
    }

    /**
     * 设置长按超时时间
     *
     * @param longPressTimeOut 时间
     * @return self
     */
    public MiniGesture setLongPressTimeOut(int longPressTimeOut) {
        this.mLongPressTimeOut = longPressTimeOut;
        return this;
    }
}
