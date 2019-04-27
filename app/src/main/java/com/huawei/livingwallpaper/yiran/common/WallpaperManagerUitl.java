package com.huawei.livingwallpaper.yiran.common;

import android.app.Activity;
import android.app.WallpaperManager;
import android.content.ComponentName;
import android.content.Intent;

public class WallpaperManagerUitl {

    public static void closeWallpaper(Activity activity) {
        Intent intent = new Intent(WallpaperManager.ACTION_LIVE_WALLPAPER_CHOOSER);
        activity.startActivity(intent);
    }

    public static void sendWallpaperForResult(Activity activity, int code) {
            Intent intent = new Intent(WallpaperManager.ACTION_CHANGE_LIVE_WALLPAPER);
            intent.putExtra(WallpaperManager.EXTRA_LIVE_WALLPAPER_COMPONENT,
                    new ComponentName(activity, SimpleWallpaper.class));
            activity.startActivityForResult(intent, code);
    }


}