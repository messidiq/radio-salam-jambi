package com.salam.jambi.listeners;

public interface PlayerListener {
    void onStartPlaying();
    void onPlayerPause();
    void onPlayerStop();
    void onPlayerError();
}
