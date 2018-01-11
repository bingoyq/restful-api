package com.restful.transfer.cach;

import com.restful.entity.MsgPkg;

import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by yuanqiang on 2017/12/19.
 */
public class GBLoginStatus {

    private ConcurrentHashMap<String ,MsgPkg> cach;

    private static GBLoginStatus instance;

    private GBLoginStatus(){
        cach = new ConcurrentHashMap<>();
    }

    public static GBLoginStatus getInstance(){
        if(instance == null)
            instance = new GBLoginStatus();
        return instance;
    }

    public void offer(String key , MsgPkg pkg) {
        cach.put(key , pkg);
    }

    public MsgPkg poll(String key){
        return cach.get(key);
    }

    public void remove(String key) {
        MsgPkg pkg = cach.get(key);
        if(pkg != null )
            cach.remove(key);
    }

}
