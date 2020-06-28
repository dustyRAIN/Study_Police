package Utils;

import android.content.Context;

import com.google.android.gms.vision.Detector;
import com.google.android.gms.vision.Tracker;
import com.google.android.gms.vision.face.Face;

public class EyeTracker extends Tracker<Face> {

    private final float THRESHOLD = 0.70f;
    private Context mContext;
    private EyeTrackCallback mEyeTrackCallback;

    public interface EyeTrackCallback{
        void onEyeDetected();
        void onEyeNotDetected();
    }

    public EyeTracker(Context mContext, EyeTrackCallback mEyeTrackCallback) {
        this.mContext = mContext;
        this.mEyeTrackCallback = mEyeTrackCallback;
    }

    @Override
    public void onUpdate(Detector.Detections<Face> detections, Face face) {
        if(face.getIsLeftEyeOpenProbability() >= THRESHOLD || face.getIsRightEyeOpenProbability() >= THRESHOLD){
            mEyeTrackCallback.onEyeDetected();
        } else {
            mEyeTrackCallback.onEyeNotDetected();
        }
    }

    @Override
    public void onMissing(Detector.Detections<Face> detections) {
        super.onMissing(detections);
        mEyeTrackCallback.onEyeNotDetected();
    }
}
