package NormalObjects;


import android.view.MotionEvent;

public class GestureDetector extends android.view.GestureDetector.SimpleOnGestureListener {

    private final int SWIPE_MAX_OFF_PATH = 100;
    private final int SWIPE_MIN_DISTANCE = 300;
    @Override
    public boolean onDoubleTap(MotionEvent e) {
        return super.onDoubleTap(e);
    }

    @Override
    public boolean onDown(MotionEvent e) {
        return true;
    }

    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {

        return super.onFling(e1, e2, velocityX, velocityY);
    }

    @Override
    public void onLongPress(MotionEvent e) {
        super.onLongPress(e);
    }

    @Override
    public boolean onSingleTapConfirmed(MotionEvent e) {
        return super.onSingleTapConfirmed(e);
    }
}
