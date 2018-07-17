package io.lundie.michael.freshpots.utilities;

import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.util.Log;

import java.io.ByteArrayOutputStream;

/**
 * A simple Async task version of {@link DbBitmapUtility} getBytes method.
 * Allows this method to run on a background thread where appropriate.
 */
public class EncodeByteArrayAsyncTask extends AsyncTask<Bitmap, Void, byte[]> {

    private static final String LOG_TAG = EncodeByteArrayAsyncTask.class.getSimpleName();

    private Listener listener;
    public EncodeByteArrayAsyncTask(final Listener listener) {this.listener = listener; }

    public interface Listener {
        void onByteArrayRetrieved(final byte[] byteArray);
        void onByteArrayRetrievalError();
    }

    @Override
    protected byte[] doInBackground(Bitmap... bitmaps) {
        final Bitmap bitmap = bitmaps[0];
        Log.i(LOG_TAG, "TEST: Bitmap size:" + bitmap.getByteCount());
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
    @Override
    protected void onPostExecute(byte[] byteArray) {
        if (null != byteArray) {
            listener.onByteArrayRetrieved(byteArray);
        } else {
            listener.onByteArrayRetrievalError();
        }
    }
}