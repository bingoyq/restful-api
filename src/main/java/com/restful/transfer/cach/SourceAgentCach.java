package com.restful.transfer.cach;

import com.restful.entity.ChannelCache;
import com.restful.transfer.send.SenderAgent;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by yuanqiang on 2017/12/20.
 */
public class SourceAgentCach {

    private ConcurrentHashMap<String , ChannelCache> currentMap = new ConcurrentHashMap<String, ChannelCache>();

    private static SourceAgentCach instance;

    private SourceAgentCach(){
        currentMap = new ConcurrentHashMap<String, ChannelCache>();
    }

    public static SourceAgentCach getInstance(){
        if(instance == null)
            instance = new SourceAgentCach();
        return instance;
    }

    /**
     * 存储／更新 缓存数据
     * @param key
     * @param cache
     */
    public void offer(String key , ChannelCache cache) {
        currentMap.put(key , cache);
    }

    /**
     * 根据KEY获取缓存数据
     * @param key
     * @return
     */
    public ChannelCache poll(String key){
        return currentMap.get(key);
    }

    /**
     * 删除缓存数据
     * @param key
     */
    public void remove(String key) {
        ChannelCache channel = currentMap.get(key);
        if(channel != null )
            currentMap.remove(key);
    }

    /**
     * 删除所有通道、用户登录信息
     */
    public void removeAll(){
        for(String key : currentMap.keySet()) {
            ChannelCache cache = currentMap.get(key);

            if(cache != null && cache.getChannel().isActive()) {
                SenderAgent.loginout(cache.getChannel(),key.split(":")[0],cache.getSerialNumber());
                //cache.getChannel().close();

//                currentMap.remove(key);
            }
        }
//        currentMap.entrySet().forEach((k) -> currentMap.remove(k));
    }

    /**
     * 获取所有连接通道
     * @return
     */
    public List<ChannelCache> pollAll(){
        List<ChannelCache> list = new ArrayList<ChannelCache>();
        currentMap.forEach((k ,v) -> list.add(v));
        return list;
    }


}
