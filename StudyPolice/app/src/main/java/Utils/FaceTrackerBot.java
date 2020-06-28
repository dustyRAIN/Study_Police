package Utils;

import android.content.Context;
import android.widget.Toast;

import com.google.android.gms.vision.face.Face;
import com.google.android.gms.vision.MultiProcessor;
import com.google.android.gms.vision.Tracker;

public class FaceTrackerBot implements MultiProcessor.Factory<Face> {

    private Context mContext;
    private EyeTracker.EyeTrackCallback mEyeTrackCallback;

    public FaceTrackerBot(Context mContext, EyeTracker.EyeTrackCallback mEyeTrackCallback) {
        this.mContext = mContext;
        this.mEyeTrackCallback = mEyeTrackCallback;
    }

    @Override
    public Tracker<Face> create(Face face) {
        Toast.makeText(mContext, "facory", Toast.LENGTH_LONG).show();
        return new EyeTracker(mContext, mEyeTrackCallback);
    }
}
