package com.salam.jambi.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class RadioChannelData {
    @SerializedName("radio_channel")
    @Expose
    private List<RadioChannel> radioChannel = null;

    public List<RadioChannel> getRadioChannel() {
        return radioChannel;
    }

    public void setRadioChannel(List<RadioChannel> radioChannel) {
        this.radioChannel = radioChannel;
    }
}
