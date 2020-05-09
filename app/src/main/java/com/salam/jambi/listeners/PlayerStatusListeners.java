package com.salam.jambi.listeners;

public interface PlayerStatusListeners {
    void onMyPlayerStartPlaying();
    void onMyPlayerPause();
    void onMyPlayerStop();
    void onMyPlayerError();
}
