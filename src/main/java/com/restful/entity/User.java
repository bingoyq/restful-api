package com.restful.entity;

/**
 * Created by yuanqiang on 2017/11/27.
 */
public class User {

    public User(){

    }

    public User(int id ,String name) {
        this.id = id;
        this.name = name;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    private int id;
    private String name;

}
