package com.restful.transfer.send;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by yuanqiang on 2017/12/18.
 */
public class SenderAgent {

    private static AtomicInteger i = new AtomicInteger(0);
    private static Logger logger = LoggerFactory.getLogger(SenderAgent.class);

    /**
     * 登录国家平台
     * @param param
     */
    public static int login(Channel channel , String ...param) { //String uniqueCode ,String uname ,String psword)

        if(param == null || param.length <3)
            throw new IllegalArgumentException("登录国家平台参数异常...");

        if(i.get() > 65531)
            i.getAndSet(0);

        String uniqueCode = param[0];
        String uname = param[1];
        String psword = param[2];

        try {
            ByteBuf buffer = Unpooled.buffer();
            buffer.writeByte(0x23).writeByte(0x23).writeByte(0x05).writeByte(0xFE);
            if (uniqueCode.length() != 17) {
                throw new IllegalArgumentException("uniqueCode length error:" + uniqueCode);
            }
            buffer.writeBytes(uniqueCode.getBytes());
            buffer.writeByte(0x01);// 数据单元加密方式
            buffer.writeShort(41);// 数据单元长度
            buffer.writeBytes(Util.makeGBDate(new Date()));// 平台登入时间
            buffer.writeShort(i.addAndGet(1));// 登入流水号
            int loginNameLength = uname.length();
            if (loginNameLength > 12) {
                throw new IllegalArgumentException("loginName length error:" + uname);
            }
            buffer.writeBytes(uname.getBytes()).writeZero(12 - loginNameLength);// 写入平台登入用户名，后面补\0到12位
            int passWordLength = psword.length();
            if (passWordLength > 20) {
                throw new IllegalArgumentException("passWordLength length error:" + passWordLength);
            }
            buffer.writeBytes(psword.getBytes()).writeZero(20 - passWordLength);// 写入平台登入密码，后面补\0到20位

            buffer.writeByte(0x01);// 加密规则
            buffer.writeByte(0b0);// 预占用校验位
            byte[] bs = new byte[buffer.readableBytes()];
            buffer.readBytes(bs);
            byte validateByte = Util.renderValidateByte(bs);
            bs[bs.length - 1] = validateByte;// 校验码置入

            ByteBuf sendbuf = Unpooled.buffer(bs.length);
            sendbuf.writeBytes(bs);
            channel.writeAndFlush(sendbuf);

            logger.info("login - 当前通道ID：" + channel.id().toString());

        } catch (Exception e) {
            e.printStackTrace();
        }

        return i.get();

    }

    /**
     * 登出国家平台
     * @param uniqueCode
     * @param resp
     */
    public static void loginout(Channel channel, String uniqueCode , int resp){
        try {
            ByteBuf buffer = Unpooled.buffer();
            buffer.writeByte(0x23).writeByte(0x23).writeByte(0x06).writeByte(0xFE);
            if (uniqueCode.length() != 17) {
                throw new IllegalArgumentException("uniqueCode length error:" + uniqueCode);
            }
            buffer.writeBytes(uniqueCode.getBytes());
            buffer.writeByte(0x01);// 数据单元加密方式
            buffer.writeShort(8);// 数据单元长度
            buffer.writeBytes(Util.makeGBDate(new Date()));// 平台登入时间
            buffer.writeShort(resp);// 登入流水号
//            buffer.writeByte(0x01);// 加密规则
            buffer.writeByte(0b0);// 预占用校验位
            byte[] bs = new byte[buffer.readableBytes()];
            buffer.readBytes(bs);
            byte validateByte = Util.renderValidateByte(bs);
            bs[bs.length - 1] = validateByte;// 校验码置入

            ByteBuf sendbuf = Unpooled.buffer(bs.length);
            sendbuf.writeBytes(bs);
            channel.writeAndFlush(sendbuf);

            logger.info("loginout - 当前通道ID：" + channel.id().toString());

//            channel.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    static class Util{
        private static byte[] makeGBDate(Date date) {
            byte[] time = new byte[6];
            Calendar cal = Calendar.getInstance();
            cal.setTime(date);
//            cal.add(Calendar.HOUR_OF_DAY, -8);
            int year = cal.get(Calendar.YEAR) - 2000;
            int month = cal.get(Calendar.MONTH) + 1;
            int day = cal.get(Calendar.DATE);
            int hour = cal.get(Calendar.HOUR_OF_DAY);
            int min = cal.get(Calendar.MINUTE);
            int sec = cal.get(Calendar.SECOND);
            time[0] = (byte) year;
            time[1] = (byte) month;
            time[2] = (byte) day;
            time[3] = (byte) hour;
            time[4] = (byte) min;
            time[5] = (byte) sec;
            return time;
        }

        private static byte renderValidateByte(byte[] data) {
            int length = data.length;

            byte tmp = data[2];
            for (int i = 3; i < length-1; i++) {
                tmp ^= data[i];
            }

            return tmp;
        }
    }


}

