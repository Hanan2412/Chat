package NormalObjects;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;


import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatImageButton;

public class ImageButtonPlus extends AppCompatImageButton {
    private int pressCycle = 0;
    private int resetOnValue = -1;
    public ImageButtonPlus(@NonNull Context context) {
        super(context);
    }

    public ImageButtonPlus(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public ImageButtonPlus(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (pressCycle < 0)
            pressCycle = 0;
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            pressCycle++;
            if (pressCycle > resetOnValue)
                pressCycle = 0;
        }
        //performClick();
        return super.onTouchEvent(event);
    }

    @Override
    public boolean performClick() {
        return super.performClick();
    }

    public int getPressCycle()
    {
        return pressCycle;
    }

    public void setPressCycle(int value){this.pressCycle = value;}
    public void setResetOnValue(int value){resetOnValue = value;}
}
