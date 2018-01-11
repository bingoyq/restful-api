package com.restful.entity;


import java.util.Date;

/**
 * 原始数据对象
 * 
 */
public class RawPkg {

	private byte[] base; // 原始数据信息
	private byte[] data; // 原始整包数据信息(从0x23开始到最后一个校验值byte)
	private byte[] context; // 原始数据单元数据信息
	private byte[] trans; // 原始转义数据信息(不包含0x23，0x23，和最后一个校验值byte)
	private byte[] response; // 应答数据信息
	private String devcode; // 车机ID
	private Date recvTime; // 平台时间 接收时间
	private Date respTime; // 平台时间 应答时间
	private Date devTime; // 车机时间
	private int funId; // 功能ID 命令标志
	private String name; // 消息标识
	private int sn; // 序号
	private int msgInfo; // 功能ID
	private String handler;
//	private DevAdaptor adaptor;
	private String reserve1;
	private int transferCount;//转发次数
	public byte[] getData() {
		return data;
	}

	public void setData(byte[] data) {
		this.data = data;
	}

	/**
	 * 获得车机ID
	 * 
	 * @return
	 */
	public String getDevcode() {
		return devcode;
	}

	/**
	 * 设置车机ID
	 * 
	 * @param devcode
	 */
	public void setDevcode(String devcode) {
		this.devcode = devcode;
	}

	/**
	 * 获得平台时间（接收时间）
	 * 
	 * @return
	 */
	public Date getRecvTime() {
		return recvTime;
	}

	/**
	 * 设置平台时间（接收时间）
	 * 
	 * @param recvTime
	 */
	public void setRecvTime(Date recvTime) {
		this.recvTime = recvTime;
	}

	/**
	 * 获得消息功能ID
	 * 
	 * @return
	 */
	public int getFunId() {
		return funId;
	}

	/**
	 * 设置消息功能ID
	 * 
	 * @param funId
	 */
	public void setFunId(int funId) {
		this.funId = funId;
	}


	public String bytesToString(byte[] data) {
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < data.length; i++) {
			sb.append(" " + Integer.toHexString(data[i] & 0xff));
		}
		return sb.toString();
	}

	public String getHandler() {
		return handler;
	}

	public void setHandler(String handler) {
		this.handler = handler;
	}

	public Date getDevTime() {
		return devTime;
	}

	public void setDevTime(Date devTime) {
		this.devTime = devTime;
	}

	public byte[] getResponse() {
		return response;
	}

	public void setResponse(byte[] response) {
		this.response = response;
	}

	public byte[] getBase() {
		return base;
	}

	public void setBase(byte[] base) {
		this.base = base;
	}


	public byte[] getTrans() {
		return trans;
	}

	public void setTrans(byte[] trans) {
		this.trans = trans;
	}

	public int getSn() {
		return sn;
	}

	public void setSn(int sn) {
		this.sn = sn;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getMsgInfo() {
		return msgInfo;
	}

	public void setMsgInfo(int msgInfo) {
		this.msgInfo = msgInfo;
	}

	public Date getRespTime() {
		return respTime;
	}

	public void setRespTime(Date respTime) {
		this.respTime = respTime;
	}

	public byte[] getContext() {
		return context;
	}

	public void setContext(byte[] context) {
		this.context = context;
	}

	public String getReserve1() {
		return reserve1;
	}

	public void setReserve1(String reserve1) {
		this.reserve1 = reserve1;
	}
	
	public int getTransferCount() {
		return transferCount;
	}

	public void setTransferCount(int transferCount) {
		this.transferCount = transferCount;
	}

	/**   
	 * <p>Title: clone</p>   
	 * <p>克隆一个新的消息</p>   
	 * @return   
	 * @see Object#clone()
	 */
	public RawPkg clone(){
		RawPkg raw=new RawPkg();
		raw.setData(this.getData());
		raw.setTrans(this.getTrans());
		raw.setContext(this.getContext());
		raw.setDevcode(this.getDevcode());
		raw.setDevTime(this.getDevTime());
		raw.setRecvTime(this.getRecvTime());
 		raw.setFunId(this.getFunId());
		raw.setName(this.getName());
		raw.setReserve1(this.getReserve1());
		return raw;
	}
}
