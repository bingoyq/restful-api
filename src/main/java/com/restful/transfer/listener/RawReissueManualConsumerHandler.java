package com.restful.transfer.listener;

import com.gdcp.common.rabbitmq.handler.ManualConsumerHandler;
import com.gdcp.common.rabbitmq.serialize.HessionCodecFactory;
import com.gdcp.common.rabbitmq.serialize.RawPacker;
import com.gdcp.svc.rpc.msgpck.MsgPackage;
import com.restful.transfer.TransferSourceAgent;
import com.restful.transfer.TransferSourceMgr;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;

import java.io.IOException;

/**
 * Created by yuanqiang on 2017/12/26.
 */
public class RawReissueManualConsumerHandler extends ManualConsumerHandler{

    private static Logger logger = LoggerFactory.getLogger(RawReissueManualConsumerHandler.class);

    @Override
    protected boolean handlerMessage(String s, String s1, MsgPackage msgPackage) {
        return false;
    }

    @Override
    protected boolean handlerMessage(Message message) {
        TransferSourceAgent agent = null;
        try {

            RawPacker packer = (RawPacker) HessionCodecFactory.getInstance().deSerialize(message.getBody());

            logger.debug("获取到监听数据：" + packer.getDevcode() + packer.getData().length);

            agent = getAgent(packer.getDevcode());

            agent.sendMsg(packer);

            Thread.sleep(300);

        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if(agent != null)
                TransferSourceMgr.getInstance().releaseSource(agent);
        }

        return true;
    }

    private TransferSourceAgent getAgent(String devcode) throws Exception {

        TransferSourceAgent agent = TransferSourceMgr.getInstance().getSource(devcode);

        if(agent != null) {
            return agent;
        }
        else {
            Thread.sleep(10000); // 休眠 10 秒
            return getAgent(devcode);
        }

    }
}
