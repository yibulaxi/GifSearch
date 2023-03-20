package com.funny.app.gif.memes.function.download;

public interface DownloadCallback {
    void onStart();

    void onConnected(long totalLength);

    void onProgress(long current, long totalLength);

    void onPause(TaskInfo taskInfo);

    void onCompleted(TaskInfo taskInfo);

    void onError(Exception e);
}
