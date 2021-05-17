package NormalObjects;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class PlayButton extends androidx.appcompat.widget.AppCompatButton {
    private boolean startPlaying = true;
    OnClickListener clicker = new OnClickListener() {
        @Override
        public void onClick(View v) {
            /*onPlay(startPlaying);
            if (startPlaying) {
                setText("Stop playing");
            } else {
                setText("Start playing");
            }
            startPlaying = !startPlaying;*/
        }
    };
    public PlayButton(Context context){
        super(context);
        setText("Start Playing");
        setOnClickListener(clicker);
    }

    public PlayButton(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public PlayButton(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public void setStartPlaying(boolean startPlaying){
        this.startPlaying = startPlaying;
    }

    public boolean isStartPlaying() {
        return startPlaying;
    }
}