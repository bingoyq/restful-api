package com.restful.transfer.client;


import com.restful.transfer.send.SenderAgent;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.LineBasedFrameDecoder;
import io.netty.handler.codec.string.StringDecoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;

/**
 * Created by yuanqiang on 2017/12/18.
 */
public class TransferClient {

    private static Logger logger = LoggerFactory.getLogger(TransferClient.class);

    private String uniqueCode;
    private String username ;
    private String password;

    private Channel channel ;

    public Channel getChannel() {
        return channel;
    }

    public TransferClient(String uniqueCode ,String username ,String password) {
        this.uniqueCode = uniqueCode;
        this.username = username;
        this.password = password;
    }

    public void connect(String url) throws InterruptedException {
        String [] str = url.split(":");

        EventLoopGroup group = new NioEventLoopGroup();
        try {
            Bootstrap b = new Bootstrap();
            b.group(group);
            b.channel(NioSocketChannel.class);
            b.remoteAddress(new InetSocketAddress(str[0], Integer.valueOf(str[1])));
            b.handler(new ChannelInitializer<SocketChannel>() {

                public void initChannel(SocketChannel ch) throws Exception {
                    //需要调用connect()方法来连接服务器端，但我们也可以通过调用bind()方法返回的ChannelFuture中获取Channel去connect服务器端。
                    ch.pipeline().addLast(new TransferHandler(new String []{uniqueCode , username , password}));
                    ch.pipeline().addLast(new LineBasedFrameDecoder(1024));
                    ch.pipeline().addLast(new StringDecoder());//channelRead的msg自动转换成了String类型
                }
            });
            //发起异步连接操作
            ChannelFuture f = b.connect().sync();

            if(f.channel().isActive())
                this.channel = f.channel();

            //等待客户端链路关闭
//            f.channel().closeFuture().sync();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
//            group.shutdownGracefully().sync();
        }
    }

    /**
     * 通道是否有效
     * @return
     */
    public boolean isActive(){
        return channel.isActive();
    }

    public void close(){
        channel.close();
    }

    public void send(ByteBuf buf ,String msg) {
        channel.writeAndFlush(buf);
        logger.info("send - 当前通道ID：" + channel.id().toString() + " -- " + msg);
    }

    /**
     * 平台登录
     * @param uniqueCode
     * @param username
     * @param password
     */
    public void login(String uniqueCode ,String username ,String password) {
        SenderAgent.login(channel ,new String[]{uniqueCode ,username ,password});
    }

}
