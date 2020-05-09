package com.salam.jambi.listeners;

public interface MediaRecorderListener {
    void onRecordingStart();
    void onRecordingStop();
    void onRecordingError();
}
