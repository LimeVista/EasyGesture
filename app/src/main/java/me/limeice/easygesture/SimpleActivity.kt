package me.limeice.easygesture

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Matrix
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.appcompat.widget.AppCompatButton
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_simple.*
import me.limeice.gesture.GestureLite
import me.limeice.gesture.MiniGesture

class SimpleActivity : AppCompatActivity() {

    companion object {
        private val TAG: String = SimpleActivity::class.java.simpleName
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_simple)
        mButton.gesture.setTap {
            Toast.makeText(this, "Button Click!", Toast.LENGTH_SHORT).show()
        }.setDrag { _, dx, dy ->
            mText.text = "dx->$dx,dy->$dy"
        }.setLongPress {
            Toast.makeText(this, "Button Long Press!", Toast.LENGTH_SHORT).show()
        }.setLongPressTimeOut(600)
        //overrideListener()
        mCustomView.gesture.isScaleEnable = true
        mCustomView.gesture.isScrollEnable = true
    }

    private fun overrideListener() {
        mButton.gesture.setOnGestureListener(object : MiniGesture.OnGestureListener {
            override fun onDrag(event: MotionEvent?, dx: Float, dy: Float) {
                mText.text = "override -> dx->$dx,dy->$dy"
            }

            override fun onTap(event: MotionEvent?) {
                Toast.makeText(this@SimpleActivity, "override -> Button Click!", Toast.LENGTH_SHORT).show()
            }

            override fun onLongPress(event: MotionEvent?) {
                Toast.makeText(this@SimpleActivity, "override -> Button Long Press!", Toast.LENGTH_SHORT).show()
            }
        })
    }
}

class MyButton : AppCompatButton {

    val gesture: MiniGesture

    constructor(context: Context) : this(context, null)

    constructor(context: Context, attrs: AttributeSet?) :
        this(context, attrs, androidx.appcompat.R.attr.buttonStyle)

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        gesture = MiniGesture(context)
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        return super.onTouchEvent(event) or gesture.onTouchEvent(event)
    }
}

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
            override fun onLongPress(e: MotionEvent?) {}

            override fun onDoubleTap(e: MotionEvent?) {}

            override fun onTap(e: MotionEvent?) {
                Toast.makeText(context, "Button Click!", Toast.LENGTH_SHORT).show()
            }

            override fun onDown(e: MotionEvent?): Boolean = true

            override fun onScroll(e1: MotionEvent?, e2: MotionEvent?, distanceX: Float, distanceY: Float): Boolean {
                mMatrix.postTranslate(-distanceX, -distanceY)
                needInvalidate = true
                return true
            }

            override fun onScale(scale: Float, focusX: Float, focusY: Float): Boolean {
                mMatrix.postScale(scale, scale, focusX, focusY)
                needInvalidate = true
                return true
            }

            override fun onFling(e1: MotionEvent?, e2: MotionEvent?, velocityX: Float, velocityY: Float): Boolean {
                needInvalidate = true
                return false
            }

        }
        gesture = GestureLite(context, listener)
        mBmp = BitmapFactory.decodeResource(context.resources, R.drawable.bg_01)
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        val isOnTouch: Boolean = super.onTouchEvent(event) or gesture.onTouchEvent(event)
        if (needInvalidate) {
            needInvalidate = false
            invalidate()
        }
        return isOnTouch
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        canvas?.drawBitmap(mBmp, mMatrix, null)
    }
}
