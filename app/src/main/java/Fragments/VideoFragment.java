package Fragments;

import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.MediaController;
import android.widget.VideoView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.example.woofmeow.R;


@SuppressWarnings("Convert2Lambda")
public class VideoFragment extends Fragment {


    private static final String URI = "uri";
    private static final String RECORD = "record";
    public static VideoFragment getInstance(Uri uri,boolean record)
    {
        Bundle bundle = new Bundle();
        bundle.putParcelable(URI,uri);
        bundle.putBoolean(RECORD,record);
        VideoFragment videoFragment = new VideoFragment();
        videoFragment.setArguments(bundle);
        return videoFragment;
    }
    public interface onVideo{
        void onSendVideo();
    }
    private onVideo listener;
    public void setListener(onVideo listener){this.listener = listener;}
    private VideoFragment(){}

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.video_layout,container,false);
        VideoView video = view.findViewById(R.id.videoMessage);
        ImageButton closeBtn = view.findViewById(R.id.closeVideoBtn);
        ImageButton sendVideo = view.findViewById(R.id.sendVideo);
        Bundle arguments = getArguments();
        if (arguments!=null) {
            Uri uri = arguments.getParcelable(URI);
            if (uri!=null)
            {
                MediaController mediaController = new MediaController(requireContext());
                video.setMediaController(mediaController);
                video.setVideoURI(uri);
                video.seekTo(1);//shows the first frame of the video
                video.start();
                mediaController.show();
            }
            boolean record = arguments.getBoolean(RECORD);
            if (!record)
                sendVideo.setVisibility(View.GONE);
        }
        closeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //closes the fragment
                if (video.canPause())
                    video.pause();
                requireActivity().getSupportFragmentManager().beginTransaction().remove(VideoFragment.this).commit();
            }
        });
        sendVideo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.onSendVideo();
            }
        });
        view.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return true;
            }
        });
        return view;
    }
}
