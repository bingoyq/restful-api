package com.restful.entity;

import com.alibaba.fastjson.JSONObject;
import io.swagger.annotations.ApiModel;

/**
 * Created by yuanqiang on 2017/12/13.
 */
@ApiModel(description = "调用结果")
public class DataObject {

    private JSONObject data ;

    public DataObject(){
        data = new JSONObject();
    }

    public DataObject offer(String key ,String value) {
        data.put(key ,value);
        return this;
    }

    public DataObject offer(String key ,Object value) {
        data.put(key ,value);
        return this;
    }

    public JSONObject getData() {
        return data;
    }

    public DataObject setData(JSONObject data) {
        this.data = data;
        return this;
    }
}
