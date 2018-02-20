/*
 * Copyright 2017 Laboratoire I3S, CNRS, Université côte d'azur
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package fr.unice.i3s.uca4svr.toucan_vr.realtimeUserPosition;


import android.os.AsyncTask;

import org.gearvrf.GVRContext;
import org.gearvrf.GVRTransform;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

public class PushRealtimeEvents extends AsyncTask<RealtimeEvent,Integer,Boolean> {

    private GVRContext context;
    private PushResponse callback;
    private String serverIP;
    private final static String pushForHeadMotion = "/logHeadMotion.php";
    private final static String pushForTapEvent = "/logTapEvent.php";


    public PushRealtimeEvents(GVRContext context, String serverIP, PushResponse callback) {
        this.context = context;
        this.serverIP = serverIP;
        this.callback = callback;
    }

    @Override
    protected Boolean doInBackground(RealtimeEvent... events) {
        for(RealtimeEvent event : events) {
            if(!push(event)) return false;
        }
        return true;
    }

    @Override
    protected void onPostExecute(Boolean result) {
        callback.pushResponse(result);
    }

    private Boolean push(RealtimeEvent event) {
        String fullURI = "";
        if (event.eventType) {
            GVRTransform headTransform = context.getMainScene().getMainCameraRig().getHeadTransform();
            fullURI = serverIP + pushForHeadMotion +
                    "?timestamp=" + event.timestamp +
                    "&x=" + headTransform.getRotationPitch() +
                    "&y=" + headTransform.getRotationYaw() +
                    "&z=" + headTransform.getRotationRoll();
        } else {
            fullURI = serverIP + pushForTapEvent +
                    "?timestamp=" + event.timestamp;
        }
        if(fullURI.length()>0) {
            try {
                HttpURLConnection urlc = (HttpURLConnection) (new URL(fullURI).openConnection());
                urlc.setRequestProperty("User-Agent", "Test");
                urlc.setRequestProperty("Connection", "close");
                urlc.setConnectTimeout(1500);
                urlc.connect();
                return (urlc.getResponseCode() == 200);
            } catch (IOException e) {
                return false;
            }
        } else {
            return false;
        }
    }
}