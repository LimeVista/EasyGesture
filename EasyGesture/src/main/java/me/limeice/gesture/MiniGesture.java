package me.limeice.gesture;

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
 */

public final class MiniGesture implements DefaultDetector {

    private static final int LONG_PRESS = 0x01;

    /**
     * 当使用此接口时，OnDrag, OnLongPress, OnTap单独接口全部忽略
     */
    public interface OnGestureListener extends OnDrag, OnLongPress, OnTap {

    }

    /**
     * 监听线程
     */
    private final class GestureHandler extends Handler {
        GestureHandler() {
            super();
        }

        GestureHandler(Handler handler) {
            super(handler.getLooper());
        }

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case LONG_PRESS:
                    dispatchLongPress();
                    break;
                default:
                    throw new RuntimeException("Unknown gesture" + msg); //who cares
            }
        }
    }

    private GestureHandler mHandler;

    private MotionEvent mCurEvent;

    private OnGestureListener mListener;

    private OnDrag mDrag;

    private OnLongPress mLongPress;

    private OnTap mTap;

    private int mLongPressTimeOut = 500;

    private int mTouchSlopSquare;

    private float mLastFocusX, mLastFocusY;

    private boolean mAlwaysInTapRegion;

    private boolean mInLongPress;

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
                    mHandler.removeMessages(LONG_PRESS);
                    if ((dx * dx) + (dy * dy) > mTouchSlopSquare) {
                        mAlwaysInTapRegion = false;
                        mLastFocusX = e.getX();
                        mLastFocusY = e.getY();
                    }
                    if (mListener != null)
                        mListener.onDrag(dx, dy);
                    else
                        mDrag.onDrag(dx, dy);
                } else {
                    if (Math.abs(dx) > 1 || Math.abs(dy) > 1) {
                        if (mListener != null)
                            mListener.onDrag(dx, dy);
                        else
                            mDrag.onDrag(dx, dy);
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
        mDrag = (x, y) -> {
        };
        mLongPress = e -> {
        };
        mTap = e -> {
        };
    }
}
