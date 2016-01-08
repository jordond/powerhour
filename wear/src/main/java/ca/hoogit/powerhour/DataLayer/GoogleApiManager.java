/**
 * Copyright (C) 2015, Jordon de Hoog
 * <p/>
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 * <p/>
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * <p/>
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 */
package ca.hoogit.powerhour.DataLayer;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.Wearable;

/**
 * @author jordon
 *
 * Date    22/12/15
 * Description
 *
 */
public class GoogleApiManager {
    private static final String TAG = GoogleApiManager.class.getSimpleName();

    private static GoogleApiClient mGoogleApiClient;

    public static GoogleApiClient getInstance (Context context) {
        if(mGoogleApiClient == null) {
            if(context == null) {
                return null;
            }
            mGoogleApiClient = new GoogleApiClient.Builder(context)
                    .addApi(Wearable.API)
                    .addConnectionCallbacks(new GoogleApiClient.ConnectionCallbacks() {
                        @Override
                        public void onConnected(Bundle connectionHint) {
                            Log.d(TAG, "onConnected: Connected to GoogleApi");
                        }
                        @Override
                        public void onConnectionSuspended (int cause){
                            Log.e(TAG, "onConnectionSuspended: Google API client was suspended " + cause);
                        }
                    }).addOnConnectionFailedListener(new GoogleApiClient.OnConnectionFailedListener() {
                        @Override
                        public void onConnectionFailed(@NonNull ConnectionResult result) {
                            Log.e(TAG, "onConnectionFailed " + result.toString());
                        }
                    })
                    .build();
        }
        return mGoogleApiClient;
    }

    public static GoogleApiClient getInstance (Context context, GoogleApiClient.ConnectionCallbacks l1) {
        if(mGoogleApiClient == null) {
            if(context == null) {
                return null;
            }
            mGoogleApiClient = new GoogleApiClient.Builder(context)
                    .addApi(Wearable.API)
                    .addConnectionCallbacks(l1)
                    .addOnConnectionFailedListener(new GoogleApiClient.OnConnectionFailedListener() {
                        @Override
                        public void onConnectionFailed(@NonNull ConnectionResult result) {
                            Log.e(TAG, "onConnectionFailed " + result.toString());
                        }
                    })
                    .build();
        }
        return mGoogleApiClient;
    }
}
