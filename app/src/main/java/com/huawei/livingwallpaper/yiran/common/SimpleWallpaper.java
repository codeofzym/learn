package com.huawei.livingwallpaper.yiran.common;

import android.content.res.AssetFileDescriptor;
import android.media.MediaPlayer;
import android.service.wallpaper.WallpaperService;
import android.view.MotionEvent;
import android.view.SurfaceHolder;

import java.io.IOException;

public class SimpleWallpaper extends WallpaperService {
    @Override
    public void onCreate() {
        super.onCreate();
        WLog.d(this, "onCreate");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        WLog.d(this, "onDestroy");
    }

    @Override
    public Engine onCreateEngine() {
        WLog.d(this, "onCreateEngine");
        return new VideoEngine();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        WLog.d(this, "onLowMemory");
    }

    @Override
    public void onTrimMemory(int level) {
        super.onTrimMemory(level);
        WLog.d(this, "onTrimMemory");
    }

    private class VideoEngine extends Engine implements MediaPlayer.OnPreparedListener, MediaPlayer.OnCompletionListener, MediaPlayer.OnErrorListener{


        private final MediaPlayer mMediaPlayer = new MediaPlayer();
        private boolean isReady = false;

        @Override
        public void onCreate(SurfaceHolder surfaceHolder) {
            super.onCreate(surfaceHolder);
            try {
                mMediaPlayer.setOnPreparedListener(this);
                mMediaPlayer.setOnCompletionListener(this);
                mMediaPlayer.setOnErrorListener(this);
                AssetFileDescriptor afd = getAssets().openFd("video.mp4");
                mMediaPlayer.setDataSource(afd.getFileDescriptor(), afd.getStartOffset(), afd.getLength());
                mMediaPlayer.setLooping(false);
                mMediaPlayer.prepareAsync();
            } catch (IOException e) {

            }
        }

        @Override
        public void onDestroy() {
            super.onDestroy();
        }


        @Override
        public void onVisibilityChanged(boolean visible) {
            super.onVisibilityChanged(visible);
            if(visible) {
                if(!mMediaPlayer.isPlaying() && !isReady) {
                    mMediaPlayer.start();
                }
            } else {
                if(mMediaPlayer.isPlaying() && !isReady) {
                    mMediaPlayer.pause();
                }
            }
        }

        @Override
        public void onTouchEvent(MotionEvent event) {
            super.onTouchEvent(event);
            WLog.i(this, "event:" + event.getAction());
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    start();
                    break;

                case MotionEvent.ACTION_UP:
                    break;

                default:
                    break;
            }
        }

        @Override
        public void onSurfaceChanged(SurfaceHolder holder, int format, int width, int height) {
            super.onSurfaceChanged(holder, format, width, height);
            mMediaPlayer.setSurface(holder.getSurface());
        }

        @Override
        public void onSurfaceRedrawNeeded(SurfaceHolder holder) {
            super.onSurfaceRedrawNeeded(holder);
            mMediaPlayer.setSurface(holder.getSurface());
        }

        @Override
        public void onSurfaceCreated(SurfaceHolder holder) {
            super.onSurfaceCreated(holder);
            mMediaPlayer.setSurface(holder.getSurface());
        }

        @Override
        public void onSurfaceDestroyed(SurfaceHolder holder) {
            super.onSurfaceDestroyed(holder);
            mMediaPlayer.setSurface(null);
        }

        @Override
        public void onCompletion(MediaPlayer mp) {
            isReady = true;
        }

        @Override
        public boolean onError(MediaPlayer mp, int what, int extra) {
            WLog.i(this, "onError what:" + what + " extra:"+ extra);
            return false;
        }

        @Override
        public void onPrepared(MediaPlayer mp) {
            WLog.i(this, "onPrepared");
            mMediaPlayer.seekTo(0);
            isReady = true;
        }

        private void start() {
            if(!isReady && mMediaPlayer.isPlaying()) return;
            isReady = false;
            mMediaPlayer.start();
        }
    }
}
