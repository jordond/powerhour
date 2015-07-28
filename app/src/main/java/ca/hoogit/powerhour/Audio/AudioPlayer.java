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
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.CountDownTimer;
import android.util.Log;

import java.io.IOException;

/**
 * @author jordon
 *
 * Date    27/07/15
 * Description 
 *
 */
public class AudioPlayer implements MediaPlayer.OnCompletionListener {

    private static final String TAG = AudioPlayer.class.getSimpleName();
    private final String RESOURCE_PREFIX = "android.resource://";
    private final int TICK_LENGTH = 100;
    private Context mContext;
    private MediaPlayer mMediaPlayer;
    private int mStreamType = AudioManager.STREAM_MUSIC;

    private CountDownTimer mTimer;

    public interface OnPlayback {
        void onPlaybackStart();
        void onPlaybackTick(float progress);
        void onFinishPlayback();
    }

    private OnPlayback mListener;

    public AudioPlayer(Context context) {
        this.mContext = context;
    }

    public AudioPlayer(Context context, Uri sound) {
        this.mContext = context;
        this.play(sound);
    }

    public boolean play(int soundId) {
        Uri file = Uri.parse(RESOURCE_PREFIX + this.mContext.getPackageName() + "/" + soundId);
        return play(file);
    }

    public boolean play(Uri soundUri) {
        if(this.mMediaPlayer != null && this.mMediaPlayer.isPlaying()) {
            this.stop();
        }
        if(this.create(soundUri)) {
            this.mMediaPlayer.setOnCompletionListener(this);
            this.mMediaPlayer.start();
            if(mListener != null) {
                mListener.onPlaybackStart();
            }
            this.createTimer((long)this.mMediaPlayer.getDuration());
            return this.mMediaPlayer.isPlaying();
        } else {
            return false;
        }
    }

    private boolean create(Uri soundUri) {
        try {
            if (this.mMediaPlayer == null) {
                this.mMediaPlayer = new MediaPlayer();
            }
            this.mMediaPlayer.setAudioStreamType(this.mStreamType);
            this.mMediaPlayer.setDataSource(this.mContext, soundUri);
            this.mMediaPlayer.prepare();
            this.mMediaPlayer.setLooping(false);
            Log.d(TAG, "Media player was created with uri: " + soundUri.toString());
            return true;
        } catch (IOException var4) {
            Log.e(TAG, "Error loading the sound file");
            var4.printStackTrace();
            return false;
        }
    }

    private void createTimer(final long duration) {
        this.mTimer = new CountDownTimer(duration, TICK_LENGTH) {
            public void onTick(long millisUntilFinished) {
                long elapsed = duration - (millisUntilFinished - 100L);
                float progress = (float)elapsed / (float)duration * 100.0F;

                if(mListener != null) {
                    mListener.onPlaybackTick(progress);
                }
            }

            public void onFinish() {
                mTimer = null;
            }
        };
        this.mTimer.start();
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        this.stop();
        if(this.mListener != null) {
            this.mListener.onFinishPlayback();
        }
    }

    public void stop() {
        if(this.mMediaPlayer != null) {
            if(this.mTimer != null) {
                this.mTimer.cancel();
            }
            this.mMediaPlayer.stop();
            this.mMediaPlayer.reset();
        }

    }

    public void destroy() {
        if(this.mMediaPlayer != null) {
            this.mMediaPlayer.release();
            this.mMediaPlayer = null;
            Log.d(TAG, "Media player was released and cleaned up");
        }
    }

    public boolean isPlaying() {
        return this.mMediaPlayer != null && this.mMediaPlayer.isPlaying();
    }

    public void setStreamType(int streamType) {
        this.mStreamType = streamType;
    }

    public void setListener(OnPlayback listener) {
        this.mListener = listener;
    }
}
