package NormalObjects;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.HashMap;
import java.util.Map;

import Consts.ButtonType;

public class ImageButtonType extends ImageButtonPlus{

    private Map<ButtonType, Integer>buttonTypeImages;
    private ButtonType currentButtonType, previousBtnType;
    private final String IMAGE_BUTTON_TYPE = "image button type";
    private long clickStartTime = 0;
    private long clickEndTime = 0;

    public interface onTypeButtonClick{
        void onPress(ButtonType buttonType);
        void onRelease(ButtonType buttonType);
    }
    private onTypeButtonClick typeBtnClick;

    public ImageButtonType(@NonNull Context context) {
        super(context);
    }

    public ImageButtonType(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public ImageButtonType(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onReset() {
        onCurrentButtonTypeChange(ButtonType.undefined);
        buttonTypeImages = new HashMap<>();
        Log.d(IMAGE_BUTTON_TYPE, "reset was called");
    }

    public void setButtonTypeImages(Map<ButtonType, Integer>buttonTypeImages)
    {
        this.buttonTypeImages = buttonTypeImages;
        Log.d(IMAGE_BUTTON_TYPE, "button types were set");
        if (listener!=null)
            listener.onImageSet();
    }

    private void onCurrentButtonTypeChange(ButtonType newBtnType)
    {
        Log.d(IMAGE_BUTTON_TYPE, "current button type were set to: " + newBtnType.name());
        if (currentButtonType == null)
            previousBtnType = newBtnType;
        else
            previousBtnType = currentButtonType;
        currentButtonType = newBtnType;
    }

    public void setCurrentButtonType(ButtonType buttonType)
    {
        onCurrentButtonTypeChange(buttonType);
        changeBtnImage(buttonType);
    }

    private void changeBtnImage(ButtonType buttonType)
    {
        if (buttonTypeImages.containsKey(buttonType)) {
            int btnImage = buttonTypeImages.get(buttonType);
            setImageResource(btnImage);
            Log.d(IMAGE_BUTTON_TYPE, "button image was changed");
        }
        else
        {
            if (listener!=null)
                listener.onError("no image for this button type: " + buttonType.name());
            Log.e(IMAGE_BUTTON_TYPE, "no image for this button type: " + buttonType.name());
        }
    }

    public ButtonType getCurrentButtonType()
    {
        Log.d(IMAGE_BUTTON_TYPE,"returning current button type");
        return currentButtonType;
    }


    @Override
    protected void changeBtnRoll() {
        //this method is not implemented in this class
    }

    @Override
    protected void changeBtnImage() {
        //this method is not implemented in this class
    }

    @Override
    protected void runAnimation() {

    }

    public void setTypeBtnClickListener(onTypeButtonClick listener)
    {
        Log.d(IMAGE_BUTTON_TYPE, "button type listener was set");
        this.typeBtnClick = listener;
    }

    @Override
    protected void onButtonPress() {
        clickStartTime = System.currentTimeMillis();
    }

    @Override
    protected void onButtonRelease() {
        clickEndTime = System.currentTimeMillis();
        if(clickEndTime - clickStartTime >= 350)
        {
            Log.d(IMAGE_BUTTON_TYPE, "long click was called");

        }
        else {
            Log.d(IMAGE_BUTTON_TYPE, "onRelease was called");
            if (typeBtnClick != null)
                typeBtnClick.onRelease(getCurrentButtonType());
        }
    }

    public ButtonType getPreviousButtonType()
    {
        return previousBtnType;
    }
}
