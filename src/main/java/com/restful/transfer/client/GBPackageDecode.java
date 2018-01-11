package com.restful.transfer.client;

import com.gdcp.common.pool.bytebuf.FixedByteBufPool;
import com.restful.entity.MsgPkg;
import io.netty.buffer.ByteBuf;

import java.util.Date;

/**
 * Created by yuanqiang on 2017/12/19.
 */
public class GBPackageDecode {

    /**
     * 国标协议解析包头
     * @param buf
     * @return
     * @throws Exception
     */
    public static MsgPkg unpack(ByteBuf buf) throws Exception{
        byte identifier = 0x23;
        //查找是否有0x23字节
        int firstIdentifier = buf.indexOf(buf.readerIndex(), buf.writerIndex(), identifier);
        if (firstIdentifier == -1) {
            return null;
        }
        //顺延读取下一个字节
        byte secondIdentifier = buf.getByte(firstIdentifier + 1);
        if (secondIdentifier != identifier) {
            //读指针下移一个
            buf.readerIndex(firstIdentifier + 1);
            buf.discardReadBytes();//丢弃字节
            return null;
        }
        buf.readerIndex(firstIdentifier);//将当前字节第一个位置作为下标
        buf.discardReadBytes();//丢弃前面的字节
        try {
            MsgPkg pkg = new MsgPkg();
            buf.readerIndex(22);
            int datalenth = buf.readShort();//数据单元长度
            int pckLength = 25 + datalenth;//总包长度
            int contextLength = 22 + datalenth;//业务数据包长度
            byte[] pck = new byte[pckLength];
            byte[] context = new byte[contextLength];
            buf.readerIndex(0);
            if (buf.readableBytes() < pckLength) {
                buf.readerIndex(0);
                return null;
            }
            buf.readBytes(pck);//读入到总包
            buf.readerIndex(2);
            buf.readBytes(context);//读入到业务数据包



            //校验最后一个字节码

            if (!validateCheckSum(context, pck[pck.length - 1])) {
                //校验码出错 继续生成对象
                pkg.setValidate(false);//校验码出错
//				buf.discardReadBytes();
//				return null;
            }

            pkg.setData(pck);
            byte[] trans = context;//特殊字节转义
            pkg.setTrans(trans);
            pkg.setRecvTime(new Date());

            //定义一个临时byte数组
            ByteBuf data = FixedByteBufPool.getInstance().borrowObject(trans.length);
            data.writeBytes(trans);//写入数据 (去掉0x23,0x23和最后一个校验byte)
            try {
                byte cmdId = data.readByte();//命令标识
                pkg.setCmdId(cmdId);
                byte respId = data.readByte();//应答标志
                pkg.setRespId(respId);
                byte[] vid = new byte[17];//车辆识别码 VIN
                data.readBytes(vid);
                for (int i = 0; i < vid.length; i++) {
                    if( vid[i] == 0b0 ){
                        vid[i] = 32;//把车机号中的\0替换为空格
                    }
                }
                String devcode = new String(vid);
                pkg.setDevcode(devcode);
                byte encryption = data.readByte();//加密方式
                pkg.setEncryption(encryption);
                data.readShort();
                byte[] databyte = new byte[datalenth];
                data.readBytes(databyte);
                pkg.setContext(databyte);
                return pkg;
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                buf.discardReadBytes();
                FixedByteBufPool.getInstance().returnObject(trans.length, data);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private static boolean validateCheckSum(byte[] msg, byte ssum) {
        byte csum = makeCheckSum(msg);
        if (csum == ssum) {
            return true;
        }
        return false;
    }

    private static byte makeCheckSum(byte[] msg) {
        byte sum = 0;
        for (int i = 0; i < msg.length; i++) {
            sum ^= (byte) (msg[i] & 0xff);
        }
        return sum;
    }
}
