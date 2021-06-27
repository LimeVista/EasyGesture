package me.limeice.easygesture

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import androidx.appcompat.widget.AppCompatButton
import me.limeice.gesture.MiniGesture

class GestureButton : AppCompatButton {

    val gesture: MiniGesture = MiniGesture(context)

    constructor(context: Context) : this(context, null)

    constructor(context: Context, attrs: AttributeSet?) :
            this(context, attrs, androidx.appcompat.R.attr.buttonStyle)

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) :
            super(context, attrs, defStyleAttr)

    override fun onTouchEvent(event: MotionEvent): Boolean {
        val ret = super.onTouchEvent(event) or gesture.onTouchEvent(event)
        if (MotionEvent.ACTION_UP == event.action) {
            performClick()
        }
        return ret
    }

    override fun performClick(): Boolean {
        super.performClick()
        return true
    }
}