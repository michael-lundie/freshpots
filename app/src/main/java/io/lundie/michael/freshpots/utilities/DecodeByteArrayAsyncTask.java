package io.lundie.michael.freshpots.utilities;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;

public class DecodeByteArrayAsyncTask extends AsyncTask<byte[], Void, Bitmap> {

    private static final String LOG_TAG = DecodeByteArrayAsyncTask.class.getSimpleName();

    private Listener listener;
    public DecodeByteArrayAsyncTask(final Listener listener) {this.listener = listener; }

    public interface Listener {
        void onImageRetrieved(final Bitmap bitmap);
        void onImageRetrievalError();
    }

    @Override
    protected Bitmap doInBackground(byte[]... byteStreams) {
        final byte[] byteSteam = byteStreams[0];
        return BitmapFactory.decodeByteArray(byteSteam, 0, byteSteam.length);
    }
    @Override
    protected void onPostExecute(Bitmap decodedBitmap) {
        if (null != decodedBitmap) {
            listener.onImageRetrieved(decodedBitmap);
        } else {
            listener.onImageRetrievalError();
        }
    }
}
