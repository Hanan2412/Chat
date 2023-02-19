package NormalObjects;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;


import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatImageButton;


import Consts.ButtonType;

public class ImageButtonPlus extends AppCompatImageButton {
    private int pressCycle = 0;
    private int resetOnValue = -1;
    private OnClick callback;
    private final String IMAGE_BUTTON_PLUS = "ImageButtonPlus";

    public interface ImageButtonListener{
        void onImageAdded(int image);
        void onImageSet();
        void onImageChanged(int image);
        void onImageMissing(ButtonType type);
        void onError(String msg);
        void onButtonStateChange(int buttonState);
        void onButtonTypeChange(ButtonType type);
    }

    protected OnClick onClickListener;
    protected ImageButtonListener listener;
    public void setOnClickListener(OnClick listener){
        onClickListener = listener;
    }
    public void setOnFullClickListener(OnClick onClickListener) {
        callback = onClickListener;
    }

    public ImageButtonPlus(@NonNull Context context) {
        super(context);
        onReset();
    }

    public ImageButtonPlus(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        onReset();
    }

    public ImageButtonPlus(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        onReset();
    }

    protected void onReset() {

    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (pressCycle < 0)
            pressCycle = 0;
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            pressCycle++;
            if (resetOnValue != -1)
                if (pressCycle > resetOnValue)
                    pressCycle = 0;
            if (callback != null)
                callback.onPress();
            if (onClickListener!=null)
                onClickListener.onPress();
            onButtonPress();
            Log.d(IMAGE_BUTTON_PLUS, "button was pressed");
        } else if (event.getAction() == MotionEvent.ACTION_UP) {
            if (callback != null)
                callback.onRelease();
            performClick();
            if (onClickListener!=null)
                onClickListener.onRelease();
            changeBtnRoll();
            changeBtnImage();
            runAnimation();
            onButtonRelease();
            Log.d(IMAGE_BUTTON_PLUS, "button was released");
        }
        return super.onTouchEvent(event);
    }

    @Override
    public boolean performClick() {
        return super.performClick();
    }

    public int getPressCycle() {
        return pressCycle;
    }

    public void setPressCycle(int value) {
        this.pressCycle = value;
    }

    public void setResetOnValue(int value) {
        resetOnValue = value;
    }


    public void setListener(ImageButtonListener listener) {
        this.listener = listener;
        Log.d(IMAGE_BUTTON_PLUS, "listener was set");
    }

    protected void changeBtnRoll()
    {

    }

    protected void changeBtnImage()
    {

    }

    protected void runAnimation()
    {

    }

    protected void onButtonPress()
    {

    }

    protected void onButtonRelease()
    {

    }
}

