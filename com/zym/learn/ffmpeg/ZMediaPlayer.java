package com.zym.learn.ffmpeg;

import android.graphics.Bitmap;
import android.util.Log;
import android.view.Surface;

public class ZMediaPlayer {
    private static final String TAG  = "ZMediaPlayer";

    Bitmap mBitmap;

    static {
        System.loadLibrary("native-lib");
    }

    public void setDataSource(String path) {
        if(path == null) {
            return;
        }
        setDataSourceNative(path);
    }

    public void setSurface(Surface surface) {
        if(surface == null) {
            return;
        }
        setSurfaceNative(surface);
    }

    public void start() {
        startNative();
    }

    public void setMarkBitmap(Bitmap bitmap, int left, int top) {
        if(bitmap == null) {
            return;
        }
        mBitmap = bitmap;
        Log.i(TAG, "setMarkBitmap:" + Integer.toHexString(System.identityHashCode(bitmap)));
        setMarkBitmapNative(left, top, bitmap);
    }

    /**
     * A native method that is implemented by the 'native-lib' native library,
     * which is packaged with this application.
     */
    public native String getTestString();
    private native void setDataSourceNative(String path);
    private native void setSurfaceNative(Surface surface);
    private native void startNative();
    private native void setMarkBitmapNative(int left, int top, Bitmap bitmap);

}
