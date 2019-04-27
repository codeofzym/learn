package com.huawei.livingwallpaper.yiran.common;

import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class MainActivity extends AppCompatActivity {
    private static final int REQUEST_CODE_SET_WALLPAPER = 1;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        WallpaperManagerUitl.sendWallpaperForResult(this, REQUEST_CODE_SET_WALLPAPER);
        finish();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == REQUEST_CODE_SET_WALLPAPER && requestCode == Activity.RESULT_CANCELED) {
            WallpaperManagerUitl.closeWallpaper(this);
        }
    }

    public void toSettingWallpaper(Intent intent) {
        if (Build.VERSION.SDK_INT > 15) {
            WallpaperManagerUitl.sendWallpaperForResult(this, REQUEST_CODE_SET_WALLPAPER);
        } else {
            WallpaperManagerUitl.closeWallpaper(this);
        }
    }
}
