package al.hexagon.polyhedroid

import android.content.Context
import android.util.AttributeSet
import android.util.Log
import android.view.WindowInsets
import androidx.constraintlayout.widget.ConstraintLayout

class KeyboardAwareLayout(context: Context, attrs: AttributeSet) : ConstraintLayout(context, attrs) {
    override fun onApplyWindowInsets(insets: WindowInsets?): WindowInsets {
        Log.d("KeyboardAwareLayout", "Bottom inset: ${insets?.systemWindowInsetBottom}")
        return super.onApplyWindowInsets(insets)
    }
}