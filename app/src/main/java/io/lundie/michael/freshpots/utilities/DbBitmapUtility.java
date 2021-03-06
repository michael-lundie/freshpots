package io.lundie.michael.freshpots.utilities;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import java.io.ByteArrayOutputStream;

// This class is taken from: https://stackoverflow.com/a/32163951

public class DbBitmapUtility {
    
    private static final String LOG_TAG = DbBitmapUtility.class.getName();

    // convert from bitmap to byte array
    public static byte[] getBytes(Bitmap bitmap) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 0, stream);
        try {
            stream.close();
        } catch (Exception e) {
            Log.e(LOG_TAG,"Stream output error:", e);
        }
        byte compressedArray[] = stream.toByteArray();
        Log.i(LOG_TAG, "TEST: array length: " + compressedArray.length);
        return compressedArray;
    }

    // convert from byte array to bitmap
    public static Bitmap getImage(byte[] image) {
        return BitmapFactory.decodeByteArray(image, 0, image.length);
    }
}