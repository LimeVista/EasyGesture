package me.limeice.easygesture

import android.content.Context
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.widget.AppCompatButton
import android.util.AttributeSet
import android.view.MotionEvent
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_simple.*
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
        }.setDrag { e, dx, dy ->
            mText.text = "dx->$dx,dy->$dy"
        }.setLongPress {
            Toast.makeText(this, "Button Long Press!", Toast.LENGTH_SHORT).show()
        }.setLongPressTimeOut(600)
    }
}

class MyButton : AppCompatButton {

    open val gesture: MiniGesture

    constructor(context: Context) : this(context, null)

    constructor(context: Context, attrs: AttributeSet?) :
            this(context, attrs, android.support.v7.appcompat.R.attr.buttonStyle)

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        gesture = MiniGesture(context)
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        return super.onTouchEvent(event) or gesture.onTouchEvent(event)
    }

}
