package com.restful.entity;

import io.netty.channel.Channel;

import java.util.Date;

/**
 * Created by yuanqiang on 2017/12/25.
 */
public class ChannelCache {

    private String key ; // 连接通道key
    private Channel channel; // 连接通道
    private String channelID; // 连接通道ID
    private Date loginDate; // 通道登录时间
    private int serialNumber; // 登录流水号

    public ChannelCache(String key ,Channel channel,Date login_date) {
        this.key = key;
        this.channel = channel ;
        this.loginDate = login_date;
        this.channelID = channel.id().toString();
    }

    public Channel getChannel() {
        return channel;
    }

    public void setChannel(Channel channel) {
        this.channel = channel;
    }

    public Date getLoginDate() {
        return loginDate;
    }

    public void setLoginDate(Date loginDate) {
        this.loginDate = loginDate;
    }

    public String getChannelID() {
        return channelID;
    }

    public void setChannelID(String channelID) {
        this.channelID = channelID;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public int getSerialNumber() {
        return serialNumber;
    }

    public void setSerialNumber(int serialNumber) {
        this.serialNumber = serialNumber;
    }
}
