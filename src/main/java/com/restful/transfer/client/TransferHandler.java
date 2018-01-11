package com.restful.transfer.client;

import com.restful.entity.ChannelCache;
import com.restful.entity.MsgPkg;
import com.restful.transfer.TransferSourceAgent;
import com.restful.transfer.cach.GBLoginStatus;
import com.restful.transfer.cach.SourceAgentCach;
import com.restful.transfer.send.SenderAgent;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.Date;

/**
 * Created by yuanqiang on  2017/12/18.
 */
public class TransferHandler extends SimpleChannelInboundHandler<ByteBuf> {

    private static Logger logger = LoggerFactory.getLogger(TransferHandler.class);

    private String uniqueCode ; // 与国家平台连接唯一标识码
    private String username ; // 与国家平台连接用户
    private String password ; // 与国家平台连接密码

    private int serial_number = 0; // 登录流水号

    public TransferHandler(String ...param){
        this.uniqueCode = param[0];
        this.username = param[1];
        this.password = param[2];
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        super.channelActive(ctx);

        logger.info("通道ID：" + ctx.channel().id().toString());

        String key = uniqueCode.concat(":").concat(username).concat(":").concat(password);

        ChannelCache cache = SourceAgentCach.getInstance().poll(key); // 通道缓存

        // 当前登录信息存在缓存，先退出。再登录！
        if(cache != null && cache.getChannel() != null && cache.getChannel().isActive()) {
            MsgPkg msg = GBLoginStatus.getInstance().poll(key); // 登录包缓存
            SenderAgent.loginout(cache.getChannel() , uniqueCode , serial_number);
            cache.getChannel().close();
        }

        SenderAgent.login(ctx.channel() ,new String[]{uniqueCode ,username ,password});

        SourceAgentCach.getInstance().offer(key , new ChannelCache(key ,ctx.channel() ,new Date())); // 登录通道

    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        super.channelInactive(ctx);

        logger.info("client -> close...");
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        super.exceptionCaught(ctx, cause);
    }

    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, ByteBuf buf) throws Exception {
        String channelID = channelHandlerContext.channel().id().toString();

        MsgPkg pkg = GBPackageDecode.unpack(buf);
        logger.info("通道ID：" + channelID + " -- 车机编码：" + pkg.getDevcode() + " -- 命令字：" + Byte.toString(pkg.getCmdId()) + " -- 原始数据：" + TransferSourceAgent.byteToString(pkg.getData()));

        if(pkg == null)
            return;

        switch (pkg.getCmdId()) {
            case 0x05 :

                GBLoginStatus.getInstance().offer(uniqueCode.concat(":").concat(username).concat(":").concat(password) , pkg); // 登录包
                break;

            case 0x06 :

//                GBLoginStatus.getInstance().remove(pkg.getDevcode().concat(Byte.toString((byte)0x05)));
//                String key = uniqueCode.concat(":").concat(username).concat(":").concat(password);
//                channelHandlerContext.channel().close();
//                SourceAgentCach.getInstance().remove(key); // 登出后清理缓存

                break;
        }

    }



}
