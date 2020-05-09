package com.salam.jambi.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class RadioChannel {

    @SerializedName("channel_name")
    @Expose
    private String channelName;
    @SerializedName("channel_url")
    @Expose
    private String channelUrl;

    public String getChannelName() {
        return channelName;
    }

    public void setChannelName(String channelName) {
        this.channelName = channelName;
    }

    public String getChannelUrl() {
        return channelUrl;
    }

    public void setChannelUrl(String channelUrl) {
        this.channelUrl = channelUrl;
    }

}

