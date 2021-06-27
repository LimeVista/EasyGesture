package me.limeice.easygesture

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Matrix
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.widget.Toast
import me.limeice.gesture.GestureLite

class CustomView : View {

    val gesture: GestureLite

    private var mBmp: Bitmap

    private var needInvalidate = false

    private var mMatrix: Matrix = Matrix()

    private val listener: GestureLite.OnGestureListener

    constructor(context: Context) : this(context, null)

    constructor(context: Context, attrs: AttributeSet?) :
            this(context, attrs, 0)

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        listener = object : GestureLite.OnGestureListener {

            override fun onTap(e: MotionEvent) {
                Toast.makeText(context, "Button Click!", Toast.LENGTH_SHORT).show()
            }

            override fun onDown(e: MotionEvent): Boolean = true

            override fun onScroll(e1: MotionEvent, e2: MotionEvent, distanceX: Float, distanceY: Float): Boolean {
                mMatrix.postTranslate(-distanceX, -distanceY)
                needInvalidate = true
                return true
            }

            override fun onScale(scale: Float, focusX: Float, focusY: Float): Boolean {
                mMatrix.postScale(scale, scale, focusX, focusY)
                needInvalidate = true
                return true
            }

            override fun onFling(e1: MotionEvent, e2: MotionEvent, velocityX: Float, velocityY: Float): Boolean {
                needInvalidate = true
                return false
            }

        }
        gesture = GestureLite(context, listener)
        mBmp = BitmapFactory.decodeResource(context.resources, R.drawable.bg_01)
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        val isOnTouch = super.onTouchEvent(event) or gesture.onTouchEvent(event)
        if (needInvalidate) {
            needInvalidate = false
            postInvalidateOnAnimation()
        }
        return isOnTouch
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        canvas?.drawBitmap(mBmp, mMatrix, null)
    }
}