package com.example.picture_locator;


import android.os.Build;
import android.support.annotation.RequiresApi;

import com.google.android.gms.maps.model.LatLng;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import androidx.exifinterface.media.ExifInterface;

public class GeneralUtils {

    @RequiresApi(api = Build.VERSION_CODES.N)
    public static LatLng getLatLngFromUri(String imagePath) {
        ExifInterface exif = null;
        try {
            exif = new ExifInterface(imagePath);
        } catch (IOException e) {
            e.printStackTrace();
        }
        double arr[] = new double[0];
        if (exif != null) {
            arr = exif.getLatLong();
        }
        assert arr != null;
        return new LatLng(arr[0], arr[1]);
    }
}
