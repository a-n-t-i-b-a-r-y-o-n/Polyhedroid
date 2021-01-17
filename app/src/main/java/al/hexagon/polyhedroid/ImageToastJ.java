package al.hexagon.polyhedroid;

import android.content.Context;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

public class ImageToastJ {

    public static Toast makeImage(Context context, int resId, int length){
        Toast t = Toast.makeText(context, null, length);
        t.setView(View.inflate(context, R.layout.imagetoast, null));
        ((ImageView)((LinearLayout) t.getView()).getChildAt(0)).setImageResource(resId);
        return t;
    }

}
