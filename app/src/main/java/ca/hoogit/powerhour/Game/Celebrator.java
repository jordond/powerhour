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
package ca.hoogit.powerhour.Game;

import android.content.Context;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Handler;
import android.util.Log;

import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;

import ca.hoogit.powerhour.Audio.AudioPlayer;
import ca.hoogit.powerhour.Audio.SoundRecorder;
import ca.hoogit.powerhour.Configure.GameOptions;
import ca.hoogit.powerhour.Util.BusProvider;

/**
 * @author jordon
 *
 * Date    28/07/15
 * Description 
 *
 */
public class Celebrator implements AudioPlayer.OnPlayback {

    private static final String TAG = Celebrator.class.getSimpleName();

    private static final int STREAM_TYPE = AudioManager.STREAM_ALARM;
    private static final long DEFAULT_DELAY = 1000;

    private AudioPlayer mAudioPlayer;
    private GameOptions mOptions;

    private Bus mBus;
    private boolean isActive;

    private String mCustomSoundPath;

    public Celebrator(Context context, GameOptions options) {
        this.mOptions = options;
        mAudioPlayer = new AudioPlayer(context);
        mAudioPlayer.setStreamType(STREAM_TYPE);
        mAudioPlayer.setListener(this);
        mBus = BusProvider.getInstance();
        mBus.register(this);
        mCustomSoundPath = context.getCacheDir().getAbsolutePath() + SoundRecorder.FILE_NAME;
    }

    public boolean celebrate() {
        if (!isActive && !mOptions.isMuted()) {
            if (mOptions.getShotSound() == -1) {
                mAudioPlayer.play(Uri.parse(mCustomSoundPath));
            } else {
                mAudioPlayer.play(mOptions.getShotSound());
            }
        }
        return mAudioPlayer.isPlaying();
    }

    public void stop() {
        if (isActive) {
            mAudioPlayer.stop();
            mBus.post(new CelebrationEvent(CelebrationEvent.ACTION_FINISH));
        }
    }

    public void finish() {
        if (mAudioPlayer != null) {
            if (mAudioPlayer.isPlaying()) {
                mAudioPlayer.stop();
            }
            mAudioPlayer.destroy();
            mAudioPlayer = null;
            mBus.unregister(this);
        }
    }

    @Subscribe
    public void onGameEvent(GameEvent event) {
        switch (event.action) {
            case UPDATE_SETTINGS:
                if (event.game != null) {
                    mOptions = event.game.options();
                    if (mOptions.isMuted() && isActive) {
                        stop();
                        mBus.post(new GameEvent(Action.RESUME));
                    }
                }
                break;
            case UPDATE:
                if (event.game != null) {
                    if (event.game.is(State.NEW_ROUND) && !isActive) {
                        if (celebrate()) {
                            isActive = true;
                        } else {
                            delayResume(DEFAULT_DELAY);
                        }
                    } else if (event.game.is(State.ACTIVE) && isActive) {
                        stop();
                        isActive = false;
                    }
                }
                break;
            case STOP:
            case FINISH:
                finish();
                break;
        }
    }

    @Override
    public void onPlaybackStart() {
        mBus.post(new CelebrationEvent(CelebrationEvent.ACTION_START));
        isActive = true;
    }

    @Override
    public void onPlaybackTick(float progress) {
        mBus.post(new CelebrationEvent(CelebrationEvent.ACTION_TICK, progress));
    }

    @Override
    public void onFinishPlayback() {
        isActive = false;
        mBus.post(new CelebrationEvent(CelebrationEvent.ACTION_FINISH));
        mBus.post(new GameEvent(Action.RESUME));
    }

    public void delayResume(long duration) {
        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                mBus.post(new GameEvent(Action.RESUME));
            }
        }, duration);
    }

    public boolean isActive() {
        return isActive;
    }

    public class CelebrationEvent {
        public static final int ACTION_START = 1;
        public static final int ACTION_TICK = 2;
        public static final int ACTION_FINISH = 3;

        public int action;
        public float progress;

        public CelebrationEvent(int action) {
            this.action = action;
        }

        public CelebrationEvent(int action, float progress) {
            this.action = action;
            this.progress = progress;
        }
    }
}
