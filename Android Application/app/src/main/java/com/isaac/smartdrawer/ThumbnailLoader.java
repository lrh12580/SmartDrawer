package com.isaac.smartdrawer;

import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;

import java.io.File;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Created by Isaac on 2016/9/1 0001.
 */
public class ThumbnailLoader<T> extends HandlerThread{
    private static final String TAG = "ThumbnailLoader";
    private static final int MESSAGE_DOWNLOAD = 0;

    private boolean mHasQuit = false;
    private Handler mRequestHandler;
    private ConcurrentMap<T, String> mRequestMap = new ConcurrentHashMap<>();
    private Handler mResponseHandler;

    private ThumbnailLoadListener<T> mThumbnailLoadListener;

    public interface ThumbnailLoadListener<T> {
        void onThumbnailLoaded(T target, Bitmap thumbnail);
    }

    public ThumbnailLoader(Handler responseHandler) {
        super(TAG);
        mResponseHandler = responseHandler;
    }

    public void setThumbnailLoadListener(ThumbnailLoadListener<T> listener) {
        mThumbnailLoadListener = listener;
    }

    public void queueThumbnail(T target, String url) {
        if (url == null) {
            mRequestMap.remove(target);
        } else {
            mRequestMap.put(target, url);
            mRequestHandler.obtainMessage(MESSAGE_DOWNLOAD, target).sendToTarget();
        }
    }

    @Override
    protected void onLooperPrepared() {
        mRequestHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                if (msg.what == MESSAGE_DOWNLOAD) {
                    T target = (T) msg.obj;
                    handleRequest(target);
                }
            }
        };
    }

    private void handleRequest(final T target) {
        final String url = mRequestMap.get(target);

        if (url == null) return;

        final Bitmap bitmap;
        bitmap = new File(url).exists()?ImageHandler.getImage(url):null;

        mResponseHandler.post(new Runnable() {
            @Override
            public void run() {
                if (mRequestMap.get(target) != url || mHasQuit) return;

                mRequestMap.remove(target);
                mThumbnailLoadListener.onThumbnailLoaded(target, bitmap);
            }
        });

    }

    public void clearQueue() {
        mRequestHandler.removeMessages(MESSAGE_DOWNLOAD);
    }

    @Override
    public boolean quit() {
        mHasQuit = true;
        return super.quit();
    }
}
