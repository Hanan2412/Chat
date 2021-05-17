package NormalObjects;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class RecordButton extends androidx.appcompat.widget.AppCompatButton{
    private boolean startRecording = true;

    OnClickListener clicker = new OnClickListener() {
        @Override
        public void onClick(View v) {
           /* onRecord(startRecording);
            if(startRecording)
                setText("Stop Recording");
            else
                setText("Start Recording");
            startRecording = !startRecording;*/
        }
    };
    public RecordButton(Context context){
        super(context);
        setText("Start Recording");
        setOnClickListener(clicker);
    }

    public RecordButton(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public RecordButton(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public void setStartRecording(boolean startRecording)
    {
        this.startRecording = startRecording;
    }

    public boolean isStartRecording(){
        return startRecording;
    }
}