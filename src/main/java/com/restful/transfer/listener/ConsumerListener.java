package com.restful.transfer.listener;

import com.gdcp.common.rabbitmq.Consumer;
import com.gdcp.common.rabbitmq.exchange.ExchangeEnum;
import com.gdcp.common.rabbitmq.exchange.MessageExchange;
import com.gdcp.common.rabbitmq.util.RabbitMQAdminUtil;
import com.gdcp.common.rabbitmq.util.RabbitMQConnectionFactoryUtil;
import com.gdcp.data.VDPConst;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by yuanqiang on 2018/1/2.
 */
public class ConsumerListener {

    private static Logger logger = LoggerFactory.getLogger(ConsumerListener.class);

    private String host;
    private int port;
    private String username;
    private String password;

    private void init(){
        logger.info("启动 rabbitmq 队列监听...");

        RabbitMQConnectionFactoryUtil factoryUtil = new RabbitMQConnectionFactoryUtil(host,port ,username , password);
//        RabbitMQConnectionFactoryUtil factoryUtil = RabbitMQConnectionFactoryUtil.getInstance();

        RabbitMQAdminUtil adminUtil = new RabbitMQAdminUtil(factoryUtil.getConnectionFactory());
        MessageExchange mex = new MessageExchange(adminUtil.getRabbitAdmin());
        mex.binding(ExchangeEnum.FANOUT , VDPConst.DATA_RAW, null , "TRANSFER.RAW.DATA.01"); // 实时原始数据

        mex.create(ExchangeEnum.FANOUT , VDPConst.DATA_RAW.concat(".REISSUE")); // 补发队列
        mex.binding(ExchangeEnum.FANOUT , VDPConst.DATA_RAW.concat(".REISSUE"), null , "TRANSFER.RAW.DATA.REISSUE"); // 补发原始数据

        Consumer consumer = new Consumer(factoryUtil.getConnectionFactory() ,adminUtil.getRabbitAdmin() , 1 , "TRANSFER.RAW.DATA.01");
        consumer.setManualConsumerHandler(new RawManualConsumerHandler());
        consumer.run();

        Consumer consumer2 = new Consumer(factoryUtil.getConnectionFactory() ,adminUtil.getRabbitAdmin() , 1 , "TRANSFER.RAW.DATA.REISSUE");
        consumer2.setManualConsumerHandler(new RawReissueManualConsumerHandler());
        consumer2.run();
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
