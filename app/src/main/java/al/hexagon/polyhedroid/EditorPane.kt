package al.hexagon.polyhedroid

import android.content.Context
import android.inputmethodservice.Keyboard
import android.util.AttributeSet
import android.util.Log
import android.view.KeyEvent
import android.widget.EditText
import android.widget.TextView
import kotlin.properties.Delegates
import androidx.appcompat.widget.AppCompatEditText

// Apparently creation from XML uses the _two- or three-argument_ constructors
class EditorPane(context: Context, attrs: AttributeSet) : androidx.appcompat.widget.AppCompatEditText(context, attrs) {

    // Custom size-change listener (for alerting the parent to the keyboard opening/closing)
    var onHeightChanged = fun(h: Int, oldH: Int) {}

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        if(h != oldh)
            onHeightChanged(h, oldh)
    }



}