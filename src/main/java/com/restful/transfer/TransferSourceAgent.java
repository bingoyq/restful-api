package com.restful.transfer;

import com.gdcp.common.rabbitmq.serialize.RawPacker;
import com.restful.transfer.client.TransferClient;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.UUID;

public class TransferSourceAgent {

	private String uniqueCode;
	private String username ;
	private String password;

	private static Logger logger_ = LoggerFactory.getLogger(TransferSourceAgent.class);
	private String sourceKey;
	private String sourceid = UUID.randomUUID().toString();
	boolean isAllow=false;//是否允许发送数据包
	public  String url;
	private int recCount = 0;// 重连次数
	private long lastConnectTime = 0;// 最近一次重连时间

	private TransferClient client ;

	public TransferSourceAgent(String uniqueCode ,String username ,String password) {
		this.uniqueCode = uniqueCode;
		this.username = username;
		this.password = password;

		client =new TransferClient(uniqueCode ,username ,password);
	}

	/**   连接服务端
	 * @param url
	 * @return
	 * @throws
	 */
	public boolean connect(String url) {

//		new Thread(()->{
			try {
				client.connect(url);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
//		});

		return true;
	}

	public void reConnect() {

		try {
			client.connect(url);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

	}

	public void login(){

	}

	public void sendMsg(RawPacker packer) {
//		logger_.info("发送车机数据：" + packer.getDevcode() + " - " + byteToString(packer.getData()));
		String msg = "发送车机数据：" + packer.getDevcode() + " - " + byteToString(packer.getData());
		byte [] bs = packer.getData();
		ByteBuf buf = Unpooled.buffer(bs.length);
		buf.writeBytes(bs);
		client.send(buf , msg);
	}

	public static String byteToString(byte [] bytes) {
		StringBuffer bf = new StringBuffer();
		for (int i = 0; i < bytes.length; i++) {
			bf.append(Integer.toHexString(bytes[i] & 0xFF).toUpperCase() + " ");
		}
		return bf.toString();
	}

	public boolean isActive() {
		return client.isActive();
	}

	public void disconnect() throws Exception {
		client.close();
	}

	public String getSourceID() {
		return sourceid;
	}

	public String getSourceKey() {
		return sourceKey;
	}

	public void setSourceKey(String sourceKey) {
		this.sourceKey = sourceKey;
	}

	public void setAllow(boolean isAllow) {
		this.isAllow = isAllow;
	}

	public int getRecCount() {
		return recCount;
	}

	public void setRecCount(int recCount) {
		this.recCount = recCount;
	}

	public long getLastConnectTime() {
		return lastConnectTime;
	}

	public void setLastConnectTime(long lastConnectTime) {
		this.lastConnectTime = lastConnectTime;
	}

}
