package com.restful.transfer.listener;

import com.gdcp.common.rabbitmq.Producer;
import com.gdcp.common.rabbitmq.handler.ManualConsumerHandler;
import com.gdcp.common.rabbitmq.serialize.HessionCodecFactory;
import com.gdcp.common.rabbitmq.serialize.RawPacker;
import com.gdcp.common.rabbitmq.util.RabbitMQAdminUtil;
import com.gdcp.common.rabbitmq.util.RabbitMQConnectionFactoryUtil;
import com.gdcp.data.VDPConst;
import com.gdcp.svc.rpc.msgpck.MsgPackage;
import com.restful.transfer.TransferSourceAgent;
import com.restful.transfer.TransferSourceMgr;
import org.springframework.amqp.core.Message;

/**
 * Created by yuanqiang on 2017/12/26.
 */
public class RawManualConsumerHandler extends ManualConsumerHandler{


    @Override
    protected boolean handlerMessage(String s, String s1, MsgPackage msgPackage) {
        return false;
    }

    @Override
    protected boolean handlerMessage(Message message) {
        TransferSourceAgent agent = null;
        try {
            RawPacker packer = (RawPacker) HessionCodecFactory.getInstance().deSerialize(message.getBody());

            agent = TransferSourceMgr.getInstance().getSource(packer.getDevcode());

            if(agent == null) {
                // 获取平台连接，如果获取不到则放入补发队列

                byte [] bytes = packer.getData();
                bytes[2] = 0x03;
                byte [] nb = new byte[bytes.length -3];
                System.arraycopy(bytes , 2 ,nb ,0 , nb.length);
                bytes[bytes.length -1] = makeCheckSum(nb);

                packer.setData(bytes); // 将实时包转换为补发包，重新计数校验码

                Object msg = HessionCodecFactory.getInstance().serialize(packer);

                RabbitMQConnectionFactoryUtil factoryUtil = new RabbitMQConnectionFactoryUtil("183.63.187.149" ,5672 ,"admin" , "admin");
//                RabbitMQConnectionFactoryUtil factoryUtil = RabbitMQConnectionFactoryUtil.getInstance();
//                RabbitMQConnectionFactoryUtil factoryUtil = new RabbitMQConnectionFactoryUtil("127.0.0.1" ,5672 ,"admin" , "admin");

                RabbitMQAdminUtil adminUtil = new RabbitMQAdminUtil(factoryUtil.getConnectionFactory());
                Producer producer = new Producer(adminUtil.getRabbitTemplate());
                producer.send(VDPConst.DATA_RAW.concat(".REISSUE") , null , msg);

            } else {
                agent.sendMsg(packer);            // 分发

            }

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        } finally {
            if(agent != null)
                TransferSourceMgr.getInstance().releaseSource(agent);
        }

        return true;
    }

    private byte makeCheckSum(byte[] msg) {
        byte sum = 0;
        for (int i = 0; i < msg.length; i++) {
            sum ^= (byte) (msg[i] & 0xff);
        }
        return sum;
    }
}
