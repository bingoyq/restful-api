package com.restful.controller;

import com.alibaba.fastjson.JSONArray;
import com.restful.entity.ChannelCache;
import com.restful.entity.DataObject;
import com.restful.transfer.TransferSourceAgent;
import com.restful.transfer.TransferSourceMgr;
import com.restful.transfer.cach.SourceAgentCach;
import com.restful.transfer.send.SenderAgent;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * Created by yuanqiang on 2017/12/22.
 */
@Api(value="国家平台登录API",tags={""})
@RestController
@RequestMapping(value="/transfer")
public class TransferControlle {

    @ApiOperation(value="国家平台，登录通道列表", notes="")
    @RequestMapping(value={"/channel-list"}, method= RequestMethod.GET)
    public DataObject channelList() {
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        DataObject object = new DataObject();
        object.offer("code" , "200");

        List<ChannelCache> caches =  SourceAgentCach.getInstance().pollAll();

        if(caches.size() <=0)
            object.offer("msg" , "还未建立连接！");

        JSONArray array = new JSONArray();
//
//        for (int i = 0; i < caches.size(); i++) {
//            array.add(i , new DataObject().offer("key" , caches.get(i).getKey()).offer("channelID" , caches.get(i).getChannelID()).offer("time" ,df.format(caches.get(i).getLoginDate()))));
//        }

//        caches.forEach((c) -> object.offer("data" , new DataObject().offer("key" , c.getKey()).offer("channelID" , c.getChannelID()).offer("time" ,df.format(c.getLoginDate()))));

        caches.forEach((c) -> array.add(new DataObject().offer("key" , c.getKey()).offer("channelID" , c.getChannelID()).offer("time" ,df.format(c.getLoginDate()))));

        object.offer("data" , array);

        object.offer("系统时间" , new Date());

        return object;
    }

    @ApiOperation(value="国家平台，初次建立连接，并且登录一次", notes="")
    @ApiImplicitParams({
            @ApiImplicitParam(paramType = "query", name = "url", value = "国家平台url:0.0.0.1:8080", dataType = "String"),
            @ApiImplicitParam(paramType = "query", name = "maxAgentNum", value = "连接池数量", dataType = "Long"),
            @ApiImplicitParam(paramType = "query", name = "transferType", value = "国家平台类型", dataType = "String"),
            @ApiImplicitParam(paramType = "query", name = "uniqueCode", value = "国家平台，唯一标识码", dataType = "String"),
            @ApiImplicitParam(paramType = "query", name = "uname", value = "国家平台登录用户名", dataType = "String"),
            @ApiImplicitParam(paramType = "query", name = "psword", value = "国家平台登录密码", dataType = "String")
    })
    @RequestMapping(value={"/conntion"}, method= RequestMethod.GET)
    public DataObject conntion(@RequestParam String url , @RequestParam Long maxAgentNum , @RequestParam String transferType , @RequestParam String uniqueCode ,
                            @RequestParam String uname , @RequestParam String psword ){

        DataObject object = new DataObject();
        object.offer("code" , "200");

        String key = uniqueCode.concat(":").concat(uname).concat(":").concat(psword);

        ChannelCache cache = SourceAgentCach.getInstance().poll(key); // 通道缓存

        // 当前登录信息存在缓存，先退出。再登录！
        if(cache != null && cache.getChannel() != null && cache.getChannel().isActive()) {
            object.offer("msg" , key.concat(" : 已存在，不能重复登录！"));
            return object;
        }

        new Thread(()->{
            try {
                TransferSourceMgr.getInstance().initCasheParameter(maxAgentNum.intValue(),url,transferType,uniqueCode,uname,psword);
                TransferSourceAgent agent = TransferSourceMgr.getInstance().initSource();
                TransferSourceMgr.getInstance().releaseSource(agent);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();

        object.offer("msg" , "登录成功！");

        object.offer("系统时间" , new Date());

        return object;

    }

    @ApiOperation(value="国家平台，发送登录包", notes="")
    @ApiImplicitParams({
            @ApiImplicitParam(paramType = "query", name = "transferType", value = "国家平台类型", dataType = "String"),
            @ApiImplicitParam(paramType = "query", name = "key", value = "国家平台，唯一标识码", dataType = "String")
    })
    @RequestMapping(value={"/login"}, method= RequestMethod.GET)
    public DataObject login(@RequestParam String transferType , @RequestParam String key ) {
        DataObject object = new DataObject();
        object.offer("code" , "200");

        ChannelCache channelCache = SourceAgentCach.getInstance().poll(key);

        try {

            if(channelCache != null && channelCache.getChannel().isActive()) {
//                String uniqueCode = key.split(":")[0];
//                SenderAgent.loginout(channelCache.getChannel() , uniqueCode ,channelCache.getSerialNumber());
//                SourceAgentCach.getInstance().remove(key); // 登出后清理缓存
                SenderAgent.login(channelCache.getChannel() , new String []{key.split(":")[0] ,key.split(":")[1] , key.split(":")[2]});
            }
            else
                object.offer("msg" , key.concat(" - 当前用户未建立连接，处理成功！"));

            object.offer("系统时间" , new Date());

        } catch (Exception e) {
            e.printStackTrace();
            object.offer("error" , "登出平台异常，参数不正确！");

        }

        return object;
    }

    @ApiOperation(value="国家平台，发送登出包", notes="")
    @ApiImplicitParams({
            @ApiImplicitParam(paramType = "query", name = "transferType", value = "国家平台类型", dataType = "String"),
            @ApiImplicitParam(paramType = "query", name = "key", value = "国家平台，唯一标识码", dataType = "String")
    })
    @RequestMapping(value={"/loginout"}, method= RequestMethod.GET)
    public DataObject loginout(@RequestParam String transferType , @RequestParam String key ) {
        DataObject object = new DataObject();
        object.offer("code" , "200");

        ChannelCache channelCache = SourceAgentCach.getInstance().poll(key);

        try {

            if(channelCache != null && channelCache.getChannel().isActive()) {
                String uniqueCode = key.split(":")[0];
                SenderAgent.loginout(channelCache.getChannel() , uniqueCode ,channelCache.getSerialNumber());
//                SourceAgentCach.getInstance().remove(key); // 登出后清理缓存
            }
            else
                object.offer("msg" , key.concat(" - 当前用户未建立连接，处理成功！"));

            object.offer("系统时间" , new Date());

        } catch (Exception e) {
            e.printStackTrace();
            object.offer("error" , "登出平台异常，参数不正确！");

        }

        return object;
    }

    @ApiOperation(value="国家平台，车机编码绑定通道", notes="")
    @ApiImplicitParams({
            @ApiImplicitParam(paramType = "query", name = "devcode", value = "车机编码", dataType = "String"),
            @ApiImplicitParam(paramType = "query", name = "transferType", value = "通道key", dataType = "String"),
    })
    @RequestMapping(value={"/banding"}, method= RequestMethod.GET)
    public DataObject banding(@RequestParam String devcode, @RequestParam String transferType ) {
        DataObject object = new DataObject();
        object.offer("code" , "200");

        boolean f = TransferSourceMgr.getInstance().banding(devcode ,transferType);
        if(f)
            object.offer("msg" , devcode.concat(" - 绑定 ").concat(transferType).concat(" 成功！"));
        else
            object.offer("msg" , devcode.concat(" - 绑定 ").concat(transferType).concat(" 失败！"));

        object.offer("系统时间" , new Date());

        return object;
    }

    @ApiOperation(value="国家平台，设置平台在线，断开状态", notes="")
    @ApiImplicitParams({
            @ApiImplicitParam(paramType = "query", name = "status", value = "状态类型：off断开／on连接", dataType = "String"),
    })
    @RequestMapping(value={"/online"}, method= RequestMethod.GET)
    public DataObject online(@RequestParam String status ) {
        DataObject object = new DataObject();
        object.offer("code" , "200");

        if("off".equals(status)) {
            TransferSourceMgr.getInstance().setOnline(false);
            object.offer("msg" , "设置平台断开成功！");
        } else {
            TransferSourceMgr.getInstance().setOnline(true);
            object.offer("msg" , "设置平台连接成功！");
        }

        object.offer("系统时间" , new Date());

        return object;
    }

    @ApiOperation(value="国家平台，设置发生数据模式", notes="")
    @ApiImplicitParams({
            @ApiImplicitParam(paramType = "query", name = "pattern", value = "模式：1／单通道，2／单车多通道，3／多车多通道", dataType = "Long"),
    })
    @RequestMapping(value={"/pattern"}, method= RequestMethod.GET)
    public DataObject pattern(@RequestParam Long pattern ) {
        DataObject object = new DataObject();
        object.offer("code" , "200");

        TransferSourceMgr.getInstance().setPattern(pattern.byteValue());
        object.offer("msg" , "设置平台连接成功！");

        object.offer("系统时间" , new Date());

        return object;
    }
}
