package me.limeice.easygesture

import android.annotation.SuppressLint
import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class SimpleActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_simple)

        setButtonGesture()

        val customView = findViewById<CustomView>(R.id.custom_view)
        customView.gesture.isScaleEnable = true
        customView.gesture.isScrollEnable = true
    }

    @SuppressLint("SetTextI18n")
    private fun setButtonGesture() {
        val text = findViewById<TextView>(R.id.display_text)
        val gesture = findViewById<GestureButton>(R.id.gesture_button).gesture
        gesture.setTap {
            Toast.makeText(this, "Button Click!", Toast.LENGTH_SHORT).show()
        }.setDrag { _, dx, dy ->
            text.text = "dx->$dx,dy->$dy"
        }.setLongPress {
            Toast.makeText(this, "Button Long Press!", Toast.LENGTH_SHORT).show()
        }.setLongPressTimeOut(600)
    }
}
