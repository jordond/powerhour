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
package ca.hoogit.powerhour.Configure;

import android.net.Uri;

import ca.hoogit.powerhour.Audio.AudioPlayer;
import ca.hoogit.powerhour.R;
import mbanje.kurt.fabbutton.FabButton;

/**
 * @author jordon
 *         <p/>
 *         Date    27/07/15
 *         Description
 */
public class ProgressButton implements AudioPlayer.OnPlayback {

    private FabButton mButton;
    private int mImageId;
    private int mImageActiveId;
    private int mImageEndId;
    private boolean mIsActive;

    private AudioPlayer mAudioPlayer;

    public ProgressButton(FabButton mButton, AudioPlayer player) {
        this.mButton = mButton;
        this.mAudioPlayer = player;
        this.mImageId = R.drawable.ic_av_play_arrow;
        this.mImageActiveId = R.drawable.ic_av_stop;
        this.mAudioPlayer.setListener(this);
    }

    public ProgressButton(FabButton button, AudioPlayer player, int imageId, int imageActiveId) {
        this.mButton = button;
        this.mAudioPlayer = player;
        this.mImageId = imageId;
        this.mImageActiveId = imageActiveId;
        this.mAudioPlayer.setListener(this);
    }

    public void start(int soundId) {
        this.mAudioPlayer.play(soundId);
        start();
    }

    public void start(Uri sound) {
        this.mAudioPlayer.play(sound);
        start();
    }

    public void start() {
        if (mImageEndId != 0) {
            this.mButton.setIcon(mImageActiveId, mImageEndId);
        } else {
            this.mButton.setIcon(mImageActiveId, mImageId);
        }
        this.mButton.showProgress(true);
        this.mButton.setProgress(0);
        this.mIsActive = true;
    }

    @Override
    public void onPlaybackStart() {
    }

    @Override
    public void onPlaybackTick(float progress) {
        this.mButton.setProgress(progress);
    }

    @Override
    public void onFinishPlayback() {
        reset();
    }

    public void setProgress(float progress) {
        this.mButton.setProgress(progress);
    }

    public void reset() {
        if (this.mAudioPlayer.isPlaying()) {
            this.mAudioPlayer.stop();
        }
        this.mButton.setProgress(0);
        this.mButton.showProgress(false);
        this.mIsActive = false;
        if (mImageEndId != 0) {
            this.mButton.setIcon(mImageId, mImageEndId);
        } else {
            this.mButton.setIcon(mImageId, mImageId);
        }
    }

    public void setImageEndId(int imageEndId) {
        this.mImageEndId = imageEndId;
    }

    public boolean isActive() {
        return mIsActive;
    }

    public void setIsActive(boolean isActive) {
        this.mIsActive = isActive;
    }
}
