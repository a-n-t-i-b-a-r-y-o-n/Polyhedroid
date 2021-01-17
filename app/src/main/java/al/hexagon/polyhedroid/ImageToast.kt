package al.hexagon.polyhedroid

import android.content.Context
import android.view.View
import android.widget.Toast

import kotlinx.android.synthetic.main.imagetoast.view.*

class ImageToast {

    companion object {
        fun makeImage(context: Context, resId: Int, length: Int) : Toast {
            return Toast.makeText(context, null, length).also {
                it.view = View.inflate(context, R.layout.imagetoast, null)
                it.view.image.setImageResource(resId)
            }
        }


        fun Toast.setImagePadding(left: Int, top: Int, right: Int, bottom: Int){ this.view.image.setPadding(left, top, right, bottom) }
    }


}