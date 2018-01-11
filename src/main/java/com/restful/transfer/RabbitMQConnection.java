package com.restful.transfer;

/**
 * Created by yuanqiang on 2018/1/3.
 */
public class RabbitMQConnection {

    private String host;
    private int port;
    private String username;
    private String password;

    public RabbitMQConnection(){

    }

    public void setHost(String host) {
        this.host = host;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
