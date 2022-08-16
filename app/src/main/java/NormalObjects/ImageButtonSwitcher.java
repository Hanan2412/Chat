package NormalObjects;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.ImageSwitcher;

import com.example.woofmeow.R;

public class ImageButtonSwitcher extends ImageSwitcher {

    public interface onClick{
        void onPress(int buttonState);
        void onRelease(int buttonState);
    }
    public static final int RECORD_VOICE = 1;
    public static final int SEND_MESSAGE = 0;
    private int buttonState;
    private onClick onClickListener;

    public ImageButtonSwitcher(Context context) {
        super(context);
        buttonState = ImageButtonSwitcher.RECORD_VOICE;
    }

    public ImageButtonSwitcher(Context context, AttributeSet attrs) {
        super(context, attrs);
        buttonState = ImageButtonSwitcher.RECORD_VOICE;
    }

    public void setButtonState(int buttonState)
    {
        if (this.buttonState != buttonState) {
            if (buttonState == ImageButtonSwitcher.RECORD_VOICE)
                setImageResource(R.drawable.ic_baseline_mic_black);
            else if (buttonState == ImageButtonSwitcher.SEND_MESSAGE)
                setImageResource(R.drawable.ic_baseline_send_24);
            this.buttonState = buttonState;
        }
    }

    public int getButtonState()
    {
        return buttonState;
    }

    public void setOnClickListener(onClick listener)
    {
        onClickListener = listener;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN)
            onClickListener.onPress(getButtonState());
        else if (event.getAction() == MotionEvent.ACTION_UP)
            onClickListener.onRelease(getButtonState());
//        if (getButtonState() == RECORD_VOICE)
//            performLongClick();
//        else if (getButtonState() == SEND_MESSAGE)
//            performClick();
        return true;
    }

    @Override
    public boolean performClick() {
        return super.performClick();
    }

    @Override
    public boolean performLongClick() {
        return super.performLongClick();
    }
}
