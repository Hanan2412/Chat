package NormalObjects;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatImageButton;

import com.example.woofmeow.R;

public class PlayAudioButton extends AppCompatImageButton {
    public interface onPlayAudio{
        void playAudio();
        void pauseAudio();
    }
    private int clicks = 0;
    private onPlayAudio listener;

    public PlayAudioButton(@NonNull Context context) {
        super(context);
    }

    public PlayAudioButton(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public PlayAudioButton(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public int getClicks() {
        return clicks;
    }

    public void setListener(onPlayAudio listener) {
        this.listener = listener;
    }

    @Override
    public boolean performClick() {
        return super.performClick();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_UP)
        {
            clicks++;
            if (clicks % 2 == 1) {
                setImageResource(R.drawable.ic_baseline_pause_circle_outline_24);
                listener.playAudio();
            } else {
                setImageResource(R.drawable.ic_baseline_play_circle_outline_24);
                listener.pauseAudio();
            }
        }
        performClick();
        return super.onTouchEvent(event);
    }
    public void resetClicks()
    {
        clicks = 0;
        setImageResource(R.drawable.ic_baseline_play_circle_outline_24);
    }
}
