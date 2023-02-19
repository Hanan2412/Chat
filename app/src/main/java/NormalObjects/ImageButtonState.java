package NormalObjects;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

import Consts.ButtonType;

public class ImageButtonState extends ImageButtonPlus{

    // iterates over possible states
    private List<Integer>states;
    private int currentStateIndex;
    private List<Integer>images;
    private final String IMAGE_BUTTON_STATE = "imageButtonState";

    public ImageButtonState(@NonNull Context context) {
        super(context);
    }

    public ImageButtonState(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public ImageButtonState(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onReset() {
        currentStateIndex = -1;
        images = new ArrayList<>();
    }

    public void setBtnStates(List<Integer>states)
    {
        this.states = states;
        changeBtnRoll();
        changeBtnImage();
    }

    public void setImages(List<Integer>images){
        this.images = images;
        Log.d(IMAGE_BUTTON_STATE, "images were set");
    }

    public void changeBtnState(int newState)
    {
        currentStateIndex = states.indexOf(newState);
        changeBtnImage();
        Log.d(IMAGE_BUTTON_STATE, "change button state was called to state: " + newState);
    }
    public void nextState()
    {
        changeBtnRoll();
        changeBtnImage();
        Log.d(IMAGE_BUTTON_STATE, "next state was called");
    }

    public int getBtnState()
    {
        return states.get(currentStateIndex);
    }

    public void addBtnState(int buttonState, int buttonStateImage)
    {
        states.add(buttonState);
        images.add(buttonStateImage);
        Log.d(IMAGE_BUTTON_STATE, "new state and image were added");
        if (listener!=null)
            listener.onImageAdded(buttonStateImage);
    }

    @Override
    protected void changeBtnRoll() {
        currentStateIndex++;
        Log.d(IMAGE_BUTTON_STATE, "button state was changed");
        if(currentStateIndex >= states.size())
        {
            currentStateIndex = 0;
            Log.d(IMAGE_BUTTON_STATE, "button state was set to 0");
        }
        if (listener!=null)
            listener.onButtonStateChange(states.get(currentStateIndex));
    }

    @Override
    protected void changeBtnImage() {
        Log.d(IMAGE_BUTTON_STATE, "new image was set");
        setImageResource(images.get(currentStateIndex));
        if (listener!=null)
            listener.onImageChanged(images.get(currentStateIndex));
    }


    @Override
    protected void runAnimation() {
        //no animation in this class
    }
}
