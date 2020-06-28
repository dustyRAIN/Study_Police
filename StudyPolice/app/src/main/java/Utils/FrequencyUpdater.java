package Utils;

import android.content.Context;
import android.util.Log;

import com.google.gson.JsonObject;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;

import static AllConstants.RestApiUrlsAndKeys.API_STATS_FREQUENCY_UPDATE;

public class FrequencyUpdater {

    public static void updateFrequency(Context context, String token, int content_id, int content_type, int user, int frequency){
        Ion.with(context)
                .load("POST", API_STATS_FREQUENCY_UPDATE)
                .setHeader("X-CSRFToken", token)
                .setMultipartParameter("content_id", String.valueOf(content_id))
                .setMultipartParameter("content_type", String.valueOf(content_type))
                .setMultipartParameter("user", String.valueOf(user))
                .setMultipartParameter("frequency", String.valueOf(frequency))
                .asJsonObject()
                .setCallback(new FutureCallback<JsonObject>() {
                    @Override
                    public void onCompleted(Exception e, JsonObject result) {
                        if(e != null){
                            Log.d("freq_upd", e.toString());
                        }
                    }
                });
    }
}
