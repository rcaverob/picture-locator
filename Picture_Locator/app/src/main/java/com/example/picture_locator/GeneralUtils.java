package com.example.picture_locator;

import androidx.exifinterface.media.ExifInterface;
import android.os.Build;
import android.support.annotation.RequiresApi;

import com.google.android.gms.maps.model.LatLng;
import java.io.FileInputStream;
import java.io.IOException;

public class GeneralUtils {

    @RequiresApi(api = Build.VERSION_CODES.N)
    public static LatLng getLatLngFromUri(FileInputStream image) throws IOException {
        float[] arr = new float[2];
        ExifInterface exif = new ExifInterface(image);
        exif.getLatLong(arr);
        return new LatLng(arr[0], arr[1]);
    }
}
