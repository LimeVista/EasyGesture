# EasyGesture [![](https://jitpack.io/v/LimeVista/EasyGesture.svg)](https://jitpack.io/#LimeVista/EasyGesture)
## Android Gesture Detector
### 一个简单的Android手势控件可用于Java、Kotlin项目  
1. 继承一个控件，使用 <code>MiniGesture</code> 和 <code>GestureLite</code> 类,并重写 <code>onTouchEvent</code> 方法。
```kotlin
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
```
2. 使用此 `View` ,并重写接口。  
```kotlin
mButton.gesture.setTap {
    Toast.makeText(this, "Button Click!", Toast.LENGTH_SHORT).show()
}.setDrag { _, dx, dy ->
    mText.text = "dx->$dx,dy->$dy"
}.setLongPress {
    Toast.makeText(this, "Button Long Press!", Toast.LENGTH_SHORT).show()
}.setLongPressTimeOut(600)
```
### 更多实例详见Simple

### 使用
```groovy
allprojects {
	repositories {
		...
		maven { url 'https://jitpack.io' }
	}
}

implementation "com.github.LimeVista:EasyGesture:{version}"
```
