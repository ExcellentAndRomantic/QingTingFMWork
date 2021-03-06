package com.lxqhmlwyh.qingtingfm.entities;

public class PlayingList {
    //private int channelId;
    private String programName;
    private String startTime;
    private String endTime;
    private String broadcasters;
    private String playUrl;
    /*private String coverUrl;
    private String channel;*/

    /*public int getChannelId() {
        return channelId;
    }

    public void setChannelId(int channelId) {
        this.channelId = channelId;
    }*/

    /*public void setChannel(String channel) {
        this.channel = channel;
    }

    public String getChannel() {
        return channel;
    }

    public void setCoverUrl(String coverUrl) {
        this.coverUrl = coverUrl;
    }

    public String getCoverUrl() {
        return coverUrl;
    }*/

    public String getProgramName() {
        return programName;
    }

    public void setProgramName(String programName) {
        this.programName = programName;
    }

    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public String getEndTime() {
        return endTime;
    }

    public void setEndTime(String endTime) {
        this.endTime = endTime;
    }

    public String getBroadcasters() {
        return broadcasters;
    }

    public void setBroadcasters(String broadcasters) {
        this.broadcasters = broadcasters;
    }

    public String getPlayUrl() {
        return playUrl;
    }

    public void setPlayUrl(String playUrl) {
        this.playUrl = playUrl;
    }

    @Override
    public String toString() {
        return "PlayingList{" +
                "programName='" + programName + '\'' +
                ", startTime='" + startTime + '\'' +
                ", endTime='" + endTime + '\'' +
                ", broadcasters='" + broadcasters + '\'' +
                ", playUrl='" + playUrl + '\'' +
                '}';
    }
}
