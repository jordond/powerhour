/*
 * Copyright (C) 2015, Jordon de Hoog
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 */
package ca.hoogit.powerhour.Audio;

import android.content.Context;
import android.media.MediaRecorder;
import android.os.CountDownTimer;
import android.os.Environment;
import android.util.Log;

import java.io.IOException;

import ca.hoogit.powerhour.R;
import mbanje.kurt.fabbutton.FabButton;

/**
 * @author jordon
 *
 * Date    27/07/15
 * Description 
 *
 */
public class SoundRecorder {

    private static final String TAG = SoundRecorder.class.getSimpleName();
    private static final String FILE_NAME = "recorded_sound.3gp";
    private static final int TICK_LENGTH = 100;
    private static final long RECORD_LENGTH_MILLIS = 10 * 1000L; // 10 seconds
    public static final int CUSTOM_RECORDED_SOUND = -1;

    private Context mContext;
    private FabButton mButton;

    private MediaRecorder mRecorder = null;
    private CountDownTimer mTimer = null;
    private String mFileName;
    private boolean mIsActive;

    public interface OnRecordFinished {
        void recordingFinished(String filename);
    }
    private OnRecordFinished mListener;

    public SoundRecorder(Context context, FabButton button, OnRecordFinished listener) {
        this.mContext = context;
        this.mButton = button;
        this.mListener = listener;
    }

    public void toggle(boolean start) {
        if (start) {
            start();
        } else {
            stop();
        }
    }

    private void start() {
        mFileName = mContext.getCacheDir().getAbsolutePath() + FILE_NAME;
        mRecorder = new MediaRecorder();
        mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        mRecorder.setOutputFile(mFileName);
        mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);

        try {
            mRecorder.prepare();
            mIsActive = true;
        } catch (IOException e) {
            Log.e(TAG, "prepare() failed");
        }

        refreshButton(true);
        createTimer();
        mRecorder.start();
    }

    private void createTimer() {
        this.mTimer = new CountDownTimer(RECORD_LENGTH_MILLIS, TICK_LENGTH) {
            public void onTick(long millisUntilFinished) {
                long elapsed = RECORD_LENGTH_MILLIS - (millisUntilFinished - 100L);
                float progress = (float) elapsed / RECORD_LENGTH_MILLIS * 100.0f;
                if (mButton != null) {
                    mButton.setProgress(progress);
                }
            }

            public void onFinish() {
                mTimer = null;
                stop();
            }
        };
        this.mTimer.start();
    }

    private void stop() {
        refreshButton(false);
        if (mRecorder != null) {
            mRecorder.stop();
            mRecorder.release();
            mRecorder = null;
            mIsActive = false;
            if (mListener != null) {
                mListener.recordingFinished(mFileName);
            }
        }
    }

    private void refreshButton(boolean isRecording) {
        if (isRecording) {
            this.mButton.setIcon(R.drawable.ic_av_stop, R.drawable.ic_av_mic);
        } else {
            this.mButton.setIcon(R.drawable.ic_av_mic, R.drawable.ic_av_stop);
        }
        this.mButton.showProgress(isRecording);
        this.mButton.setProgress(0);
    }


    public boolean isActive() {
        return mIsActive;
    }
}
